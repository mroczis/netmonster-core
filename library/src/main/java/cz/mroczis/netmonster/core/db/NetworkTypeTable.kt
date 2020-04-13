package cz.mroczis.netmonster.core.db

import cz.mroczis.netmonster.core.db.model.NetworkType

/**
 * Mapping between AOSP's constants and ours which are nearly the same
 */
object NetworkTypeTable {

    private val bands: Map<Int, NetworkType> = mutableMapOf<Int, NetworkType>().apply {
        put(0, NetworkType.Unknown(NetworkType.UNKNOWN))
        put(1, NetworkType.Gsm(NetworkType.GPRS))
        put(2, NetworkType.Gsm(NetworkType.EDGE))
        put(3, NetworkType.Wcdma(NetworkType.UMTS))
        put(4, NetworkType.Cdma(NetworkType.CDMA))
        put(5, NetworkType.Cdma(NetworkType.EVDO_0))
        put(6, NetworkType.Cdma(NetworkType.EVDO_A))
        put(7, NetworkType.Cdma(NetworkType.ONExRTT))
        put(8, NetworkType.Wcdma(NetworkType.HSDPA))
        put(9, NetworkType.Wcdma(NetworkType.HSUPA))
        put(10, NetworkType.Wcdma(NetworkType.HSPA))
        put(11, NetworkType.Unknown(NetworkType.IDEN))
        put(12, NetworkType.Cdma(NetworkType.EVDO_B))
        put(13, NetworkType.Lte(NetworkType.LTE))
        put(14, NetworkType.Unknown(NetworkType.EHRPD)) // Can be LTE or CDMA
        put(15, NetworkType.Wcdma(NetworkType.HSPAP))
        put(16, NetworkType.Gsm(NetworkType.GSM))
        put(17, NetworkType.Tdscdma(NetworkType.TD_SCDMA))
        put(18, NetworkType.Lte(NetworkType.IWLAN))
        put(20, NetworkType.Nr(NetworkType.NR))

        // Not in AOSP / not public in AOSP
        put(NetworkType.LTE_CA, NetworkType.Lte(NetworkType.LTE_CA))
        put(NetworkType.HSPA_DC, NetworkType.Wcdma(NetworkType.HSPA_DC))
        put(NetworkType.LTE_NR, NetworkType.Nr(NetworkType.LTE_NR))
        put(NetworkType.LTE_CA_NR, NetworkType.Nr(NetworkType.LTE_CA_NR))
    }


    fun get(networkType: Int) : NetworkType = bands.getOrElse(networkType) { bands.getValue(0) }


}