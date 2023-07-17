package com.example.project1

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BluetoothBroadcastReceiver(private val listener: BluetoothStateChangeListener) : BroadcastReceiver() {


    interface BluetoothStateChangeListener {
        fun onBluetoothStateChanged(isEnabled: Boolean)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            val isEnabled = state == BluetoothAdapter.STATE_ON
            listener.onBluetoothStateChanged(isEnabled)
        }
    }
}