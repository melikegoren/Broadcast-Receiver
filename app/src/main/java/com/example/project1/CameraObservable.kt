import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.widget.CheckBox
import androidx.core.app.ActivityCompat

class CameraObservable(private val context: Context, private val checkBox: CheckBox, private val activity: Activity) {
    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var isCameraOpen: Boolean = false
    private val cameraStateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == CAMERA_STATE_CHANGED_ACTION) {
                val cameraId = intent.getStringExtra(EXTRA_CAMERA_ID)
                val isOpen = intent.getBooleanExtra(EXTRA_CAMERA_STATE, false)
                if (cameraId != null && cameraId == getBackCameraId() && isOpen) {
                    isCameraOpen = true
                } else {
                    isCameraOpen = false
                }
                checkBox.isChecked = isCameraOpen
            }
        }
    }

    fun startObserving() {
        // Register broadcast receiver to listen for camera state changes
        val filter = IntentFilter(CAMERA_STATE_CHANGED_ACTION)
        context.registerReceiver(cameraStateBroadcastReceiver, filter)

        // Check if camera is already open
        val cameraId = getBackCameraId()
        if (cameraId != null) {
            try {
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                val isCameraAvailable =
                    cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
                isCameraOpen = isCameraAvailable && cameraManager.cameraIdList.contains(cameraId)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        // Set checkbox state based on camera status
        checkBox.isChecked = isCameraOpen

        // Request camera permission if needed
        if (!isCameraOpen && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun stopObserving() {
        // Unregister the broadcast receiver
        context.unregisterReceiver(cameraStateBroadcastReceiver)
    }

    private fun getBackCameraId(): String? {
        try {
            val cameraIds = cameraManager.cameraIdList
            for (cameraId in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private const val CAMERA_STATE_CHANGED_ACTION = "com.example.camera_state_changed"
        private const val EXTRA_CAMERA_ID = "camera_id"
        private const val EXTRA_CAMERA_STATE = "camera_state"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }
}
