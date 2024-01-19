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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import com.google.android.gms.location.*
import com.google.gson.Gson
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.sample.MainActivity.Companion.REFRESH_RATIO
import cz.mroczis.netmonster.sample.databinding.ActivityMainBinding
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.lang.reflect.Method
import kotlin.random.Random
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONArray
import java.time.LocalDateTime

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




    private val brokerUri = "tcp://broker.hivemq.com:1883"
    private val clientId = "publish-${Random.nextInt(0, 1000)}"
    private val persistence = MemoryPersistence()
    private val mqttClient = MqttClient(brokerUri, clientId, persistence)
  
    private val operation = "local"

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


    private fun device_removed_action_message(){

        val actionDetails= JSONObject()
        actionDetails.put("receiver","${getSystemDetail()}")
        actionDetails.put("received_signals",ArrayList<String>())
        actionDetails.put("status","DEVICE_ABORTED")
        publishMqttMessage(actionDetails.toString().toByteArray(), "dt/message/action")
        println("aborted")

    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        device_removed_action_message()
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
        handler.postDelayed(REFRESH_RATIO) { loop() }
    }


    @SuppressLint("HardwareIds")
    private fun getSystemDetail(): String {
        val result = JSONObject().apply {
            put("Model", Build.MODEL)
            put("BuildID", Build.ID)
            put("receiverID", Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
            put("Manufacture", Build.MANUFACTURER)
            put("Brand", Build.BRAND)
        }
        return result.toString()
    }



    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("MissingPermission")
    private fun updateCellularData() {


        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }


        NetMonsterFactory.get(this).apply {
            val subset: List<ICell> = getCells()

            val gson = Gson()
            val mergedJson = gson.toJson(subset)


            val cellDetails = JSONObject().apply {
                put("receiver", getSystemDetail())
                put("received_signals", mergedJson)
                put("ingestor", current)
            }


            publishMqttMessage(cellDetails.toString().toByteArray(), "dt/message/cell")


            adapter.data = subset

        }
    }


    fun publishMqttMessage(data: ByteArray, topic: String) {
        val mqttMessage = MqttMessage(data)
        mqttMessage.qos = 0
        mqttMessage.isRetained = false
        mqttClient.publish(topic.toString(), mqttMessage)
    }


    fun formatSSID(input: String): String {
        if (input.isEmpty()) {
            return ""
        }
        var result = input

        if (input.first() == '"' && input.last() == '"') {
            result = result.substring(1, result.length - 1)
        }

        return result
    }




    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("MissingPermission")
    private fun updateWifiData() {
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

            val wifiInfo = wifiManager.scanResults
            val connectedWifi = getConnectedWifiSSID(context)

            val wifiDetails = JSONObject()
            wifiDetails.put("receiver", getSystemDetail().toString())

            val storage = JSONArray()

            for (scanResult in wifiInfo) {
                val tempObject = JSONObject()
                val signalDetail = JSONObject()
                val speedDetail = JSONObject()
                val frequencyDetail = JSONObject()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    tempObject.put("SSID", formatSSID(scanResult.wifiSsid.toString()))
                }

                tempObject.put("BSSID", scanResult.BSSID)
                tempObject.put("capabilities", scanResult.capabilities)

                signalDetail.put("RSSI", scanResult.level.toInt())
                frequencyDetail.put("Frequency", scanResult.frequency.toInt())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    speedDetail.put("channel_width", scanResult.channelWidth)
                    frequencyDetail.put("centerFreq0", scanResult.centerFreq0.toInt())
                    frequencyDetail.put("centerFreq1", scanResult.centerFreq1.toInt())
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    tempObject.put("wifi_standard", scanResult.wifiStandard.toInt())
                }

                signalDetail.put("isconnected", "false")

                // Check if there is any connected WiFi network
                if (connectedWifi != null && connectedWifi.bssid == scanResult.BSSID) {
                    signalDetail.put("Link_speed", connectedWifi.linkSpeed)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        speedDetail.put(
                            "max_Supported_Tx_Link_Speed",
                            connectedWifi.maxSupportedTxLinkSpeedMbps
                        )
                        speedDetail.put(
                            "max_Supported_Rx_Link_Speed",
                            connectedWifi.maxSupportedRxLinkSpeedMbps
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        speedDetail.put("tx_link_speed", connectedWifi.txLinkSpeedMbps)
                        signalDetail.put("rx_link_speed", connectedWifi.rxLinkSpeedMbps)
                    }

                    signalDetail.put("RSSI", connectedWifi.rssi)
                    signalDetail.put("isconnected", "true")
                }

                tempObject.put("signal", signalDetail)
                tempObject.put("speed", speedDetail)
                tempObject.put("frequency", frequencyDetail)
                storage.put(tempObject)
            }


            val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now()
            } else {
                TODO("VERSION.SDK_INT < O")
            }

            wifiDetails.put("received_signals", storage)
            wifiDetails.put("ingestor", current)


        publishMqttMessage(wifiDetails.toString().toByteArray(), "dt/message/wifi")


    }



    //    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getConnectedWifiSSID(context: Context): WifiInfo? {

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        var data: WifiInfo? = null

        if (networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI) {
            val networkCapabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            } else {
                TODO("VERSION.SDK_INT < M")
            }

            if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                val wifiInfo = wifiManager.connectionInfo
                data=wifiInfo
            }
        }

        return data

    }


    private fun checkBluetoothDeviceType(int: Int): String {
        var strings="";
        if(int == 1076){
            strings="AUDIO_VIDEO_CAMCORDER"
        }else if (int == 1056){
            strings = "AUDIO_VIDEO_CAR_AUDIO"
        }else if (int == 1032){
            strings = "AUDIO_VIDEO_HANDSFREE"
        }else if (int == 1056){
            strings = "AUDIO_VIDEO_HEADPHONES"
        }else if (int == 1048){
            strings = "AUDIO_VIDEO_HIFI_AUDIO"
        }else if (int == 1064){
            strings = "AUDIO_VIDEO_LOUDSPEAKER"
        }else if (int == 1044){
            strings = "AUDIO_VIDEO_CAR_AUDIO"
        }else if (int == 1040){
            strings = "AUDIO_VIDEO_MICROPHONE"
        }else if (int == 1052){
            strings = "AUDIO_VIDEO_PORTABLE_AUDIO"
        }else if (int == 1060){
            strings = "AUDIO_VIDEO_SET_TOP_BOX"
        }else if (int == 1024){
            strings = "AUDIO_VIDEO_UNCATEGORIZED"
        }else if (int == 1068){
            strings = "AUDIO_VIDEO_VCR"
        }else if (int == 1072){
            strings = "AUDIO_VIDEO_VIDEO_CAMERA"
        }else if (int == 1088){
            strings = "AUDIO_VIDEO_VIDEO_CONFERENCING"
        }else if (int == 1084){
            strings = "AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER"
        }else if (int == 1096){
            strings = "AUDIO_VIDEO_VIDEO_GAMING_TOY"
        }else if (int == 1080){
            strings = "AUDIO_VIDEO_VIDEO_MONITOR"
        }else if (int == 1028){
            strings = "AUDIO_VIDEO_WEARABLE_HEADSET"
        }else if (int == 260){
            strings = "COMPUTER_DESKTOP"
        }else if (int == 272){
            strings = "COMPUTER_HANDHELD_PC_PDA"
        }else if (int == 268){
            strings = "COMPUTER_LAPTOP"
        }else if (int == 276){
            strings = "COMPUTER_PALM_SIZE_PC_PDA"
        }else if (int == 264){
            strings = "COMPUTER_SERVER"
        }else if (int == 256){
            strings = "COMPUTER_UNCATEGORIZED"
        }else if (int == 280){
            strings = "COMPUTER_WEARABLE"
        }else if (int == 2308){
            strings = "HEALTH_BLOOD_PRESSURE"
        }else if (int == 2332){
            strings = "HEALTH_DATA_DISPLAY"
        }else if (int == 2320){
            strings = "HEALTH_GLUCOSE"
        }else if (int == 2324){
            strings = "HEALTH_PULSE_OXIMETER"
        }else if (int == 2328){
            strings = "HEALTH_PULSE_RATE"
        }else if (int == 2312){
            strings = "HEALTH_THERMOMETER"
        }else if (int == 2304){
            strings = "HEALTH_UNCATEGORIZED"
        }else if (int == 2316){
            strings = "HEALTH_WEIGHING"
        }else if (int == 1344){
            strings = "PERIPHERAL_KEYBOARD"
        }else if (int == 1472){
            strings = "PERIPHERAL_KEYBOARD_POINTING"
        }else if (int == 1280){
            strings = "PERIPHERAL_NON_KEYBOARD_NON_POINTING"
        }else if (int == 1408){
            strings = "PERIPHERAL_POINTING"
        }else if (int == 516){
            strings = "PHONE_CELLULAR"
        }else if (int == 520){
            strings = "PHONE_CORDLESS"
        }else if (int == 532){
            strings = "PHONE_ISDN"
        }else if (int == 528){
            strings = "PHONE_MODEM_OR_GATEWAY"
        }else if (int == 524){
            strings = "PHONE_SMART"
        }else if (int == 512){
            strings = "PHONE_UNCATEGORIZED"
        }else if (int == 2064){
            strings = "TOY_CONTROLLER"
        }else if (int == 2060){
            strings = "TOY_DOLL_ACTION_FIGURE"
        }else if (int == 2068){
            strings = "TOY_GAME"
        }else if (int == 2052){
            strings = "TOY_ROBOT"
        }else if (int == 2048){
            strings = "TOY_UNCATEGORIZED"
        }else if (int == 2056){
            strings = "TOY_VEHICLE"
        }else if (int == 1812){
            strings = "WEARABLE_GLASSES"
        }else if (int == 1808){
            strings = "WEARABLE_HELMET"
        }else if (int == 1804){
            strings = "WEARABLE_JACKET"
        }else if (int == 1800){
            strings = "WEARABLE_UNCATEGORIZED"
        }else if (int == 1796){
            strings = "WEARABLE_WRIST_WATCH"
        }else{
            strings = "unknown"
        }

        return strings;
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
                            val typeint = device.bluetoothClass.deviceClass


                            val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                            val connection = isConnected(device)


                            if(deviceName != null){
                                tempDetails.put("Name", "${deviceName}")
                                tempDetails.put("MAC", "${deviceAddress}")
                                tempDetails.put("Type", "${checkBluetoothDeviceType(typeint)}")

                                signalDetail.put("RSSI", rssi.toInt())



                                val conn_status = connection.toString()

                                signalDetail.put("isconnected", "${conn_status}")


                                tempDetails.put("signal", signalDetail)

                                storage.add(tempDetails.toString())


                            }
                        }
                    }
                }
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                    bluetoothDetails.put("received_signals", storage.distinct().toString())
                    bluetoothDetails.put("receiver","${getSystemDetail()}")

                    val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDateTime.now()
                    } else {
                        TODO("VERSION.SDK_INT < O")
                    }
                    bluetoothDetails.put("ingestor", current)

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
}








