package com.kimiphone.app

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private lateinit var number: EditText
    private lateinit var content: LinearLayout
    private val ink = Color.rgb(32, 36, 42)
    private val muted = Color.rgb(112, 119, 128)

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun card() = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dp(20).toFloat() }
    private fun label(t: String, s: Float = 16f, b: Boolean = false) = TextView(this).apply {
        text = t; textSize = s; setTextColor(ink)
        if (b) setTypeface(typeface, Typeface.BOLD)
    }
    private fun button(t: String, action: () -> Unit) = Button(this).apply {
        text = t; isAllCaps = false; setTextColor(ink); background = card()
        setOnClickListener { action() }
    }

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showHome() }

    private fun showHome() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(12))
            setBackgroundColor(Color.rgb(246, 247, 249))
        }
        root.addView(label("Kimi Phone", 28f, true))
        root.addView(label("تماس‌ها و مخاطبین", 13f).apply { setTextColor(muted) })
        number = EditText(this).apply {
            hint = "شماره تلفن"; textSize = 23f; gravity = android.view.Gravity.CENTER
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            background = card(); setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        root.addView(number, LinearLayout.LayoutParams(-1, dp(64)).apply { topMargin = dp(20) })
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = android.view.Gravity.CENTER }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { gravity = android.view.Gravity.CENTER }
        listOf("اخیر", "مخاطبین", "شماره‌گیر", "مسدودها").forEachIndexed { i, title ->
            nav.addView(button(title) {
                when (i) { 0 -> showRecent(); 1 -> showContacts(); 2 -> showKeypad(); else -> showBlocked() }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(nav)
        setContentView(root)
        showKeypad()
    }

    private fun showKeypad() {
        content.removeAllViews()
        content.addView(label("شماره‌گیر", 20f, true))
        val keys = listOf("1","2","3","4","5","6","7","8","9","*","0","#")
        for (r in 0..3) {
            val line = LinearLayout(this).apply { gravity = android.view.Gravity.CENTER }
            for (c in 0..2) {
                val key = keys[r * 3 + c]
                line.addView(button(key) { number.append(key) },
                    LinearLayout.LayoutParams(0, dp(54), 1f).apply { setMargins(dp(4), dp(3), dp(4), dp(3)) })
            }
            content.addView(line)
        }
        content.addView(button("تماس") { placeCall() }, LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(12) })
        content.addView(button("پاک کردن") { val n = number.text.toString(); if (n.isNotEmpty()) number.setText(n.dropLast(1)) })
        content.addView(button("انتخاب برنامه پیش‌فرض تماس") { requestDialerRole() })
    }

    private fun showRecent() {
        content.removeAllViews()
        content.addView(label("تماس‌های اخیر", 20f, true))
        content.addView(label("اتصال به سابقه تماس گوشی در مرحله بعد اضافه می‌شود.", 14f))
    }
    private fun showContacts() {
        content.removeAllViews()
        content.addView(label("مخاطبین", 20f, true))
        content.addView(label("اتصال دفترچه مخاطبین در مرحله بعد اضافه می‌شود.", 14f))
        content.addView(button("درخواست مجوز مخاطبین") {
            if (Build.VERSION.SDK_INT >= 23) requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 20)
        })
    }
    private fun showBlocked() {
        content.removeAllViews()
        content.addView(label("شماره‌های مسدودشده", 20f, true))
        content.addView(label("مدیریت مسدودسازی تماس‌ها در مرحله بعد پیاده‌سازی می‌شود.", 14f))
    }
    private fun placeCall() {
        val phone = number.text.toString().trim()
        if (phone.isEmpty()) { number.error = "شماره‌ای وارد کن"; return }
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 10); return
        }
        try { startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))) }
        catch (e: Exception) { number.error = "تماس انجام نشد" }
    }
    private fun requestDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val manager = getSystemService(RoleManager::class.java)
            if (manager.isRoleAvailable(RoleManager.ROLE_DIALER) && !manager.isRoleHeld(RoleManager.ROLE_DIALER))
                startActivityForResult(manager.createRequestRoleIntent(RoleManager.ROLE_DIALER), 30)
        } else startActivity(Intent(Intent.ACTION_DIAL))
    }
}
