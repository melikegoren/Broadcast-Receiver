import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

class VibrationModeObservable(private val context: Context) {

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var callback: ((Boolean) -> Unit)? = null
    lateinit var receiver: BroadcastReceiver

    fun setOnVibrationModeChangeListener(listener: (Boolean) -> Unit) {
        callback = listener
    }

    fun startObserving() {
        callback?.invoke(isDeviceInVibrationMode())

        val filter = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                    callback?.invoke(isDeviceInVibrationMode())
                }
            }
        }

        context.registerReceiver(receiver, filter)
    }

    fun stopObserving() {
        callback = null
        receiver?.let {
            context.unregisterReceiver(it)
        }
    }

    private fun isDeviceInVibrationMode(): Boolean {
        val currentMode = audioManager.ringerMode
        return currentMode == AudioManager.RINGER_MODE_VIBRATE
    }
}
