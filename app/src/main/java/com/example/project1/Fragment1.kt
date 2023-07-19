package com.example.project1

import NetworkStateObserver
import NfcObservable
import ProximityControl
import VibrationModeObservable
import android.bluetooth.BluetoothAdapter
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.project1.databinding.Fragment1Binding
import kotlin.properties.Delegates


class Fragment1 : Fragment(), ProximityControl.ProximityListener, WifiObservable.WifiStateListener {
    var PERMISSION_REQUEST_CODE by Delegates.notNull<Int>()

    private lateinit var wifiManager: WifiManager
    private lateinit var cameraManager: CameraManager
    private lateinit var nfc: NfcObservable
   // private lateinit var networkStateObserver: NetworkStateObserver
    private lateinit var audioManager: AudioManager
    private lateinit var vibrationModeObservable: VibrationModeObservable
    private lateinit var proximityControl: ProximityControl
    private lateinit var wifiObservable: WifiObservable
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var mobileInternetChecker: MobileInternetObservable


    private val bluetoothLiveData = MutableLiveData<Boolean>()
    private val wifiLiveData = MutableLiveData<Boolean>()
    private val networkStatus = MutableLiveData<Boolean>()


    private var _binding: Fragment1Binding? = null
    val binding get() = _binding!!

    lateinit var viewModel: ViewModel1


    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action != null) {
                if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            // Bluetooth turned off
                            bluetoothLiveData.value = false
                        }

                        BluetoothAdapter.STATE_ON -> {
                            // Bluetooth turned on
                            bluetoothLiveData.value = true
                        }

                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[ViewModel1::class.java]
        wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        //networkStateObserver = NetworkStateObserver(requireContext())
        nfc = NfcObservable(requireContext())
        vibrationModeObservable = VibrationModeObservable(requireContext())
        connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mobileInternetChecker = MobileInternetObservable(requireContext())






        _binding = Fragment1Binding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        observeData()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetooth()
            flash()
            network()


        }
        viewModel.simCardStatus(requireContext())


        nfc()
        setFlash()
        proximity()
        vibrate()

        wifiObservable = WifiObservable(requireContext())


        //According to documentation in 3rd party app it is not possible to change the state of wifi
        binding.checkboxWifi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableWifi()
            } else {
                disableWifi()
            }
        }











    }

    private fun enableWifi() {
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
            wifiLiveData.value = true
        }
    }

    private fun disableWifi() {
        if (wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = false
            wifiLiveData.value = false
        }
    }

    private fun setMobileDataEnabled(enabled: Boolean) {
        try {
            val setMobileDataEnabledMethod = ConnectivityManager::class.java.getDeclaredMethod(
                "setMobileDataEnabled", Boolean::class.javaPrimitiveType
            )
            setMobileDataEnabledMethod.invoke(connectivityManager, enabled)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun observeData() {
        bluetoothLiveData.observe(viewLifecycleOwner) {
            binding.switchBluetooth.isChecked = it
        }

        ViewModel1.liveDataSim.observe(viewLifecycleOwner) {
            binding.checkBoxSim.isChecked = it
        }

        ViewModel1.chargingSocket.observe(viewLifecycleOwner) {
            binding.checkBoxCharging.isChecked = it
        }










    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()
        registerBatteryStatusReceiver()
        registerBluetoothStatusReceiver()
        registerHeadphoneStatusReceiver()
        nfc.startObserving()
        proximityControl.start()
        wifiObservable.startMonitoring(this)
        vibrationModeObservable.startObserving()

    }


    override fun onPause() {
        super.onPause()
        unregisterBatteryStatusReceiver()
        unregisterBluetoothStatusReceiver()
        unregisterHeadphoneStatusRegister()
        nfc.stopObserving()
        proximityControl.stop()
        wifiObservable.stopMonitoring()
        vibrationModeObservable.stopObserving()
        //mobileInternetChecker.stopListening()




    }

    private fun registerBluetoothStatusReceiver() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        requireActivity().registerReceiver(bluetoothReceiver, filter)
    }

    private fun registerBatteryStatusReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        requireActivity().registerReceiver(ViewModel1.chargingSocketReceiver, filter)
    }

    private fun registerHeadphoneStatusReceiver() {
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        requireActivity().registerReceiver(ViewModel1.headphoneReceiver, filter)
    }

    private fun unregisterBluetoothStatusReceiver() {
        requireActivity().unregisterReceiver(bluetoothReceiver)
    }

    private fun unregisterBatteryStatusReceiver() {
        requireActivity().unregisterReceiver(ViewModel1.chargingSocketReceiver)
    }

    private fun unregisterHeadphoneStatusRegister() {
        requireActivity().unregisterReceiver(ViewModel1.headphoneReceiver)
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun bluetooth() {
        binding.switchBluetooth.setOnCheckedChangeListener { _, isChecked ->

            requestPermission(hasBluetoothPermission(), Manifest.permission.BLUETOOTH_CONNECT)
            if (!hasBluetoothPermission()) {
                binding.switchBluetooth.isChecked = false
            }

            if (hasBluetoothPermission() && isChecked) {
                viewModel.enableBluetooth()

            } else if (hasBluetoothPermission() && !isChecked) {
                viewModel.disableBluetooth()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun flash() {

        binding.checkBoxFlash.setOnCheckedChangeListener { buttonView, isChecked ->
            requestPermission(hasCameraPermission(), Manifest.permission.CAMERA)
            if(hasCameraPermission()){
                if (isChecked) {
                    turnOnFlash()
                } else turnOffFlash()
            }
            else Toast.makeText(requireContext(), "Camera permission needed", Toast.LENGTH_SHORT).show()

        }

    }

    private fun setFlash() {
        val flashlightMonitor = FlashlightStateMonitor(requireContext(), binding.checkBoxFlash)
        flashlightMonitor.startMonitoringFlashlightState()
    }


    private fun nfc() {
        val nfcObservable = NfcObservable(requireContext())
        nfcObservable.setNFCStateListener(object : NfcObservable.NFCStateListener {
            override fun onNFCStateChanged(enabled: Boolean) {
                binding.checkBoxNfc.isChecked = enabled
            }
        })

        nfcObservable.startObserving()
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())

        binding.checkBoxNfc.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                enableNFC()
            } else {
                disableNFC()
            }

        }

    }

    private fun enableNFC() {
        if (nfcAdapter != null) {
            if (!nfcAdapter!!.isEnabled) {
                // NFC adapter exists but is currently disabled
                // Prompt the user to enable NFC via system settings
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            } else {
                // NFC is already enabled
                Toast.makeText(requireContext(), "NFC is already enabled", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            // NFC is not supported on this device
            Toast.makeText(
                requireContext(),
                "NFC is not supported on this device",
                Toast.LENGTH_SHORT
            ).show()

            binding.checkBoxNfc.isChecked = false
        }
    }

    private fun disableNFC() {
        if (nfcAdapter != null) {
            if (nfcAdapter!!.isEnabled) {
                // NFC adapter exists and is currently enabled
                // Prompt the user to disable NFC via system settings
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            } else {
                // NFC is already disabled
                Toast.makeText(requireContext(), "NFC is already disabled", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            // NFC is not supported on this device
            Toast.makeText(
                requireContext(),
                "NFC is not supported on this device",
                Toast.LENGTH_SHORT
            ).show()
            binding.checkBoxNfc.isChecked = false


        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun vibrate() {

        vibrationModeObservable.startObserving()

        val vibrationModeChangeListener: (Boolean) -> Unit = { isInVibrationMode ->
            binding.checkBoxTitresim.isChecked = isInVibrationMode
        }

        vibrationModeObservable.setOnVibrationModeChangeListener(vibrationModeChangeListener)

        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager


        binding.checkBoxTitresim.setOnCheckedChangeListener { _, isChecked ->
            requestPermission(hasAudioPermission(), Manifest.permission.MODIFY_AUDIO_SETTINGS)
            if (isChecked) {
                // Change device mode to vibration
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            } else {
                // Change device mode to normal
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        }
    }







    private fun proximity() {
        proximityControl = ProximityControl(requireContext())
        proximityControl.setProximityListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun network() {
        val listener = object : MobileInternetListener {
            override fun onMobileInternetStatusChanged(isEnabled: Boolean) {
                binding.checkbox3G.isChecked = isEnabled
            }
        }
        mobileInternetChecker.startListening(listener)


        binding.checkbox3G.setOnCheckedChangeListener { _, isChecked ->
            requestPermission(hasNetworkPermission(), Manifest.permission.READ_PHONE_STATE)
            setMobileDataEnabled(isChecked)
        }
    }

    private fun turnOnFlash() {

        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true)
        } catch (e: CameraAccessException) {
            binding.checkBoxFlash.isChecked = false
            Toast.makeText(requireContext(), "Failed to turn on flash", Toast.LENGTH_SHORT).show()
        }
    }

    private fun turnOffFlash() {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Assuming the first camera has a flash
            cameraManager.setTorchMode(cameraId, false)
        } catch (e: CameraAccessException) {
            binding.checkBoxFlash.isChecked = true
            Toast.makeText(requireContext(), "Failed to turn off flash", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onProximityDetected() {
        binding.checkboxProxy.isChecked = true
    }

    override fun onProximityFar() {
        binding.checkboxProxy.isChecked = false
    }

    override fun onWifiStateChanged(wifiState: Int) {
        binding.checkboxWifi.isChecked = wifiState == WifiManager.WIFI_STATE_ENABLED

    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasBluetoothPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.BLUETOOTH_CONNECT
        ) ==
                PackageManager.PERMISSION_GRANTED

    private fun hasWifiPermission() =
        (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CHANGE_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED)




    private fun hasAudioPermission() =
        ActivityCompat.checkSelfPermission(requireContext(),
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasCameraPermission() =
        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermission(hasPermission: Boolean, vararg permission: String) {
        var permissionsToRequest = mutableListOf<String>(*permission)
        if (!hasPermission) {
            for (permissio in permission) {
                permissionsToRequest.add(permissio)
            }
        }

        if (hasPermission == hasBluetoothPermission()) {
            PERMISSION_REQUEST_CODE = 2
        } else if (hasPermission == hasWifiPermission()) {
            PERMISSION_REQUEST_CODE = 1
        } else if (hasPermission == hasNetworkPermission())
            PERMISSION_REQUEST_CODE = 3

        else if(hasPermission == hasAudioPermission()) PERMISSION_REQUEST_CODE = 4
        else if(hasPermission == hasCameraPermission()) PERMISSION_REQUEST_CODE =5

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }


    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val a = PERMISSION_REQUEST_CODE
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permission Granted.", Toast.LENGTH_LONG)
                        .show()

                }
            }
        }
    }


}
