package com.example.project1import

import android.content.Context
import android.net.wifi.WifiManager
import android.widget.CheckBox


// Observer class
class WifiConnectionStatusListener(private val checkBox: CheckBox, private val context: Context): WifiConnectionObservable.ConnectionStatusListener {
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    override fun onConnectionStatusChanged(isConnected: Boolean) {
        checkBox.isChecked = isConnected

    }


}

