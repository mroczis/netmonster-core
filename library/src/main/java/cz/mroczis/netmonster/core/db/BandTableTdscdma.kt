package cz.mroczis.netmonster.core.db

import cz.mroczis.netmonster.core.db.model.BandEntity
import cz.mroczis.netmonster.core.model.band.BandTdscdma

/**
 * Sources:
 * [China's TD-SCDMA allocation](https://techliberation.com/wp-content/uploads/2013/02/TDD_band.html)
 */
object BandTableTdscdma {

    const val DOWNLINK_MIN = 9400
    const val DOWNLINK_MAX = 13_100

    private val CHINA_MCC = arrayOf("460", "461")

    private val bands = arrayOf(
        // BandEntity(9250..9550, "PCS", 35), // Uplink only -> Android reports just downlink
        BandEntity(DOWNLINK_MIN..9600, "1900", 39), // China only
        BandEntity(9500..9600, "1900", 33),
        BandEntity(9550..9650, "PCS", 37),
        BandEntity(9650..9950, "PCS", 36),
        BandEntity(10050..10125, "2000", 34),
        BandEntity(11500..12000, "2300", 40), // China only
        BandEntity(12850..DOWNLINK_MAX, "2600", 38)

    )

    internal fun get(uarfcn: Int, mcc: String?): BandEntity? {
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

    internal fun map(uarfcn: Int, mcc: String?): BandTdscdma? =
        get(uarfcn,mcc)?.let {
            BandTdscdma(
                downlinkUarfcn = uarfcn,
                number = it.number,
                name = it.name
            )
        }

}