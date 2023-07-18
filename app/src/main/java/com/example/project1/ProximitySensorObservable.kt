import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor

import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ProximityControl(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var proximityListener: ProximityListener? = null

    interface ProximityListener {
        fun onProximityDetected()
        fun onProximityFar()
    }

    fun setProximityListener(listener: ProximityListener) {
        proximityListener = listener
    }

    fun start() {
        proximitySensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_PROXIMITY)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent) {
        val distance = event.values[0]

        if (distance < (proximitySensor?.maximumRange ?: 0.0).toFloat()) {
            proximityListener?.onProximityDetected()
        } else {
            proximityListener?.onProximityFar()
        }
    }
}

/*class ProximitySensorObservable(private val context: Context) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val observers: MutableList<Observer> = mutableListOf()
    private val proximitySensorReceiver = ProximitySensorReceiver()

    fun addObserver(observer: Observer) {
        observers.add(observer)
    }

    fun removeObserver(observer: Observer) {
        observers.remove(observer)
    }

    fun startListening() {
        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        context.registerReceiver(proximitySensorReceiver, intentFilter)
    }

    fun stopListening() {
        context.unregisterReceiver(proximitySensorReceiver)
    }

    private fun notifyObservers(proximityEnabled: Boolean) {
        for (observer in observers) {
            observer.onProximityStateChanged(proximityEnabled)
        }
    }

    interface Observer {
        fun onProximityStateChanged(proximityEnabled: Boolean)
    }

    private inner class ProximitySensorReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                val proximityEnabled =
                    sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
                notifyObservers(proximityEnabled)
            }
        }
    }
}*/
