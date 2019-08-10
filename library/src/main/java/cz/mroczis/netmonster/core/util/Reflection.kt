package cz.mroczis.netmonster.core.util

object Reflection {

    // CellSignalStrengthGsm
    const val GSM_BIT_ERROR_RATE = "mBitErrorRate"
    const val GSM_TIMING_ADVANCE = "mTimingAdvance" // Standard Android API
    const val GSM_TA = "mTa" // Sony Xperia

    // CellSignalStrengthLte
    const val LTE_RSSI = "mSignalStrength"
    const val LTE_RSRP = "mRsrp"
    const val LTE_RSRQ = "mRsrq"
    const val LTE_SNR = "mRssnr"
    const val LTE_CQI = "mCqi"
    const val LTE_TA = "mTimingAdvance"

    // SignalStrengths
    const val UMTS_RSCP = "mWcdmaRscp" // Huawei Dual SIM devices
    const val UMTS_ECIO = "mWcdmaEcio" // Huawei Dual SIM devices

    const val SS_LTE_RSSI = "mLteSignalStrength"
    const val SS_LTE_RSRP = "mLteRsrp"
    const val SS_LTE_RSRQ = "mLteRsrq"
    const val SS_LTE_SNR = "mLteRssnr"
    const val SS_LTE_CQI = "mLteCqi"

    fun intFieldOrNull(name: String, source: Any?) =
        try {
            source?.javaClass?.getDeclaredField(name)?.apply {
                isAccessible = true
            }?.get(source) as? Int
        } catch (e: Throwable) {
            null
        }


}