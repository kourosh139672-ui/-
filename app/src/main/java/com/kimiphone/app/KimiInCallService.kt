package com.kimiphone.app

import android.telecom.Call
import android.telecom.InCallService
import android.content.Intent

class KimiInCallService : InCallService() {
    companion object { var activeCall: Call? = null }
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        activeCall = call
        call.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                if (state == Call.STATE_DISCONNECTED) activeCall = null
                val i = Intent(this@KimiInCallService, CallScreenActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(i)
            }
        })
        val i = Intent(this, CallScreenActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(i)
    }
    override fun onCallRemoved(call: Call) {
        if (activeCall == call) activeCall = null
        super.onCallRemoved(call)
    }
}