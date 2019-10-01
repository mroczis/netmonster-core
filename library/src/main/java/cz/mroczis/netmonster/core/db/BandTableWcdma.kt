package cz.mroczis.netmonster.core.db

import cz.mroczis.netmonster.core.db.model.BandEntity
import cz.mroczis.netmonster.core.db.model.IBandEntity
import cz.mroczis.netmonster.core.model.band.BandWcdma

/**
 * Source:
 * [3GPP 25.101](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=1151)
 */
object  BandTableWcdma {

    private val bands = arrayOf(
        BandEntity(412..687, "1900", 2),
        BandEntity(712..763, "800", 19),
        BandEntity(862..912, "1500", 21),
        BandEntity(1_007..1_087, "850", 5),
        BandEntity(1_162..1_513, "1800", 3),
        BandEntity(1_537..2_087, "AWS", 4),
        BandEntity(2_237..2_563, "2600", 7),
        BandEntity(2_937..3_088, "900", 8),
        BandEntity(3_112..3_388, "AWS", 10),
        BandEntity(3_712..3_787, "1500", 11),
        BandEntity(3_842..3_903, "700", 12),
        BandEntity(4_017..4_043, "700", 13),
        BandEntity(4_117..4_143, "700", 14),
        BandEntity(4_357..4_458, "850", 5),
        BandEntity(4_512..4_638, "800", 20),
        BandEntity(4_662..5_038, "3500", 22),
        BandEntity(5_112..5_413, "1900", 25),
        BandEntity(5_762..5_913, "850", 26),
        BandEntity(6_292..6_592, "1900", 25),
        BandEntity(6_617..6_813, "1500", 32),
        BandEntity(9_237..9_387, "1800", 9),
        BandEntity(9_662..9_938, "1900", 2),
        BandEntity(10_562..10_838, "2100", 1)
    )

    internal fun get(uarfcn: Int): IBandEntity? =
        bands.firstOrNull { it.channelRange.contains(uarfcn) }


    /**
     * Attempts to find current band information depending on [uarfcn].
     * If no such band is found then result [BandWcdma] will contain only [BandWcdma.downlinkUarfcn].
     */
    fun map(uarfcn: Int): BandWcdma {
        val raw = get(uarfcn)
        return BandWcdma(
            downlinkUarfcn = uarfcn,
            number = raw?.number,
            name = raw?.name
        )
    }
}