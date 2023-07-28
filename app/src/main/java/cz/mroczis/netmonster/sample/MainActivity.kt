package cz.mroczis.netmonster.sample

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import com.google.android.gms.location.*
import com.google.gson.Gson
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.feature.merge.CellSource
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.sample.MainActivity.Companion.REFRESH_RATIO
import cz.mroczis.netmonster.sample.databinding.ActivityMainBinding
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.lang.reflect.Method
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random


/**
 * Activity periodically updates data (once in [REFRESH_RATIO] ms) when it's on foreground.
 */


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private var context: Context = this
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val REFRESH_RATIO = 5_000L
    }



    private val handler = Handler(Looper.getMainLooper())
    private val adapter = MainAdapter()

    private lateinit var binding: ActivityMainBinding
    private lateinit var wifiManager: WifiManager


    //    val brokerUri = "tcp://10.0.2.2:1883" // Replace with your MQTT broker URI
    private val brokerUri = "tcp://broker.hivemq.com:1883" //
    private val clientId = "publish-${Random.nextInt(0, 1000)}"
    private val persistence = MemoryPersistence()
    private val mqttClient = MqttClient(brokerUri, clientId, persistence)
    val LOCATION_PERMISSION_REQUEST_CODE = 1001




    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            recycler.adapter = adapter
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


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
        requestLocationUpdates()
        updateCellularData()
        updateWifiData()
        handler.postDelayed(REFRESH_RATIO) { loop() }
    }

    @SuppressLint("HardwareIds")
    private fun getSystemDetail(): String {
        val deviceDetail= JSONObject()

        deviceDetail.put("Model","${Build.MODEL}")
        deviceDetail.put("BuildID","${Build.ID}")
        deviceDetail.put("AndroidID","${
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }")
        deviceDetail.put("Manufacture","${Build.MANUFACTURER}")
        deviceDetail.put("Brand","${Build.BRAND}")

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val locationData = sharedPreferences.getString("locationData", "")
        deviceDetail.put("locationData","${locationData}")


        println(deviceDetail)
        return deviceDetail.toString()
    }


   


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("MissingPermission")
    private fun updateCellularData() {

        NetMonsterFactory.get(this).apply {
            val subset : List<ICell> = getCells(
                // subset of available sources
                CellSource.ALL_CELL_INFO,

                CellSource.CELL_LOCATION,

                )

            adapter.data = subset
            val separated = " \n${subset.joinToString(separator = "\n")}"
            Log.d("NTM-RES", separated)



            //this is testing
            val gson = Gson()
            val mergedJson = gson.toJson(subset)
            val cellDetails = JSONObject()
            cellDetails.put("Receiver","${getSystemDetail()}")
            cellDetails.put("received_signals","${mergedJson}")

            publishMqttMessage(cellDetails.toString().toByteArray(), "dt/message/cell")



        }

    }


    fun publishMqttMessage(data: ByteArray, topic: String) {
        val mqttMessage = MqttMessage(data)
        mqttMessage.qos = 0
        mqttMessage.isRetained = false
        mqttClient.publish(topic.toString(), mqttMessage)
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("MissingPermission")
    private fun updateWifiData(){

        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.scanResults
        val connectedWifi = getConnectedWifiSSID(context)

        val wifiDetails= JSONObject()
        val storage = ArrayList<String>()
        wifiDetails.put("Receiver","${getSystemDetail()}")

        for (scanResult in wifiInfo) {
            val tempObject = JSONObject()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                tempObject.put("SSID","${scanResult.wifiSsid}")
            }
            tempObject.put("BSSID","${scanResult.BSSID}")
            tempObject.put("capabilities","${scanResult.capabilities}")
            tempObject.put("Level","${scanResult.level}")
            tempObject.put("Frequency","${scanResult.frequency}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tempObject.put("Frequency","${scanResult.channelWidth}")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tempObject.put("centerFreq0","${scanResult.centerFreq0}")
                val distance = calculateDistance(scanResult.level, scanResult.centerFreq0)
                tempObject.put("Distance", "${distance}")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tempObject.put("centerFreq1","${scanResult.centerFreq1}")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                tempObject.put("WifiStandard","${scanResult.wifiStandard}")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tempObject.put("mcResponder80211","${scanResult.is80211mcResponder}")
            }

            storage.add(tempObject.toString())

        }
        wifiDetails.put("received_signals","${storage}")
        wifiDetails.put("primary","${connectedWifi}")

        print(wifiDetails)

        publishMqttMessage(wifiDetails.toString().toByteArray(), "dt/message/wifi")

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getConnectedWifiSSID(context: Context): String {

        val connectedWifiDetails= JSONObject()
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

                connectedWifiDetails.put("SSID","${wifiInfo.ssid}")
                connectedWifiDetails.put("BSSID","${wifiInfo.bssid}")
                connectedWifiDetails.put("RSSI","${wifiInfo.rssi}")
                connectedWifiDetails.put("Link_speed","${wifiInfo.linkSpeed}")
                connectedWifiDetails.put("Frequency", "${wifiInfo.frequency}")

                val distance = calculateDistance(wifiInfo.rssi, wifiInfo.frequency)
                connectedWifiDetails.put("Distance", "${distance}")

                connectedWifiDetails.put("hidden_SSID","${wifiInfo.hiddenSSID}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    connectedWifiDetails.put("security_type","${wifiInfo.currentSecurityType}")
                }
                connectedWifiDetails.put("supplicant_state","${wifiInfo.supplicantState}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    connectedWifiDetails.put("wifi_standard","${wifiInfo.wifiStandard}")
                    connectedWifiDetails.put("max_Supported_Tx_Link_Speed","${wifiInfo.maxSupportedTxLinkSpeedMbps}")
                    connectedWifiDetails.put("max_Supported_Rx_Link_Speed","${wifiInfo.maxSupportedRxLinkSpeedMbps}")
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    connectedWifiDetails.put("tx_link_speed", "${wifiInfo.txLinkSpeedMbps}")
                    connectedWifiDetails.put("rx_link_speed", "${wifiInfo.rxLinkSpeedMbps}")
                }
                connectedWifiDetails.put("netID", "${wifiInfo.networkId}")
                connectedWifiDetails.put("isConnected", "true")


            }
        }
        return connectedWifiDetails.toString()
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun scanForDevices(): String {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        // Register a BroadcastReceiver to listen for Bluetooth device discovery

        val storage = ArrayList<String>()
        val bluetoothDetails= JSONObject()

        val receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    // A Bluetooth device is found

                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {

                        val tempDetails = JSONObject()
                        val signalDetail = JSONObject()


                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            val deviceName = device.name
                            val deviceAddress = device.address


                            val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                            val connection = isConnected(device)

                            if(deviceName != null){
                                tempDetails.put("Name", "${deviceName}")
                                tempDetails.put("MAC", "${deviceAddress}")
                                signalDetail.put("RSSI", rssi.toInt())


                                val conn_status = connection.toString()

                                signalDetail.put("isconnected", "${conn_status}")



                                val distance = calculateDistancewithOnlyRssi(rssi.toInt())
                                signalDetail.put("Distance", distance.toFloat())
                                tempDetails.put("signal", signalDetail)

                                storage.add(tempDetails.toString())


                            }
                        }
                    }
                }
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                    bluetoothDetails.put("received_signals", storage.distinct().toString())
                    bluetoothDetails.put("Receiver","${getSystemDetail()}")

                    println("bluetooth")
                    println(bluetoothDetails)

                    publishMqttMessage(bluetoothDetails.toString().toByteArray(), "dt/message/bluetooth")
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


    private fun calculateDistance(signalLevelInDb: Int, freqInMHz: Int): Double {
        val exp = (27.55 - (20 * log10(freqInMHz.toDouble())) + abs(signalLevelInDb)) / 20.0
        val distance = 10.0.pow(exp)
        return "%.2f".format(distance).toDouble()
    }

    private fun calculateDistancewithOnlyRssi(rssi: Int): Double {
        val distance = 10.0.pow((-69 - rssi) / (10 * 2))
        return "%.2f".format(distance).toDouble()
    }


    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationDetails = JSONObject()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations, this can be null.
                location?.let {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    locationDetails.put("latitide", "${location.latitude}")
                    locationDetails.put("longitude", "${location.longitude}")



                    val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("locationData", locationDetails.toString())
                    editor.apply()

                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, start location updates
                requestLocationUpdates()
            } else {
                // Location permission denied. Handle accordingly.
            }
        }
    }


}





