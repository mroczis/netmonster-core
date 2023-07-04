package cz.mroczis.netmonster.sample

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import kotlin.random.Random


/**
 * Activity periodically updates data (once in [REFRESH_RATIO] ms) when it's on foreground.
 */


class MainActivity : AppCompatActivity() {
    var context: Context = this
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            recycler.adapter = adapter
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)




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
        updateData()
        handler.postDelayed(REFRESH_RATIO) { loop() }
    }




    @SuppressLint("MissingPermission")
    private fun updateData() {

        NetMonsterFactory.get(this).apply {
            val merged = getCells()
            adapter.data = merged

            val gson = Gson()


            val separated = " \n${merged.joinToString(separator = "\n")}"
            val mergedJson = gson.toJson(separated)
            println("--------------------------------------------------------")
            Log.d("NTM-RES", separated)
            println("--------------------------------------------------------")

            val mqttMessage = MqttMessage(separated.toByteArray())
            mqttMessage.qos = 0
            mqttMessage.isRetained = false
            mqttClient.publish("dt/message", mqttMessage)
        }



//        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val wifiInfo: MutableList<ScanResult>? = wifiManager.scanResults
//        println("------------------------")
//        println(wifiInfo)


    }




    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("sdjskdhkjshdkjshdkjs")
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                println("Bluetooth")
                println("Bluetooth")
                println("Bluetooth")
                println(rssi)
            }
        }
    }



    }


