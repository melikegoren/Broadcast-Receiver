package com.example.project1

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.widget.CheckBox

class FlashlightStateMonitor(private val context: Context, private val checkBox: CheckBox) {
    private val TAG = FlashlightStateMonitor::class.java.simpleName
    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun startMonitoringFlashlightState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager.registerTorchCallback(torchCallback, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register torch callback: ${e.message}")
            }
        } else {
            Log.e(TAG, "Flashlight state monitoring requires at least Android Marshmallow (API 23)")
        }
    }

    fun stopMonitoringFlashlightState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.unregisterTorchCallback(torchCallback)
        }
    }

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)
            Log.d(TAG, "Flashlight state changed for camera $cameraId: ${if (enabled) "ON" else "OFF"}")

                checkBox.isChecked = enabled
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            super.onTorchModeUnavailable(cameraId)
            Log.e(TAG, "Flashlight not available for camera $cameraId")
        }
    }
}
