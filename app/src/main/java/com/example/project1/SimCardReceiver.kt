package com.example.project1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.SIM_STATE_ABSENT
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/*class SimCardReceiver: BroadcastReceiver(){

    //private val _simCardStatus = MutableLiveData<Boolean>()
    //val simCardStatus: LiveData<Boolean> = _simCardStatus

    /* override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.SIM_STATE_CHANGED") {
            val simState = intent.getStringExtra("ss")
            val isSimCardPresent = simState != "ABSENT"

            val viewModel = ViewModel1.getInstance()
            viewModel.setSimCardPresent(isSimCardPresent)
        }
    }
*/
    /*override fun onReceive(context: Context, intent: Intent) {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simState = telephonyManager.simState
        Log.d("simState", simState.toString())
        val isSimCardPresent = simState != TelephonyManager.SIM_STATE_ABSENT
        Log.w("isSimCardPresent", isSimCardPresent.toString())

        val viewModel = ViewModel1.getInstance()
        viewModel.setSimCardPresent(isSimCardPresent)
    }*/

    /*override fun onReceive(context: Context, intent: Intent) {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simState = telephonyManager.getSimState()
        val viewModel = ViewModel1.getInstance()
        val isSimCardPresent = simState != TelephonyManager.SIM_STATE_ABSENT
        viewModel.setSimCardPresent(isSimCardPresent)

    }*/
}
*/
