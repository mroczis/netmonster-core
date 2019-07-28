package cz.mroczis.netmonster.core.db

import cz.mroczis.netmonster.core.db.model.BandEntity
import cz.mroczis.netmonster.core.model.band.BandWcdma

object WcdmaBandTable {

    /**
     * WCDMA FDD min (band 2, additional column) from
     * [3GPP 25.101](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=1151)
     */
    const val DOWNLINK_MIN = 412

    /**
     * WCDMA FDD max (band 1) from
     * [3GPP 25.101](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=1151)
     */
    const val DOWNLINK_MAX = 10_838

    private val bands = arrayOf(
        BandEntity(DOWNLINK_MIN..687, "1900", 2),
        BandEntity(712..763, "800", 19),
        BandEntity(862..912, "1500", 21),
        BandEntity(1007..1087, "850", 5),
        BandEntity(1162..1513, "1800", 3),
        BandEntity(1537..2087, "AWS", 4),
        BandEntity(2237..2563, "2600", 7),
        BandEntity(2937..3088, "900", 8),
        BandEntity(3112..3388, "AWS", 10),
        BandEntity(3712..3787, "1500", 11),
        BandEntity(3842..3903, "700", 12),
        BandEntity(4017..4043, "700", 13),
        BandEntity(4117..4143, "700", 14),
        BandEntity(4357..4458, "850", 5),
        BandEntity(4512..4638, "800", 20),
        BandEntity(4662..5038, "3500", 22),
        BandEntity(5112..5413, "1900", 25),
        BandEntity(5762..5913, "850", 26),
        BandEntity(6292..6592, "1900", 25),
        BandEntity(6617..6813, "1500", 32),
        BandEntity(9237..9387, "1800", 9),
        BandEntity(9662..9938, "1900", 2),
        BandEntity(10562..DOWNLINK_MAX, "2100", 1)
    )

    internal fun get(uarfcn: Int): BandEntity? =
        bands.firstOrNull { it.channelRange.contains(uarfcn) }


    internal fun map(uarfcn: Int): BandWcdma? =
        get(uarfcn)?.let {
            BandWcdma(
                downlinkUarfcn = uarfcn,
                number = it.number,
                name = it.name
            )
        }

}