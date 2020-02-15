package cz.mroczis.netmonster.core.model

import cz.mroczis.netmonster.core.db.MccIsoTable
import cz.mroczis.netmonster.core.util.inRangeOrNull

/**
 * Representation of network based on MCC, MNC codes
 */
data class Network internal constructor(
    /**
     * Mobile country code, 3 numeric characters
     *
     * Range: 001..999
     */
    val mcc: String,

    /**
     * Mobile network code, 2 numeric characters
     *
     * Range: 01..99
     */
    val mnc: String,

    /**
     * ISO 3166-1 alpha-2 code of country on planet Earth,
     * null if our library does not know such [mcc]
     */
    val iso: String?
) {

    fun toInt(): Int = toPlmn(separator = "").toInt()

    fun toPlmn(separator: String = "") = "$mcc$separator$mnc"

    companion object {

        const val MCC_MIN = 1
        const val MCC_MAX = 999

        const val MNC_MIN = 0
        const val MNC_MAX = 999

        internal val MCC_RANGE = MCC_MIN..MCC_MAX
        internal val MNC_RANGE = MNC_MIN..MNC_MAX

        fun map(mcc: Int, mnc: Int): Network? =
            if (mcc.inRangeOrNull(MCC_RANGE) != null && mnc.inRangeOrNull(MNC_RANGE) != null) {
                Network(mcc.toString(), mnc.toString().padStart(2, '0'), MccIsoTable.getByMcc(mcc.toString()))
            } else null

        fun map(mcc: String?, mnc: String?): Network? {
            val mccInt = mcc?.toIntOrNull()
            val mncInt = mnc?.toIntOrNull()

            return if (mccInt?.inRangeOrNull(MCC_RANGE) != null && mncInt?.inRangeOrNull(MNC_RANGE) != null) {
                return Network(mcc, mnc, MccIsoTable.getByMcc(mcc))
            } else null
        }

        fun map(plmn: String?): Network? =
            if (plmn != null && plmn.length >= 5) {
                map(plmn.substring(0, 3), plmn.substring(3))
            } else null

    }

}