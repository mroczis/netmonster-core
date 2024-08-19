package cz.mroczis.netmonster.core.util

import android.telephony.ServiceState

private val DATA_STATE_REGEX = "mDataRegState=(\\d+)".toRegex()

/**
 * Detects registration state combining data + voice registration state
 */
internal val ServiceState.registrationState: Int
    get() {
        val voiceState = state
        val dataState = DATA_STATE_REGEX.find(toString())?.groups?.get(1)?.value?.toIntOrNull() // not public in AOSP API

        return if (voiceState == ServiceState.STATE_IN_SERVICE || dataState == ServiceState.STATE_IN_SERVICE) {
            ServiceState.STATE_IN_SERVICE
        } else if (voiceState == ServiceState.STATE_EMERGENCY_ONLY || dataState == ServiceState.STATE_EMERGENCY_ONLY) {
            ServiceState.STATE_EMERGENCY_ONLY
        } else if (voiceState == ServiceState.STATE_OUT_OF_SERVICE || dataState == ServiceState.STATE_OUT_OF_SERVICE) {
            ServiceState.STATE_OUT_OF_SERVICE
        } else {
            ServiceState.STATE_POWER_OFF
        }
    }
