import android.content.Context
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

