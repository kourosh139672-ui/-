package com.kimiphone.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.telecom.Call
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class CallScreenActivity : Activity() {
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun panel(c:Int,r:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);render()}
    override fun onResume(){super.onResume();render()}
    private fun render(){
        val call=KimiInCallService.activeCall
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(24),dp(30),dp(24),dp(30));setBackgroundColor(Color.rgb(247,249,248))}
        val title=TextView(this).apply{text="Kimi Phone";textSize=20f;setTextColor(Color.rgb(35,40,45));gravity=Gravity.CENTER}
        root.addView(title)
        val details=call?.details
        val number=details?.handle?.schemeSpecificPart?: "تماس"
        root.addView(TextView(this).apply{text=number;textSize=27f;setTextColor(Color.rgb(30,35,40));gravity=Gravity.CENTER;setPadding(0,dp(30),0,dp(8))})
        val state=when(call?.state){Call.STATE_RINGING->"تماس ورودی";Call.STATE_DIALING->"در حال شماره‌گیری…";Call.STATE_ACTIVE->"تماس برقرار است";Call.STATE_HOLDING->"تماس در انتظار";else->"تماس"}
        root.addView(TextView(this).apply{text=state;textSize=15f;setTextColor(Color.GRAY);gravity=Gravity.CENTER})
        val spacer=TextView(this);root.addView(spacer,LinearLayout.LayoutParams(1,dp(110)))
        val row=LinearLayout(this).apply{gravity=Gravity.CENTER}
        if(call?.state==Call.STATE_RINGING){
            row.addView(action("پاسخ",Color.rgb(22,163,74)){call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)})
            row.addView(action("رد تماس",Color.rgb(220,55,65)){call.reject(false,null);finish()})
        }else{
            row.addView(action("قطع تماس",Color.rgb(220,55,65)){call?.disconnect();finish()})
        }
        root.addView(row);setContentView(root)
    }
    private fun action(t:String,c:Int,run:()->Unit)=TextView(this).apply{text=t;textSize=17f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;background=panel(c,30);setPadding(dp(24),dp(18),dp(24),dp(18));setOnClickListener{run()}}
}