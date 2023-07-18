import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager

class WifiConnectionObservable(private val context: Context) {

    // BroadcastReceiver to monitor WiFi connection changes
    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                val wifiState = intent.getIntExtra(
                    WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN
                )
                val isConnected = wifiState == WifiManager.WIFI_STATE_ENABLED
                updateConnectionStatus(isConnected)
            }
        }
    }

    // Method to start monitoring WiFi connection changes
    fun startMonitoring() {
        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(wifiReceiver, intentFilter)
    }

    // Method to stop monitoring WiFi connection changes
    fun stopMonitoring() {
        context.unregisterReceiver(wifiReceiver)
    }

    // Listener interface for WiFi connection status changes
    interface ConnectionStatusListener {
        fun onConnectionStatusChanged(isConnected: Boolean)
    }

    // Collection of registered listeners
    private val listeners = mutableListOf<ConnectionStatusListener>()

    // Method to register a listener for WiFi connection status changes
    fun registerListener(listener: ConnectionStatusListener) {
        listeners.add(listener)
    }

    // Method to unregister a listener
    fun unregisterListener(listener: ConnectionStatusListener) {
        listeners.remove(listener)
    }

    // Method to update the WiFi connection status and notify listeners
    private fun updateConnectionStatus(isConnected: Boolean) {
        for (listener in listeners) {
            listener.onConnectionStatusChanged(isConnected)
        }
    }
}
