package com.example.project1

import CameraObservable
import NetworkStateObserver
import NfcEnabledObservable
import ProximityControl
import VibrationModeObservable
import WifiConnectionObservable
import android.bluetooth.BluetoothAdapter
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.project1.databinding.Fragment1Binding
import com.example.project1import.WifiConnectionStatusListener
import java.lang.reflect.Method
import kotlin.properties.Delegates




@Suppress("DEPRECATION")
class Fragment1 : Fragment(), ProximityControl.ProximityListener,  WifiObservable.WifiStateListener {
    var PERMISSION_REQUEST_CODE by Delegates.notNull<Int>()

    lateinit var wifiManager: WifiManager

    private lateinit var cameraManager: CameraManager
    private lateinit var nfc: NfcEnabledObservable
    private lateinit var networkStateObserver: NetworkStateObserver
    private lateinit var audioManager: AudioManager
    private lateinit var vibrationModeObservable: VibrationModeObservable
    private lateinit var proximityControl: ProximityControl
    private lateinit var wifiObservable: WifiObservable


    private var camera: android.hardware.Camera? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var cameraId = -1



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
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
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
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this)[ViewModel1::class.java]
        wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        networkStateObserver = NetworkStateObserver(requireContext())
        nfc = NfcEnabledObservable(requireContext())
        vibrationModeObservable = VibrationModeObservable(requireContext())






        _binding = Fragment1Binding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        observeData()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetooth()
            //wifi()
            flash()
        }
        viewModel.simCardStatus(requireContext())


        nfc()
        vibrate()
        setFlash()

        proximity()

        wifiObservable = WifiObservable(requireContext())




        binding.checkboxWifi.setOnCheckedChangeListener { _ , isChecked ->
            if(isChecked){
                enableWifi()
            }

            else {
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


    fun observeData(){
        bluetoothLiveData.observe(viewLifecycleOwner){
            binding.switchBluetooth.isChecked = it
        }

        ViewModel1.liveDataSim.observe(viewLifecycleOwner){
            binding.checkBoxSim.isChecked = it
        }

        ViewModel1.chargingSocket.observe(viewLifecycleOwner){
            binding.checkBoxCharging.isChecked = it
        }

        networkStatus.observe(viewLifecycleOwner){
            binding.checkbox3G.isChecked = it
        }

        nfc.liveData().observe(viewLifecycleOwner){
            binding.checkBoxNfc.isChecked = it

        }

        val vibrationModeChangeListener: (Boolean) -> Unit = { isInVibrationMode ->
            binding.checkBoxTitresim.isChecked = isInVibrationMode
        }

        vibrationModeObservable.setOnVibrationModeChangeListener(vibrationModeChangeListener)


    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()
        registerBatteryStatusReceiver()
        registerBluetoothStatusReceiver()
        //registerWifiStatusReceiver()
        registerHeadphoneStatusReceiver()
        nfc.startObserving()
        vibrationModeObservable.startObserving()
        proximityControl.start()
        wifiObservable.startMonitoring(this)
    }






    override fun onPause() {
        super.onPause()
        unregisterBatteryStatusReceiver()
        unregisterBluetoothStatusReceiver()
     //   unregisterWifiStatusReceiver()
        unregisterHeadphoneStatusRegister()
        nfc.stopObserving()
        proximityControl.stop()
        wifiObservable.stopMonitoring()

    }
    private fun registerBluetoothStatusReceiver(){
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        requireActivity().registerReceiver(bluetoothReceiver, filter)
    }

    private fun registerBatteryStatusReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        requireActivity().registerReceiver(ViewModel1.chargingSocketReceiver, filter)
    }

    private fun registerWifiStatusReceiver(){
        val filter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        //requireActivity().registerReceiver(wifiReceiver, filter)
    }


    /*private fun registerNetworkStatusReceiver(){
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireActivity().registerReceiver(networkReceiver, intentFilter)
    }*/

    private fun registerHeadphoneStatusReceiver(){
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        requireActivity().registerReceiver(ViewModel1.headphoneReceiver, filter)
    }

    private fun unregisterBluetoothStatusReceiver(){
        requireActivity().unregisterReceiver(bluetoothReceiver)
    }
    private fun unregisterBatteryStatusReceiver() {
        requireActivity().unregisterReceiver(ViewModel1.chargingSocketReceiver)
    }
    private fun unregisterWifiStatusReceiver(){
        //requireActivity().unregisterReceiver(wifiReceiver)
    }
    /*private fun unregisterNetworkStatusReceiver(){
        requireActivity().unregisterReceiver(networkReceiver)
    }*/
    private fun unregisterHeadphoneStatusRegister(){
        requireActivity().unregisterReceiver(ViewModel1.headphoneReceiver)
    }




    @RequiresApi(Build.VERSION_CODES.S)
    private fun bluetooth(){
        binding.switchBluetooth.setOnCheckedChangeListener { _ , isChecked ->

            requestPermission(hasBluetoothPermission(),Manifest.permission.BLUETOOTH_CONNECT)
            if(!hasBluetoothPermission()){
                binding.switchBluetooth.isChecked = false
            }

            if(hasBluetoothPermission() && isChecked){
                viewModel.enableBluetooth()

            }

            else if(hasBluetoothPermission() && !isChecked){
                viewModel.disableBluetooth()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun wifi(){




    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun flash(){

        binding.checkBoxFlash.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                turnOnFlash()
            }
            else turnOffFlash()
        }

    }

    private fun setFlash(){
        val flashlightMonitor = FlashlightStateMonitor(requireContext(), binding.checkBoxFlash)
        flashlightMonitor.startMonitoringFlashlightState()
    }


    private fun nfc(){
        binding.checkBoxNfc.setOnCheckedChangeListener { buttonView, isChecked ->
            goToNfcSettings()
        }
    }

    private fun vibrate(){
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        binding.checkBoxTitresim.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Change device mode to vibration
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            } else {
                // Change device mode to normal
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        }
    }
    private fun proximity(){
        proximityControl = ProximityControl(requireContext())
        proximityControl.setProximityListener(this)
    }

    /*private fun proximity(){
        val proximityObserver = object : ProximitySensorObservable.Observer {
            override fun onProximityStateChanged(proximityEnabled: Boolean) {
                // Handle proximity state change here
                binding.checkboxProxy.isChecked = proximityEnabled
            }
        }

        proximitySensorObservable.addObserver(proximityObserver)
        proximitySensorObservable.startListening()
    }*/






    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasBluetoothPermission(): Boolean =
        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED

    private fun hasWifiPermission() =
            (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)




    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermission(hasPermission: Boolean,vararg permission: String){
        var permissionsToRequest = mutableListOf<String>(*permission)
        if(!hasPermission){
            for(permissio in permission){
                permissionsToRequest.add(permissio)
            }
        }

        if(hasPermission == hasBluetoothPermission()){
            PERMISSION_REQUEST_CODE = 2
        }

        else if(hasPermission == hasWifiPermission()){
            PERMISSION_REQUEST_CODE = 1
        }


        if(permissionsToRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(requireActivity(), permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }



    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val a = PERMISSION_REQUEST_CODE
        if(requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()){
            for(i in grantResults.indices){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(requireContext(), "Permission Granted.", Toast.LENGTH_LONG).show()

                }
            }
        }




    }

    private fun turnOnFlash() {

        try {
            val cameraId = cameraManager.cameraIdList[0] // Assuming the first camera has a flash
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

    private fun goToNfcSettings() {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
        startActivity(intent)
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


}
