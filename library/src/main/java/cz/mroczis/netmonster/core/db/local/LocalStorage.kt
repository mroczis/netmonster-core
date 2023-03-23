package cz.mroczis.netmonster.core.db.local

import android.content.Context
import android.os.Build
import cz.mroczis.netmonster.core.SubscriptionId

/**
 * Simple persistence implementation that uses Android's shared prefs
 */
internal class LocalStorage private constructor(
    context: Context
) : ILocalStorage {

    private val prefs = context.getSharedPreferences("netmonster-core", Context.MODE_PRIVATE)

    override var locationAreaEndiannessIncorrect
        get() = prefs.getBoolean(LOCATION_AREA_ENDIANNESS_INCORRECT, false)
        set(value) = prefs.edit().putBoolean(LOCATION_AREA_ENDIANNESS_INCORRECT, value).apply()

    override var reportsLteBandwidthDirectly: Boolean
        get() = prefs.getBoolean(REPORTS_LTE_BANDWIDTH_DIRECTLY, false)
        set(value) = prefs.edit().putBoolean(REPORTS_LTE_BANDWIDTH_DIRECTLY, value).apply()

    override fun setReportsLteTimingAdvance(id: SubscriptionId, reports: Boolean) {
        prefs.edit().putBoolean("${REPORTS_LTE_TIMING_ADVANCE}_$id", reports).apply()
    }

    override fun getReportsLteTimingAdvance(id: SubscriptionId): Boolean =
        prefs.getBoolean("${REPORTS_LTE_TIMING_ADVANCE}_$id", false)

    companion object {

        @Volatile
        private var instance: LocalStorage? = null

        private val LOCATION_AREA_ENDIANNESS_INCORRECT = "area_endianness_flipped_${Build.TIME}"
        private val REPORTS_LTE_BANDWIDTH_DIRECTLY = "reports_lte_bandwidth_${Build.TIME}"
        private val REPORTS_LTE_TIMING_ADVANCE = "reports_lte_timing_advance_${Build.TIME}"

        /**
         * Singleton impl
         */
        fun getInstance(context: Context): LocalStorage =
            instance ?: synchronized(this) {
                instance ?: LocalStorage(context.applicationContext).also { instance = it }
            }
    }
}