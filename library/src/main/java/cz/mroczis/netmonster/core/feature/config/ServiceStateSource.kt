package cz.mroczis.netmonster.core.feature.config

import android.Manifest
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.cache.TelephonyCache
import cz.mroczis.netmonster.core.feature.config.ServiceStateSource.ServiceStateListener
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort

/**
 * On Android N and older fetches [ServiceState] using [ServiceStateListener].
 * On Android O and newer takes [ServiceState] from [TelephonyManager.getServiceState].
 */
class ServiceStateSource {

    /**
     * Registers [ServiceStateListener] and awaits data. After 100 milliseconds time outs if
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
        TelephonyCache.getOrUpdate(subId, PhoneStateListener.LISTEN_SERVICE_STATE) {
            telephonyManager.requestSingleUpdate<ServiceState>(PhoneStateListener.LISTEN_SERVICE_STATE) { onData ->
                ServiceStateListener(subId, onData)
            }
        }

    /**
     * Kotlin friendly PhoneStateListener that grabs [ServiceState]
     */
    private class ServiceStateListener(
        subId: Int?,
        private val simStateListener: ServiceStateListener.(state: ServiceState) -> Unit
    ) : PhoneStateListenerPort(subId) {

        override fun onServiceStateChanged(serviceState: ServiceState?) {
            if (serviceState != null) {
                simStateListener.invoke(this, serviceState)
            }
        }
    }
}