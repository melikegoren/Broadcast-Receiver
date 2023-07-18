package com.example.project1

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.nfc.NfcManager
import android.os.BatteryManager
import android.os.Vibrator
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class ViewModel1 : ViewModel() {

    //val bluetoothEnabled = MutableLiveData<Boolean>()
    lateinit var wifiManager: WifiManager


    @SuppressLint("MissingPermission")
    fun enableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.enable()
    }

    @SuppressLint("MissingPermission")
    fun disableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.disable()
    }

    fun toggleWifi(context: Context) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = wifiManager.isWifiEnabled

    }


    companion object {

        val headPhoneStatus = MutableLiveData<Boolean>()
        val chargingSocket = MutableLiveData<Boolean>()
        val liveDataSim = MutableLiveData<Boolean>()
        val nfc = MutableLiveData<Boolean>()
        val vibration = MutableLiveData<Boolean>()


        val headphoneReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                    val state = intent.getIntExtra("state", -1)
                    if (state == 0) {
                        headPhoneStatus.value = false
                    } else if (state == 1) {
                        headPhoneStatus.value = true
                    }
                }
            }
        }

        val chargingSocketReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                chargingSocket.value = isCharging
            }
        }

        val nfcReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action

                if (action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                    val state = intent.getIntExtra(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED,NfcAdapter.STATE_ON)
                    when (state) {
                        NfcAdapter.STATE_OFF -> { nfc.value = false }
                        NfcAdapter.STATE_ON -> { nfc.value = true}
                        else -> { nfc.value = true}

                    }
                }

            }

        }

        val vibrationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action

                val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibration.value = vibrator.hasVibrator()
            }
        }

    }

    fun simCardStatus(context: Context) {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val hasSimCard = telephonyManager.simState == TelephonyManager.SIM_STATE_ABSENT
        liveDataSim.value = !hasSimCard
    }
}






