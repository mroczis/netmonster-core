package cz.mroczis.netmonster.sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Activity periodically updates data (once in [REFRESH_RATIO] ms) when it's on foreground.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REFRESH_RATIO = 5_000L
    }

    private val handler = Handler()
    private val adapter = MainAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED
        ) {
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
    }

    private fun loop() {
        updateData()
        handler.postDelayed(REFRESH_RATIO) { loop() }
    }

    @SuppressLint("MissingPermission")
    private fun updateData() {
        NetMonsterFactory.get(this).apply {
            val merged = getCells()
            adapter.data = merged

            Log.d("NTM-RES", " \n${merged.joinToString(separator = "\n")}")
        }
    }

}
