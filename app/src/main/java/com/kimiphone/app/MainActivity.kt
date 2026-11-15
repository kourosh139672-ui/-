package com.kimiphone.app

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val green = Color.rgb(22, 163, 74)
    private val ink = Color.rgb(28, 33, 40)
    private val muted = Color.rgb(112, 120, 130)
    private val bg = Color.rgb(247, 249, 248)
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout
    private lateinit var number: EditText
    private var currentTab = "شماره‌گیر"
    private var recordingOption = false

    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun shape(color:Int, radius:Int=22, stroke:Int?=null)=GradientDrawable().apply {
        setColor(color); cornerRadius=dp(radius).toFloat()
        if(stroke!=null) setStroke(dp(1),stroke)
    }
    private fun txt(s:String,size:Float=16f,bold:Boolean=false,color:Int=ink)=TextView(this).apply {
        text=s; textSize=size; setTextColor(color); gravity=Gravity.CENTER_VERTICAL
        if(bold) setTypeface(typeface,Typeface.BOLD)
        textDirection=View.TEXT_DIRECTION_FIRST_STRONG
    }
    private fun pill(s:String, action:()->Unit, color:Int=Color.WHITE, fg:Int=ink):TextView =
        TextView(this).apply {
            text=s; textSize=14f; setTextColor(fg); gravity=Gravity.CENTER
            background=shape(color,28,Color.rgb(232,236,234)); setPadding(dp(16),dp(12),dp(16),dp(12))
            setOnClickListener{action()}
        }
    private fun bubble(s:String, action:()->Unit, iconColor:Int=ink):TextView =
        TextView(this).apply {
            text=s; textSize=14f; setTextColor(iconColor); gravity=Gravity.CENTER
            background=shape(Color.WHITE,26,Color.rgb(235,239,237))
            setPadding(dp(12),dp(13),dp(12),dp(13)); setOnClickListener{action()}
        }

    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState); window.statusBarColor=bg; window.navigationBarColor=bg; window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR; showHome()}
    private fun showHome(){
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(bg);setPadding(dp(18),dp(16),dp(18),dp(10))}
        val header=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        val brand=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
        brand.addView(txt("Kimi Phone",27f,true)); brand.addView(txt("تماس‌ها، مخاطبین و شماره‌گیر",12f,false,muted))
        header.addView(brand); header.addView(bubble("⚙", {showSettings()}))
        root.addView(header)
        number=EditText(this).apply{
            hint="شماره تلفن"; textSize=26f; gravity=Gravity.CENTER
            textDirection=View.TEXT_DIRECTION_LTR; setTextColor(ink); setHintTextColor(muted)
            inputType=android.text.InputType.TYPE_CLASS_PHONE
            background=shape(Color.WHITE,24,Color.rgb(235,239,237))
            setPadding(dp(12),dp(10),dp(12),dp(10)); isSingleLine=true
        }
        root.addView(number,LinearLayout.LayoutParams(-1,dp(62)).apply{topMargin=dp(20);bottomMargin=dp(12)})
        body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.TOP}
        root.addView(body,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;background=shape(Color.WHITE,26)}
        listOf("علاقه‌مندی","اخیر","مخاطبین","شماره‌گیر").forEach{tab->
            val v=TextView(this).apply{
                text=tab;textSize=12f;gravity=Gravity.CENTER
                setTextColor(if(tab==currentTab) green else muted)
                setPadding(dp(5),dp(15),dp(5),dp(15))
                setOnClickListener{currentTab=tab;renderTab()}
            }
            nav.addView(v,LinearLayout.LayoutParams(0,-2,1f))
        }
        root.addView(nav,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        setContentView(root); renderTab()
    }
    private fun renderTab(){when(currentTab){"شماره‌گیر"->showKeypad();"مخاطبین"->showContacts();"اخیر"->showRecent();else->showFavorites()}}
    private fun showKeypad(){
        body.removeAllViews()
        body.addView(txt("شماره‌گیر",20f,true))
        val keys=listOf("1","2","3","4","5","6","7","8","9","*","0","#")
        for(r in 0..3){
            val row=LinearLayout(this).apply{gravity=Gravity.CENTER}
            for(c in 0..2){
                val key=keys[r*3+c]
                val v=TextView(this).apply{
                    text=key;textSize=25f;setTextColor(ink);gravity=Gravity.CENTER
                    background=shape(Color.WHITE,30,Color.rgb(237,240,238))
                    setOnClickListener{number.append(key)}
                }
                row.addView(v,LinearLayout.LayoutParams(0,dp(54),1f).apply{setMargins(dp(5),dp(4),dp(5),dp(4))})
            }
            body.addView(row)
        }
        val actions=LinearLayout(this).apply{gravity=Gravity.CENTER;orientation=LinearLayout.HORIZONTAL}
        actions.addView(bubble("⌫ پاک‌کردن",{val s=number.text.toString();if(s.isNotEmpty())number.setText(s.dropLast(1))}))
        val call=TextView(this).apply{
            text="☎";textSize=29f;setTextColor(Color.WHITE);gravity=Gravity.CENTER
            background=shape(green,50);setOnClickListener{placeCall()}
        }
        actions.addView(call,LinearLayout.LayoutParams(dp(76),dp(58)).apply{leftMargin=dp(18);rightMargin=dp(18)})
        actions.addView(bubble("＋ افزودن",{if(number.text.isNotEmpty())showCreateContact()}))
        body.addView(actions,LinearLayout.LayoutParams(-1,dp(76)).apply{topMargin=dp(8)})
        val options=LinearLayout(this).apply{gravity=Gravity.CENTER;orientation=LinearLayout.HORIZONTAL}
        options.addView(bubble("مسدودسازی",{showBlocked()}),LinearLayout.LayoutParams(0,-2,1f).apply{setMargins(dp(4),0,dp(4),0)})
        options.addView(bubble("تنظیمات",{showSettings()}),LinearLayout.LayoutParams(0,-2,1f).apply{setMargins(dp(4),0,dp(4),0)})
        body.addView(options)
    }
    private fun placeCall(){
        val phone=number.text.toString().trim()
        if(phone.isEmpty()){number.error="اول شماره را وارد کن";return}
        if(checkSelfPermission(Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.CALL_PHONE),10);return}
        try{startActivity(Intent(Intent.ACTION_CALL,Uri.parse("tel:"+Uri.encode(phone))))}
        catch(e:Exception){number.error="تماس برقرار نشد"}
    }
    private fun showRecent(){
        body.removeAllViews();body.addView(txt("تماس‌های اخیر",20f,true))
        if(checkSelfPermission(Manifest.permission.READ_CALL_LOG)!=PackageManager.PERMISSION_GRANTED){
            body.addView(pill("اجازهٔ نمایش تماس‌ها",{requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG),11)}));return
        }
        try{
            val c:Cursor?=contentResolver.query(CallLog.Calls.CONTENT_URI,arrayOf(CallLog.Calls.CACHED_NAME,CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DATE),null,null,CallLog.Calls.DATE+" DESC")
            c?.use{while(it.moveToNext()&&body.childCount<16){
                val name=it.getString(0)?:it.getString(1)?: "ناشناس"
                val phone=it.getString(1)?: "";val type=it.getInt(2)
                val whenText=SimpleDateFormat("HH:mm  yyyy/MM/dd",Locale.getDefault()).format(Date(it.getLong(3)))
                addPersonRow(name,phone,whenText+if(type==CallLog.Calls.MISSED_TYPE)"  • تماس ازدست‌رفته" else "")
            }}
        }catch(_:Exception){body.addView(txt("دسترسی به سابقه تماس ممکن نیست.",14f,false,muted))}
    }
    private fun showContacts(){
        body.removeAllViews();body.addView(txt("مخاطبین",20f,true))
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){
            body.addView(pill("نمایش مخاطبین",{requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),12)}));return
        }
        try{
            val c=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER),null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")
            c?.use{while(it.moveToNext()&&body.childCount<30)addPersonRow(it.getString(0)?: "بدون نام",it.getString(1)?: "","برای تماس لمس کن")}
        }catch(_:Exception){body.addView(txt("مخاطبین بارگذاری نشدند.",14f,false,muted))}
        body.addView(pill("＋ مخاطب جدید",{showCreateContact()}))
    }
    private fun addPersonRow(name:String,phone:String,sub:String){
        val row=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=shape(Color.WHITE,18);setPadding(dp(14),dp(10),dp(14),dp(10));setOnClickListener{number.setText(phone);currentTab="شماره‌گیر";renderTab()}}
        row.addView(txt(name,16f,true));row.addView(txt("$phone   •   $sub",12f,false,muted))
        body.addView(row,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
    }
    private fun showFavorites(){body.removeAllViews();body.addView(txt("علاقه‌مندی‌ها",20f,true));body.addView(txt("برای افزودن مخاطب، از بخش مخاطبین استفاده کن.",14f,false,muted))}
    private fun showCreateContact(){
        val i=Intent(Intent.ACTION_INSERT).setType(ContactsContract.Contacts.CONTENT_TYPE)
        if(number.text.isNotEmpty())i.putExtra(ContactsContract.Intents.Insert.PHONE,number.text.toString())
        startActivity(i)
    }
    private fun showBlocked(){body.removeAllViews();body.addView(txt("مسدودسازی شماره",20f,true));body.addView(txt("برای مسدودسازی کامل، Kimi Phone باید برنامهٔ پیش‌فرض تماس باشد و قابلیت مسدودسازی سیستم را پشتیبانی کند.",14f,false,muted));body.addView(pill("درخواست برنامهٔ پیش‌فرض تماس",{requestDialerRole()}))}
    private fun showSettings(){
        body.removeAllViews();body.addView(txt("تنظیمات",22f,true))
        body.addView(txt("ظاهر و تماس",16f,true).apply{setPadding(0,dp(16),0,dp(8))})
        body.addView(pill("انتخاب Kimi Phone به‌عنوان برنامهٔ تماس پیش‌فرض",{requestDialerRole()}))
        body.addView(txt("ضبط تماس",16f,true).apply{setPadding(0,dp(18),0,dp(5))})
        body.addView(txt("ضبط صدای تماس سیم‌کارت در اندروید محدودیت دارد و این گزینه به‌تنهایی ضبط واقعی را فعال نمی‌کند. فقط در دستگاه‌هایی که ضبط رسمی و مجاز دارند قابل استفاده است.",13f,false,muted))
        val sw=Switch(this).apply{text="نمایش گزینه ضبط تماس";isChecked=recordingOption;setOnCheckedChangeListener{_,v->recordingOption=v}}
        body.addView(sw)
        body.addView(txt("نسخه 1.1 • Kimi Phone",12f,false,muted).apply{setPadding(0,dp(22),0,0)})
    }
    private fun requestDialerRole(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            val manager=getSystemService(RoleManager::class.java)
            if(manager.isRoleAvailable(RoleManager.ROLE_DIALER)&&!manager.isRoleHeld(RoleManager.ROLE_DIALER))startActivityForResult(manager.createRequestRoleIntent(RoleManager.ROLE_DIALER),30)
            else Toast.makeText(this,"Kimi Phone برنامهٔ پیش‌فرض تماس است.",Toast.LENGTH_SHORT).show()
        }else startActivity(Intent(Intent.ACTION_DIAL))
    }
    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED)renderTab()}
}
