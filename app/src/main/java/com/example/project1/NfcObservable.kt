import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter

class NfcObservable(private val context: Context) {

    private var nfcAdapter: NfcAdapter? = null
    private var nfcStateListener: NFCStateListener? = null
    private val nfcBroadcastReceiver: BroadcastReceiver

    init {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context)

        nfcBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                    val state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)
                    val isNFCEnabled = state == NfcAdapter.STATE_ON
                    nfcStateListener?.onNFCStateChanged(isNFCEnabled)
                }
            }
        }
    }

    fun setNFCStateListener(listener: NFCStateListener) {
        nfcStateListener = listener
    }

    fun startObserving() {
        if (nfcAdapter == null) {
            nfcStateListener?.onNFCStateChanged(false)
            return
        }

        val intentFilter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        context.registerReceiver(nfcBroadcastReceiver, intentFilter)

        val isNFCEnabled = nfcAdapter?.isEnabled ?: false
        nfcStateListener?.onNFCStateChanged(isNFCEnabled)
    }

    fun stopObserving() {
        try {
            context.unregisterReceiver(nfcBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered, ignore.
        }
    }

    interface NFCStateListener {
        fun onNFCStateChanged(enabled: Boolean)
    }
}
