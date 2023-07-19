package com.example.project1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.util.Log

class WifiObservable(private val context: Context) {
    private var wifiStateListener: WifiStateListener? = null
    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val wifiBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (WifiManager.WIFI_STATE_CHANGED_ACTION == action) {
                val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                wifiStateListener?.onWifiStateChanged(wifiState)


            }
        }
    }

    fun startMonitoring(listener: WifiStateListener) {
        wifiStateListener = listener
        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(wifiBroadcastReceiver, intentFilter)

        Log.d("wifiManager", wifiManager.toString())

    }

    fun stopMonitoring() {
        wifiStateListener = null
        context.unregisterReceiver(wifiBroadcastReceiver)
    }



    interface WifiStateListener {
        fun onWifiStateChanged(wifiState: Int)
    }
}
