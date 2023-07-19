package com.example.project1

import NfcObservable
import ProximityControl
import VibrationModeObservable
import android.bluetooth.BluetoothAdapter
import android.Manifest
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
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.project1.databinding.Fragment1Binding
import kotlin.properties.Delegates


class Fragment1 : Fragment(), ProximityControl.ProximityListener, WifiObservable.WifiStateListener {
    var PERMISSION_REQUEST_CODE by Delegates.notNull<Int>()

    private lateinit var wifiManager: WifiManager
    private lateinit var cameraManager: CameraManager
    private lateinit var nfc: NfcObservable
    private lateinit var audioManager: AudioManager
    private lateinit var vibrationModeObservable: VibrationModeObservable
    private lateinit var proximityControl: ProximityControl
    private lateinit var wifiObservable: WifiObservable
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var mobileInternetChecker: MobileInternetObservable


    val helper = Helper()


    private var _binding: Fragment1Binding? = null
    val binding get() = _binding!!

    lateinit var viewModel: ViewModel1




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

        }
        viewModel.simCardStatus(requireContext())

        nfc()
        setFlash()
        proximity()
        vibrate()
        network()

        wifiObservable = WifiObservable(requireContext())

    }

    fun observeData() {
        ViewModel1.bluetoothLiveData.observe(viewLifecycleOwner) {
            binding.switchBluetooth.isChecked = it
        }

        ViewModel1.liveDataSim.observe(viewLifecycleOwner) {
            binding.checkBoxSim.isChecked = it
        }

        ViewModel1.chargingSocket.observe(viewLifecycleOwner) {
            binding.checkBoxCharging.isChecked = it
        }

    }


    override fun onResume() {
        super.onResume()
        registerReceivers()


    }


    override fun onPause() {
        super.onPause()
        unregisterReceivers()

    }

    fun registerReceivers(){
        helper.register(IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED), ViewModel1.bluetoothReceiver, requireActivity())
        helper.register(IntentFilter(Intent.ACTION_BATTERY_CHANGED), ViewModel1.chargingSocketReceiver, requireActivity())
        helper.register(IntentFilter(Intent.ACTION_HEADSET_PLUG), ViewModel1.headphoneReceiver, requireActivity())

        nfc.startObserving()
        proximityControl.start()
        wifiObservable.startMonitoring(this)
        vibrationModeObservable.startObserving()


    }

    fun unregisterReceivers(){
       ViewModel1.apply {
           helper.unregister(bluetoothReceiver, requireActivity())
           helper.unregister(chargingSocketReceiver, requireActivity())
           helper.unregister(headphoneReceiver, requireActivity())
       }

        nfc.stopObserving()
        proximityControl.stop()
        wifiObservable.stopMonitoring()
        vibrationModeObservable.stopObserving()
    }




    @RequiresApi(Build.VERSION_CODES.S)
    private fun bluetooth() {
        binding.switchBluetooth.setOnCheckedChangeListener { _, isChecked ->

            requestPermission(requireContext().has(Manifest.permission.BLUETOOTH_CONNECT), Manifest.permission.BLUETOOTH_CONNECT)
            if (!requireContext().has(Manifest.permission.BLUETOOTH_CONNECT)) {
                binding.switchBluetooth.isChecked = false
            }
            else{
                if (requireContext().has(Manifest.permission.BLUETOOTH_CONNECT) && isChecked) {
                    viewModel.enableBluetooth()

                } else if (requireContext().has(Manifest.permission.BLUETOOTH_CONNECT) && !isChecked) {
                    viewModel.disableBluetooth()
                }
            }


        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun flash() {
        binding.checkBoxFlash.setOnCheckedChangeListener { buttonView, isChecked ->
            requestPermission(requireContext().has(Manifest.permission.CAMERA), Manifest.permission.CAMERA)
            if(requireContext().has(Manifest.permission.CAMERA)){
                if (isChecked) {
                    turnOnFlash()
                } else turnOfFlash()
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
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            } else {
                Toast.makeText(requireContext(), "NFC is already enabled", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
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
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            } else {
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


    private fun vibrate() {

        vibrationModeObservable.startObserving()

        val vibrationModeChangeListener: (Boolean) -> Unit = { isInVibrationMode ->
            binding.checkBoxTitresim.isChecked = isInVibrationMode
        }

        vibrationModeObservable.setOnVibrationModeChangeListener(vibrationModeChangeListener)

        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager


        binding.checkBoxTitresim.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            } else {
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

    private fun turnOfFlash() {
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
    private fun requestPermission(hasPermission: Boolean, vararg permission: String) {
        var permissionsToRequest = mutableListOf<String>(*permission)
        if (!hasPermission) {
            for (permissio in permission) {
                permissionsToRequest.add(permissio)
            }
        }

        if (hasPermission == context?.has(Manifest.permission.BLUETOOTH_CONNECT)) PERMISSION_REQUEST_CODE = 2
        else if (hasPermission == context?.has(Manifest.permission.CHANGE_WIFI_STATE)) PERMISSION_REQUEST_CODE = 1
        else if(hasPermission == context?.has(Manifest.permission.MODIFY_AUDIO_SETTINGS)) PERMISSION_REQUEST_CODE = 4
        else if(hasPermission == context?.has(Manifest.permission.CAMERA)) PERMISSION_REQUEST_CODE =5

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

