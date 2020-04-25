package cz.mroczis.netmonster.core.telephony.network

import android.Manifest
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat
import java.lang.reflect.Method

/**
 * Fetches current network operator.
 * Based on [TelephonyManager.getNetworkOperator]
 */
internal class NetworkOperatorGetter : INetworkGetter {

    companion object {

        /**
         * Reflection does not work since P+ so there's no need to attempt it.
         */
        private val REFLECTION_ENABLED = Build.VERSION.SDK_INT < Build.VERSION_CODES.P

        /**
         * Known methods that we can call to get network operator.
         * These methods were never public in AOSP API. However, they were working mainly
         * on Huawei phones and they are the only ones that return valid values.
         *
         * HUAWEI DLI-L22 / SDK 24
         *  - getNetworkOperator() always returns PLMN for SIM 1 no matter what subId bound to [TelephonyManager]
         *  - getNetworkOperator(int) returns correct values
         */
        private val NETWORK_OPERATOR_FIELDS =
            if (REFLECTION_ENABLED) {
                arrayOf("getNetworkOperatorForSubscription", "getNetworkOperator")
            } else {
                emptyArray()
            }
    }

    /**
     * Reflection methods that we might use to get network operator.
     * This list contains only methods that do exist and might be invoked.
     */
    private val existingMethods: List<Method> =
        NETWORK_OPERATOR_FIELDS.mapNotNull { methodName ->
            try {
                TelephonyManager::class.java.getDeclaredMethod(
                    methodName, Int::class.javaPrimitiveType
                ).apply { isAccessible = true }
            } catch (e: Throwable) {
                null
            }
        }


    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getNetwork(telephony: ITelephonyManagerCompat): Network? =
        getFromReflection(telephony.getTelephonyManager(), telephony.getSubscriberId())
            ?: getFromServiceState(telephony)
            ?: getFromNetworkOperator(telephony.getTelephonyManager())

    private fun getFromReflection(telephony: TelephonyManager?, subId: Int) =
        if (existingMethods.isNotEmpty() && telephony != null) {
            existingMethods.mapNotNull { method ->
                try {
                    Network.map(method.invoke(telephony, subId) as? String)
                } catch (ignored: Throwable) {
                    null
                }
            }.firstOrNull()
        } else null

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getFromServiceState(telephony: ITelephonyManagerCompat) =
        Network.map(telephony.getServiceState()?.operatorNumeric)

    private fun getFromNetworkOperator(telephony: TelephonyManager?) =
        telephony?.networkOperator?.let { Network.map(it) }

}