package com.example.project1

import android.bluetooth.BluetoothAdapter
import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.project1.databinding.Fragment1Binding
import kotlin.properties.Delegates


@Suppress("DEPRECATION")
class Fragment1 : Fragment() {
    var PERMISSION_REQUEST_CODE by Delegates.notNull<Int>()

    //lateinit var wifiManager: WifiManager

    private lateinit var cameraManager: CameraManager

    private val bluetoothLiveData = MutableLiveData<Boolean>()
    private val wifiLiveData = MutableLiveData<Boolean>()
    private val networkStatus = MutableLiveData<Boolean>()
    private lateinit var networkLiveData: NetworkLiveData






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

    private val wifiReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if(action != null){
                if (WifiManager.WIFI_STATE_CHANGED_ACTION == action) {
                    val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED)

                    when (wifiState) {
                        WifiManager.WIFI_STATE_ENABLED -> {
                            wifiLiveData.value = true
                        }
                        WifiManager.WIFI_STATE_DISABLED -> {
                            wifiLiveData.value = false
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
        cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager


        _binding = Fragment1Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeData()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetooth()
            wifi()
            //nfc()
            vibrate()

        }
        //nfc()

        viewModel.simCardStatus(requireContext())

        val flashlightMonitor = FlashlightStateMonitor(requireContext(), binding.checkBoxFlash)

        flashlightMonitor.startMonitoringFlashlightState()

        flash()




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
        wifiLiveData.observe(viewLifecycleOwner){
            binding.switchWifi.isChecked = it
        }

        networkStatus.observe(viewLifecycleOwner){
            binding.checkbox3G.isChecked = it
        }

        ViewModel1.nfc.observe(viewLifecycleOwner){
            binding.checkBoxNfc.isChecked = it
        }

        ViewModel1.vibration.observe(viewLifecycleOwner){
            binding.checkBoxTitresim.isChecked = it
        }



    }
    private fun enableWifi() {
       val wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
    }

    private fun disableWifi() {
       val  wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = false
        }
    }


    override fun onResume() {
        super.onResume()
        registerBatteryStatusReceiver()
        registerBluetoothStatusReceiver()
        registerWifiStatusReceiver()
        //registerNetworkStatusReceiver()
        registerHeadphoneStatusReceiver()
        registerNfcStatusReceiver()
        registerVibrationStatusReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterBatteryStatusReceiver()
        unregisterBluetoothStatusReceiver()
        unregisterWifiStatusReceiver()
        //unregisterNetworkStatusReceiver()
        unregisterHeadphoneStatusRegister()
        unregisterNfcStatusReceiver()
        unregisterVibrationStatusReceiver()
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
        requireActivity().registerReceiver(wifiReceiver, filter)
    }


    /*private fun registerNetworkStatusReceiver(){
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireActivity().registerReceiver(networkReceiver, intentFilter)
    }*/

    private fun registerHeadphoneStatusReceiver(){
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        requireActivity().registerReceiver(ViewModel1.headphoneReceiver, filter)
    }

    private fun registerNfcStatusReceiver(){
        val filter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        requireActivity().registerReceiver(ViewModel1.nfcReceiver, filter)
    }

    private fun registerVibrationStatusReceiver(){
        val filter = IntentFilter(Context.VIBRATOR_MANAGER_SERVICE)
        requireActivity().registerReceiver(ViewModel1.vibrationReceiver, filter)
    }

    /*private fun registerSimCardStatusReceiver(){
        val intentFilter = IntentFilter(TelephonyManager.EXTRA_STATE)
        requireActivity().registerReceiver(ViewModel1.simCardReceiver, intentFilter)
    }*/

    private fun unregisterBluetoothStatusReceiver(){
        requireActivity().unregisterReceiver(bluetoothReceiver)
    }
    private fun unregisterBatteryStatusReceiver() {
        requireActivity().unregisterReceiver(ViewModel1.chargingSocketReceiver)
    }
    private fun unregisterWifiStatusReceiver(){
        requireActivity().unregisterReceiver(wifiReceiver)
    }
    /*private fun unregisterNetworkStatusReceiver(){
        requireActivity().unregisterReceiver(networkReceiver)
    }*/
    private fun unregisterHeadphoneStatusRegister(){
        requireActivity().unregisterReceiver(ViewModel1.headphoneReceiver)
    }
    /*private fun unregisterSimCardStatusRegister(){
        requireActivity().unregisterReceiver(ViewModel1.simCardReceiver)
    }*/

    private fun unregisterNfcStatusReceiver(){
        requireContext().unregisterReceiver(ViewModel1.nfcReceiver)
        ViewModel1.nfc.removeObservers(viewLifecycleOwner)
    }

    private fun unregisterVibrationStatusReceiver(){
        requireActivity().unregisterReceiver(ViewModel1.nfcReceiver)
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
        binding.switchWifi.setOnCheckedChangeListener{ b , isChecked ->
            if(isChecked){
                enableWifi()
            }
            else{
                disableWifi()
            }

        }

    }

    private fun flash(){

        binding.checkBoxFlash.setOnCheckedChangeListener { buttonView, isChecked ->

            if(isChecked){
                turnOnFlash()
            }
            else turnOffFlash()
        }


    }

    private fun nfc(){
        var adapter = NfcAdapter.getDefaultAdapter(this.context)
        /*binding.checkBoxNfc.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){

                Toast.makeText(context, "NFC is available.",Toast.LENGTH_SHORT).show()
            }
            else{
                //val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                //startActivity(intent)
                !adapter.isEnabled

                Toast.makeText(context, "NFC is unavailable.",Toast.LENGTH_SHORT).show()

            }
        }*/

        if(adapter !=null){
            if(adapter.isEnabled) binding.checkBoxNfc.isChecked = true
        }
            else binding.checkBoxNfc.isChecked = true
    }

    private fun vibrate(){
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        binding.checkBoxTitresim.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                vibrator.vibrate(1000)
            }

        }
    }






    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasBluetoothPermission(): Boolean =
        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED

    private fun hasWifiPermission(): Boolean{
        val permissions = arrayOf(Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE)
        var i = 0
        for(permission in permissions){
            if(ActivityCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED)
                i++
            if(i == 2) break
        }

        return i == 2
    }



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











}
