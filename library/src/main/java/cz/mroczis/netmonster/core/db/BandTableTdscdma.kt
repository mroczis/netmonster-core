package cz.mroczis.netmonster.core.db

import cz.mroczis.netmonster.core.db.model.BandEntity
import cz.mroczis.netmonster.core.db.model.IBandEntity
import cz.mroczis.netmonster.core.model.band.BandTdscdma

/**
 * Sources:
 * [China's TD-SCDMA allocation](https://techliberation.com/wp-content/uploads/2013/02/TDD_band.html)
 */
object BandTableTdscdma {

    private val CHINA_MCC = arrayOf("460", "461")

    private val bands = arrayOf(
        // BandEntity(9250..9550, "PCS", 35), // Uplink only -> Android reports just downlink
        BandEntity(9_400..9_600, "1900", 39), // China only
        BandEntity(9_500..9_600, "1900", 33),
        BandEntity(9_550..9_650, "PCS", 37),
        BandEntity(9_650..9_950, "PCS", 36),
        BandEntity(10_050..10_125, "2000", 34),
        BandEntity(11_500..12_000, "2300", 40), // China only
        BandEntity(12_850..13_100, "2600", 38)

    )

    internal fun get(uarfcn: Int, mcc: String?): IBandEntity? {
        // Here it's a bit complicated cause bands 37 and 33 are overlapping and
        // there's no info which is used where so result might be misleading
        // TODO investigate usage of TD-SCDMA bands across the world

        return if (CHINA_MCC.contains(mcc)) {
            bands.firstOrNull { it.channelRange.contains(uarfcn) }
        } else {
            // Last cause we want to mitigate China's bands
            bands.lastOrNull { it.channelRange.contains(uarfcn) }
        }
    }

    /**
     * Attempts to find current band information depending on [uarfcn] and [mcc].
     * If no such band is found then result [BandTdscdma] will contain only [BandTdscdma.downlinkUarfcn].
     */
    fun map(uarfcn: Int, mcc: String?): BandTdscdma {
        val raw = get(uarfcn, mcc)
        return BandTdscdma(
            downlinkUarfcn = uarfcn,
            number = raw?.number,
            name = raw?.name
        )
    }

}