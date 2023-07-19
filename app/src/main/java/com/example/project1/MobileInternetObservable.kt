package com.example.project1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

class MobileInternetObservable(private val context: Context) {

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }


    private val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    private var listener: MobileInternetListener? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            listener?.onMobileInternetStatusChanged(isMobileInternetEnabled())
        }
    }

    fun startListening(listener: MobileInternetListener) {
        this.listener = listener
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    fun stopListening() {
        context.unregisterReceiver(broadcastReceiver)
        listener = null
    }

    fun isMobileInternetEnabled(): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_MOBILE


    }
}

interface MobileInternetListener {
    fun onMobileInternetStatusChanged(isEnabled: Boolean)
}
