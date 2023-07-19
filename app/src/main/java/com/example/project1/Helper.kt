package com.example.project1

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.IntentFilter


class Helper {

    fun register(filter: IntentFilter, receiver: BroadcastReceiver, activity: Activity){
        activity.registerReceiver(receiver, filter)

    }

    fun unregister(receiver: BroadcastReceiver, activity: Activity){
        activity.unregisterReceiver(receiver)
    }

}