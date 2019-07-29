package cz.mroczis.netmonster.core.db

import cz.mroczis.netmonster.core.db.model.BandEntity
import cz.mroczis.netmonster.core.model.band.BandLte

object BandTableLte {

    /**
     * LTE min, deafult is 0.
     * Changed to 1 cause Samsung phones report 0 when data are incorrect.
     * [3GPP 36.101](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=2411)
     */
    const val DOWNLINK_MIN = 1

    /**
     * WCDMA FDD max (band 1) from
     * [3GPP 36.101](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=2411)
     */
    const val DOWNLINK_MAX = 70_645

    private val bands = arrayOf(
        BandEntity(DOWNLINK_MIN..599, "2100", 1),
        BandEntity(600..1199, "1900", 2),
        BandEntity(1200..1949, "1800", 3),
        BandEntity(1950..2399, "AWS", 4),
        BandEntity(2400..2649, "850", 5),
        BandEntity(2650..2749, "900", 6),
        BandEntity(2750..3449, "2600", 7),
        BandEntity(3450..3799, "900", 8),
        BandEntity(3800..4149, "1800", 9),
        BandEntity(4150..4749, "AWS", 10),
        BandEntity(4750..5009, "1500", 11),
        BandEntity(5010..5179, "700", 12),
        BandEntity(5180..5279, "700", 13),
        BandEntity(5280..5729, "700", 14),
        BandEntity(5730..5849, "700", 17),
        BandEntity(5850..5999, "800", 18),
        BandEntity(6000..6149, "800", 19),
        BandEntity(6150..6449, "800", 20),
        BandEntity(6450..6599, "1500", 21),
        BandEntity(6600..7499, "3500", 22),
        BandEntity(7500..7699, "2000", 23),
        BandEntity(7700..8039, "1600", 24),
        BandEntity(8040..8689, "1900", 25),
        BandEntity(8690..9039, "850", 26),
        BandEntity(9040..9209, "800", 27),
        BandEntity(9210..9659, "700", 28),
        BandEntity(9660..9769, "700", 29),
        BandEntity(9770..9869, "2300", 30),
        BandEntity(9870..9919, "450", 31),
        BandEntity(9920..10359, "1500", 32),

        // TDD start
        BandEntity(36000..36199, "1900", 33),
        BandEntity(36200..36349, "2000", 34),
        BandEntity(36350..36949, "PCS", 35),
        BandEntity(36950..37549, "PCS", 36),
        BandEntity(37550..37749, "PCS", 37),
        BandEntity(37750..38249, "2600", 38),
        BandEntity(38250..38649, "1900", 39),
        BandEntity(38650..39649, "2300", 40),
        BandEntity(39650..41589, "2500", 41),
        BandEntity(41590..43589, "3500", 42),
        BandEntity(43590..45589, "3700", 43),
        BandEntity(45590..46589, "700", 45),
        BandEntity(46590..46789, "1500", 44),
        BandEntity(55240..56739, "3600", 48),

        BandEntity(56740..58239, "3600", 49),
        BandEntity(58240..59089, "1500", 50),
        BandEntity(59090..59139, "1500", 51),
        BandEntity(59140..60139, "3300", 52),
        BandEntity(60140..60254, "2500", 53),
        // TDD end

        BandEntity(65536..66435, "2100", 65),
        BandEntity(66436..67335, "AWS", 66),
        BandEntity(67336..67535, "700", 67),
        BandEntity(67536..67835, "700", 68),
        BandEntity(67836..68335, "2500", 69),
        BandEntity(68336..68585, "AWS", 70),
        BandEntity(68586..68935, "600", 71),
        BandEntity(68936..68985, "450", 72),
        BandEntity(68986..69035, "450", 73),
        BandEntity(69036..69465, "L", 74),
        BandEntity(69466..70315, "1500", 75),
        BandEntity(70316..70365, "1500", 76),
        BandEntity(70366..70545, "700", 85),
        BandEntity(70546..70595, "410", 87),
        BandEntity(70596..DOWNLINK_MAX, "410", 88)
    )

    internal fun get(earfcn: Int): BandEntity? =
        bands.firstOrNull { it.channelRange.contains(earfcn) }


    internal fun map(earfcn: Int, bandwidth: Int?): BandLte? =
        get(earfcn)?.let {
            BandLte(
                downlinkEarfcn = earfcn,
                number = it.number,
                name = it.name,
                bandwidth = bandwidth
            )
        }

}