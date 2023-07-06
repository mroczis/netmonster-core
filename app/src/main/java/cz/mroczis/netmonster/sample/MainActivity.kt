package cz.mroczis.netmonster.sample

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import com.google.gson.Gson
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.sample.MainActivity.Companion.REFRESH_RATIO
import cz.mroczis.netmonster.sample.databinding.ActivityMainBinding
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.lang.reflect.Method
import kotlin.random.Random
import android.net.wifi.WifiInfo
import java.io.DataInput

/**
 * Activity periodically updates data (once in [REFRESH_RATIO] ms) when it's on foreground.
 */


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    var context: Context = this
    companion object {
        private const val REFRESH_RATIO = 5_000L
    }


    private val handler = Handler(Looper.getMainLooper())
    private val adapter = MainAdapter()

    private lateinit var binding: ActivityMainBinding
    private lateinit var wifiManager: WifiManager
    private lateinit var receiver: BroadcastReceiver




//    val brokerUri = "tcp://10.0.2.2:1883" // Replace with your MQTT broker URI
    private val brokerUri = "tcp://broker.hivemq.com:1883" //
    private val clientId = "publish-${Random.nextInt(0, 1000)}"
    private val persistence = MemoryPersistence()
    private val mqttClient = MqttClient(brokerUri, clientId, persistence)



    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            recycler.adapter = adapter
        }
        scanForDevices()




    }

    private fun connectToMqttBroker() {
        try {
            val mqttOptions = MqttConnectOptions()
            mqttOptions.isCleanSession = true
            mqttClient.connect(mqttOptions)
            println("connected")
            println("connected")
            println("connected")
            println("connected")
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            connectToMqttBroker()
            loop()



        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            ), 0)


        }



    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        disconnectFromMqttBroker()

    }

    private fun disconnectFromMqttBroker() {
        try {
            mqttClient.disconnect()
            println("Mqtt Disconnected")
        } catch (e: MqttException) {
            e.printStackTrace()
            println("Problem to discconnect")
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun loop() {
        updateCellularData()
        updateWifiData()
//        updateBluetoothData()
        handler.postDelayed(REFRESH_RATIO) { loop() }
    }




    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("MissingPermission")
    private fun updateCellularData() {

        NetMonsterFactory.get(this).apply {
            val merged = getCells()
            adapter.data = merged

            val gson = Gson()


            val separated = " \n${merged.joinToString(separator = "\n")}"
            val mergedJson = gson.toJson(separated)
            println("--------------------------------------------------------")
            Log.d("NTM-RES", separated)
            println("--------------------------------------------------------")
            publishMqttMessage(separated.toByteArray())
        }

    }


    fun publishMqttMessage(data: ByteArray) {
        val mqttMessage = MqttMessage(data)
        mqttMessage.qos = 0
        mqttMessage.isRetained = false
        mqttClient.publish("dt/message", mqttMessage)
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("MissingPermission")
    private fun updateWifiData(){
        val storage = ArrayList<String>()
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.scanResults
        val connectedWifi = getConnectedWifiSSID(context)



        for (scanResult in wifiInfo) {
            val temp = ArrayList<String>()
            temp.add(scanResult.toString())
            storage.add(temp.toString())
        }

        storage.add(connectedWifi.toString())
        println(storage)

        publishMqttMessage(storage.toString().toByteArray())

    }


    data class WifiList(
        val wifiInfo: WifiInfo,
        val isConnected: Boolean = true
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getConnectedWifiSSID(context: Context): ArrayList<String> {
        val storage = ArrayList<String>()
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI) {
            val networkCapabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            } else {
                TODO("VERSION.SDK_INT < M")
            }

            if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                val wifiInfo = wifiManager.connectionInfo
                val customWifiInfo = WifiList(wifiInfo)
                storage.add(customWifiInfo.toString())

            }
        }
        return storage
    }



    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("MissingPermission")
    private fun updateBluetoothData(){
//        getPairedBluetoothDevice(context)
//        scanForDevices()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getPairedBluetoothDevice(context: Context): Nothing? {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val deviceList: MutableList<BluetoothDevice> = mutableListOf()
        var retunedData = null

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.let {
                for (device: BluetoothDevice in it) {
                    val deviceName = device.name
                    val macAddress = device.address

                    val aliasing = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        device.alias
                    } else {
                        null
                    }



                    // Get RSSI
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val scanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner
                        val scanCallback: ScanCallback = object : ScanCallback() {
                            override fun onScanResult(callbackType: Int, result: ScanResult) {
                                if (result.device.address == device.address) {
                                    val rssi = result.rssi

                                    Log.i("pairedDevices", "paired device: $deviceName at $macAddress + $rssi dBM " + isConnected(device))
                                    // Handle the RSSI value here or save it to a variable
                                }else{
                                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                                    Log.i("pairedDevices", "paired device: $deviceName at $macAddress + $rssi dBM " + isConnected(device))
                                }

                            }
                        }

                        // Check for location permission
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            // Start scanning for devices
                            val scanFilters: MutableList<ScanFilter> = ArrayList()
                            val settings: ScanSettings = ScanSettings.Builder().build()
                            scanner?.startScan(scanFilters, settings, scanCallback)

                            // Stop scanning after a certain duration (e.g., 5 seconds)
                            val handler = Handler()
                            handler.postDelayed({
                                scanner?.stopScan(scanCallback)
                            }, REFRESH_RATIO)
                        } else {
                            // Request location permission


                        }
                    }
                }
            }
        }

    return retunedData
    }



    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun scanForDevices(): String {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        // Register a BroadcastReceiver to listen for Bluetooth device discovery
        val storage = ArrayList<String>()
        val receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    // A Bluetooth device is found

                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val tempStorage = ArrayList<String>()
                        val deviceName = if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            device.name

                        } else {
                            "Unknown"
                        }


                        val deviceAddress = device.address
                        val linkSpeed = device.bluetoothClass?.majorDeviceClass
                        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                        val connection = isConnected(device)

                        if(deviceName == null){
                            tempStorage.add("Unknown")
                        }else{
                            tempStorage.add(deviceName)
                        }

                        tempStorage.add(deviceAddress)
                        tempStorage.add(rssi.toString())
                        tempStorage.add(linkSpeed.toString())
                        tempStorage.add(connection.toString())
                        storage.add(tempStorage.toString())



                    }


                }



                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                    publishMqttMessage(storage.toString().toByteArray())
                    println(storage)
                    storage.clear()
                    scanForDevices()

                }

            }


        }



        // Register the BroadcastReceiver
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(receiver, filter)
        // Start Bluetooth device discovery
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
            == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothAdapter.startDiscovery()

        }

        return "None"

    }



    private fun isConnected(device: BluetoothDevice): Boolean {
        return try {
            val m: Method = device.javaClass.getMethod("isConnected")
            m.invoke(device) as Boolean
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }



}



