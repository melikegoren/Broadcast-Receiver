import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NfcEnabledObservable(private val context: Context) {

    private val liveData = MutableLiveData<Boolean>()
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    private var listener: NfcEnabledListener? = null

    fun setNfcEnabledListener(listener: NfcEnabledListener) {
        this.listener = listener
    }

    fun startObserving() {
        listener?.onNfcEnabledChanged(isNfcEnabled())

        nfcAdapter?.let { adapter ->
            val callback = object : NfcAdapter.OnNdefPushCompleteCallback,
                NfcAdapter.CreateNdefMessageCallback {
                override fun onNdefPushComplete(p0: NfcEvent?) {
                    listener?.onNfcEnabledChanged(isNfcEnabled())
                }

                override fun createNdefMessage(p0: NfcEvent?): NdefMessage? {
                    // Do nothing
                    return null
                }
            }

            adapter.setOnNdefPushCompleteCallback(callback, null)
        }
    }

    fun stopObserving() {
        nfcAdapter?.setOnNdefPushCompleteCallback(null, null)
        listener = null
    }

    fun isNfcEnabled(): Boolean {
        liveData.value = nfcAdapter?.isEnabled ?: false
        return nfcAdapter?.isEnabled ?: false
    }

    fun liveData():LiveData<Boolean>{
        return liveData
    }
    interface NfcEnabledListener {
        fun onNfcEnabledChanged(enabled: Boolean)
    }
}
