package cz.mroczis.netmonster.core.feature.merge

import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalGsm
import cz.mroczis.netmonster.core.model.signal.SignalWcdma
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat
import cz.mroczis.netmonster.core.util.maxOrNotnull
import cz.mroczis.netmonster.core.util.minOrNotnull
import cz.mroczis.netmonster.core.util.removeFirstOrNull

/**
 * Merges not primary cells (with [NoneConnection], [SecondaryConnection] as connection) from two sources:
 *  - [ITelephonyManagerCompat.getNeighboringCellInfo]
 *  - [ITelephonyManagerCompat.getAllCellInfo]
 * into one list without duplicities.
 */
class CellMergerPrimary : ICellMerger {

    override fun merge(oldApi: List<ICell>, newApi: List<ICell>, displayOn: Boolean): List<ICell> =
        if (newApi.isEmpty()) {
            oldApi
        } else if (!displayOn || newApi.size > 1) {
            // 1. Old API does not refresh when display is off
            // 2. When new API reports multiple primary cells then we've got dual SIM phone
            //    and it's better to avoid old API which in most cases return mixed data
            newApi
        } else {
            val oldCopy = oldApi.toMutableList()
            val merged = newApi.map { new ->
                // Cells from old API might be only CDMA, GSM, WCDMA or LTE
                when (new) {
                    is CellCdma -> oldCopy
                        .removeFirstOrNull { old -> old is CellCdma && old.bid == new.bid }
                        ?.let { old ->
                            mergeCdma(new, old as CellCdma)
                        } ?: new
                    is CellGsm -> oldCopy
                        .removeFirstOrNull { old -> old is CellGsm && old.cid == new.cid }
                        ?.let { old ->
                            mergeGsm(new, old as CellGsm)
                        } ?: new
                    is CellWcdma -> oldCopy
                        .removeFirstOrNull { old -> old is CellWcdma && old.cid == new.cid }
                        ?.let { old ->
                            mergeWcdma(new, old as CellWcdma)
                        } ?: new
                    is CellLte -> oldCopy
                        .removeFirstOrNull { old -> old is CellLte && old.cid == new.cid }
                        ?.let { old ->
                            mergeLte(new, old as CellLte)
                        } ?: new
                    // TD-SCDMA, NR not supported by old API
                    else -> new
                }
            }.toMutableList()

            // Append the rest of old API if there are no siblings left in new api
            if (oldCopy.isNotEmpty()) {
                merged.addAll(oldCopy)
            }

            merged
        }

    /**
     * CDMA merging for primary cells:
     *  - [old] & [new] usually report same data
     */
    private fun mergeCdma(new: CellCdma, old: CellCdma): CellCdma {
        val lat = new.lat ?: old.lat
        val lon = new.lon ?: old.lon

        return if (lat != new.lat || lon != new.lon) {
            new.copy(lat = lat, lon = lon)
        } else {
            new
        }
    }

    /**
     * LTE merging for primary cells:
     *  - [old] contains CID, TAC + RSSI, RSRP, RSRQ, CQI, SNR
     *  - [new] same as old + band, PCI and TA
     *
     * What can be improved:
     *  - Generally just signal metrics, we'll take first valid
     */
    private fun mergeLte(new: CellLte, old: CellLte): CellLte =
        new.copy(signal = new.signal.merge(old.signal))

    /**
     * WCDMA merging for primary cells:
     *  - [old] contains CID, LAC, PSC + RSSI, BER
     *  - [new] same as old + band and multiple signal values
     *
     * What can be improved:
     *  - RSSI, RSCP, Ec/Io, BER
     *  - PSC
     *  - LAC
     */
    private fun mergeWcdma(new: CellWcdma, old: CellWcdma): CellWcdma {
        val rssi = pickBetterRssi(new.signal.rssi, old.signal.rssi, SignalWcdma.RSSI_MIN.toInt())

        // Older API used to return better PSC values, keep favouring except 0 which
        // some phones use as invalid value
        val psc = if (old.psc != null && old.psc > CellWcdma.PSC_MIN) {
            old.psc
        } else {
            new.psc ?: old.psc
        }

        return new.copy(
            lac = old.lac ?: new.lac,
            psc = psc,
            signal = new.signal.copy(
                rssi = rssi,
                rscp = new.signal.rscp ?: old.signal.rscp,
                ecio = new.signal.ecio ?: old.signal.ecio,
                bitErrorRate = new.signal.bitErrorRate ?: old.signal.bitErrorRate
            )
        )
    }

    /**
     * GSM merging for primary cells:
     * - [old] contains CID, LAC, RSSI and BER
     * - [new] same as old + BSIC, band
     *
     * What can be improved:
     *  - RSSI
     *  - BER
     */
    private fun mergeGsm(new: CellGsm, old: CellGsm): CellGsm {
        val rssi = pickBetterRssi(new.signal.rssi, old.signal.rssi, SignalGsm.RSSI_MIN.toInt())
        val ber = new.signal.bitErrorRate ?: old.signal.bitErrorRate

        return if (rssi != new.signal.rssi || ber != new.signal.bitErrorRate) {
            new.copy(signal = new.signal.copy(rssi = rssi, bitErrorRate = ber))
        } else {
            new
        }
    }

    /**
     * Picks better signal favouring smaller values and ignoring marginal
     * values.
     */
    private fun pickBetterRssi(new: Int?, old: Int?, minPossible: Int): Int? =
        if (new != old) {
            // Marginal situation - some devices constantly report -51 or -113
            // we'll try pick something else if possible
            val min = minOrNotnull(new, old)
            if (min == minPossible) {
                maxOrNotnull(new, old)
            } else min
        } else {
            new
        }


}