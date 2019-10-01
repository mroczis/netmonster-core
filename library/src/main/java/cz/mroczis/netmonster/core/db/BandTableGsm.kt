package cz.mroczis.netmonster.core.db

import cz.mroczis.netmonster.core.db.model.BandEntity
import cz.mroczis.netmonster.core.db.model.IBandEntity
import cz.mroczis.netmonster.core.model.band.BandGsm

object BandTableGsm {

    const val NUMBER_UNKNOWN = -1

    /**
     * North America + Caribbean prefix
     */
    private const val MCC_PREFIX_NORTH_AMERICA = "3"

    /**
     * South America + central America
     */
    private const val MCC_PREFIX_SOUTH_AMERICA = "7"

    private val bands = arrayOf(
        BandEntity(0..124, "900", 900),
        BandEntity(128..251, "850", 850),
        BandEntity(259..293, "450", 450),
        BandEntity(306..340, "480", 480),
        BandEntity(512..810, "1800/1900", NUMBER_UNKNOWN),
        BandEntity(811..885, "1800", 1800),
        BandEntity(955..1_023, "900", 900)
    )

    /**
     * Countries in Americas that do run both GSM 1800 and 1900
     */
    private val PCS_AND_DCS_AMERICAS = arrayOf(
        "348", "346", "370", "338", "356", "358", "376",
        "724", "748", "734"
    )

    /**
     * Countries in Americas that do run both GSM 1800
     */
    private val DCS_AMERICAS = arrayOf(
        "352", "372", "360", "712"
    )

    internal fun get(arfcn: Int, mcc: String?): IBandEntity? {
        val band = bands.firstOrNull { it.channelRange.contains(arfcn) }

        return if (band != null && band.number == NUMBER_UNKNOWN) {
            // PSC, DSC situation here -> result depends on country
            if (mcc == null) {
                band
            } else if (mcc.startsWith(MCC_PREFIX_NORTH_AMERICA) || mcc.startsWith(MCC_PREFIX_SOUTH_AMERICA)) {
                // And it gets even more complicated cause those rules are not universal
                when {
                    PCS_AND_DCS_AMERICAS.contains(mcc) ->
                        band
                    DCS_AMERICAS.contains(mcc) ->
                        band.copy(name = "1800", number = 1800)
                    else ->
                        band.copy(channelRange = 512..810, name = "1900", number = 1900)
                }
            } else {
                band.copy(name = "1800", number = 1800)
            }
        } else {
            band
        }
    }

    /**
     * Attempts to find current band information depending on [arfcn] and [mcc].
     * If no such band is found then result [BandGsm] will contain only [BandGsm.arfcn].
     *
     * In case of PCS/DCS uses [mcc] to better identify current band. If result is ambiguous
     * then "1800/1900" band name is returned.
     */
    fun map(arfcn: Int, mcc: String?) : BandGsm {
        val raw = get(arfcn, mcc)
        return BandGsm(arfcn, name = raw?.name, number = raw?.number)
    }


}

