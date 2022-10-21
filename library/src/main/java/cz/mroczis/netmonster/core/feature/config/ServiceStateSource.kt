package cz.mroczis.netmonster.core.feature.config

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.cache.TelephonyCache
import cz.mroczis.netmonster.core.util.SingleEventPhoneStateListener

/**
 * On Android N and older fetches [ServiceState] using service state listener.
 * On Android O and newer takes [ServiceState] from [TelephonyManager.getServiceState].
 */
class ServiceStateSource {

    /**
     * Registers service state listener and awaits data. After 1000 milliseconds time outs if
     * nothing is delivered.
     *
     * On Android O and newer directly grabs [ServiceState] from [TelephonyManager].
     */
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun get(telephonyManager: TelephonyManager, subId: Int): ServiceState? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                telephonyManager.serviceState
            } catch (e: SecurityException) {
                // Samsung Galaxy A80 throws SecurityException and requires `android.permission.MODIFY_PHONE_STATE` to access this field
                getPreOreo(telephonyManager, subId)
            }
        } else {
            getPreOreo(telephonyManager, subId)
        }

    private fun getPreOreo(telephonyManager: TelephonyManager, subId: Int): ServiceState? =
        TelephonyCache.getOrUpdate(subId, TelephonyCache.Event.SERVICE_STATE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager.requestSingleUpdate { serviceStateListener (it) }
            } else {
                telephonyManager.requestPhoneStateUpdate { serviceStateListener(subId, it) }
            }
        }

    /**
     * Kotlin friendly PhoneStateListener that grabs [ServiceState]
     */
    @TargetApi(Build.VERSION_CODES.R)
    private fun serviceStateListener(
        subId: Int?,
        onChanged: UpdateResult<SingleEventPhoneStateListener, ServiceState>
    ) = object : SingleEventPhoneStateListener(LISTEN_SERVICE_STATE, subId) {
        override fun onServiceStateChanged(serviceState: ServiceState?) {
            super.onServiceStateChanged(serviceState)
            if (serviceState != null) {
                onChanged.invoke(this, serviceState)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun serviceStateListener(onChanged: UpdateResult<TelephonyCallback, ServiceState>) =
        object : TelephonyCallback(), TelephonyCallback.ServiceStateListener {
            override fun onServiceStateChanged(serviceState: ServiceState) {
                onChanged(this, serviceState)
            }
        }
}