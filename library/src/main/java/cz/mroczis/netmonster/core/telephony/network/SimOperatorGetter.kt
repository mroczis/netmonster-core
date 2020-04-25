package cz.mroczis.netmonster.core.telephony.network

import android.os.Build
import android.telephony.TelephonyManager
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat
import java.lang.reflect.Method

/**
 * Fetches current SIM operator.
 * Based on [TelephonyManager.getSimOperator]
 */
internal class SimOperatorGetter : INetworkGetter {

    companion object {
        /**
         * Reflection does not work since P+ so there's no need to attempt it.
         */
        private val REFLECTION_ENABLED = Build.VERSION.SDK_INT < Build.VERSION_CODES.P
        
        /**
         * Known methods that we can call to get SIM operator.
         * These methods were never public in AOSP API. However, they were working mainly
         * on Huawei phones and they are the only ones that return valid values.
         *
         * HUAWEI DLI-L22 / SDK 24
         *  - getSimOperator() always returns PLMN for SIM 1 no matter what subId bound to [TelephonyManager]
         *  - getSimOperator(int) returns correct values
         */
        val SIM_OPERATOR_FIELDS =
            if (REFLECTION_ENABLED) {
                arrayOf("getSimOperatorForSubscription", "getSimOperator")
            } else emptyArray()
    }

    /**
     * Reflection methods that we might use to get SIM operator.
     * This list contains only methods that do exist and might be invoked.
     */
    private val existingMethods: List<Method> = SIM_OPERATOR_FIELDS.mapNotNull { methodName ->
        try {
            TelephonyManager::class.java.getDeclaredMethod(
                methodName, Int::class.javaPrimitiveType
            ).apply { isAccessible = true }
        } catch (e: Throwable) {
            null
        }
    }

    override fun getNetwork(telephony: ITelephonyManagerCompat): Network? =
        getFromReflection(telephony.getTelephonyManager(), telephony.getSubscriberId())
            ?: getFromSimOperator(telephony.getTelephonyManager())

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

    private fun getFromSimOperator(telephony: TelephonyManager?) =
        telephony?.simOperator?.let { Network.map(it) }

}