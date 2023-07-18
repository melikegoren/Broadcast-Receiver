import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import androidx.lifecycle.LiveData

class NetworkStateObserver(private val context: Context) : LiveData<Boolean>() {
    private val telephonyManager: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val cmManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onServiceStateChanged(serviceState: ServiceState) {
            postValue(is3GEnabled())
        }
    }

    private val networkStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            postValue(is3GEnabled())
        }
    }

    override fun onActive() {
        super.onActive()
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE)
        val filter = IntentFilter()
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        context.registerReceiver(networkStateReceiver, filter)
    }

    override fun onInactive() {
        super.onInactive()
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        context.unregisterReceiver(networkStateReceiver)
    }

    @SuppressLint("MissingPermission")
    private fun is3GEnabled(): Boolean {
        val networkType = cmManager.getNetworkCapabilities(cmManager.activeNetwork)
        return networkType!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
