package cz.mroczis.netmonster.core.db.local

import android.content.Context
import android.os.Build

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

    companion object {

        @Volatile
        private var instance: LocalStorage? = null

        private val LOCATION_AREA_ENDIANNESS_INCORRECT = "area_endianness_flipped_${Build.TIME}"

        /**
         * Singleton impl
         */
        fun getInstance(context: Context): LocalStorage =
            instance ?: synchronized(this) {
                instance ?: LocalStorage(context.applicationContext).also { instance = it }
            }
    }
}