package cz.mroczis.netmonster.core.telephony.mapper.cell

import android.annotation.TargetApi
import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.CellSignalStrengthLte
import android.telephony.SignalStrength
import android.telephony.gsm.GsmCellLocation
import cz.mroczis.netmonster.core.db.BandTableLte
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.band.AggregatedBandLte
import cz.mroczis.netmonster.core.model.band.BandLte
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.util.*
import kotlin.math.abs

/**
 * [CellIdentityLte] -> [CellLte]
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
internal fun CellIdentityLte.mapCell(
    subId: Int,
    connection: IConnection,
    signal: SignalLte,
    timestamp: Long? = null,
    plmn: Network? = null,
): CellLte {
    val network = plmn ?: mapNetwork()
    val ci = ci.inRangeOrNull(CellLte.CID_RANGE)
    val tac = tac.inRangeOrNull(CellLte.TAC_RANGE)
    val pci = pci.inRangeOrNull(CellLte.PCI_RANGE)

    val earfcn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        earfcn.inRangeOrNull(BandLte.DOWNLINK_EARFCN_RANGE)
    } else null

    val band = if (earfcn != null) {
        BandTableLte.map(earfcn = earfcn, mcc = network?.mcc)
    } else null

    val suggestedBands = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        bands.filter { it in BandTableLte.BAND_NUMBER_RANGE }
    } else {
        emptyList()
    }

    val aggregatedBands = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && band?.number != null) {
        val bands = bands
        if (bands.size > 1 && bands.contains(band.number)) {
            (bands.toList() - band.number)
                .mapNotNull { BandTableLte.getByNumber(it) }
                .map { AggregatedBandLte(it.number, it.name) }
        } else emptyList()
    } else emptyList()

    val bandwidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        bandwidth.inRangeOrNull(CellLte.BANDWIDTH_RANGE).takeIf {
            // Sync issue, devices sometimes report invalid combos
            // Example: EARFCN=473, Bands=[20], BW=10_000
            // Real values: EARFCN=473, Bands=[1], BW=20_000
            band?.number == null || suggestedBands.isEmpty() || suggestedBands.contains(band.number)
        }
    } else null


    return CellLte(
        network = network,
        eci = ci,
        tac = tac,
        pci = pci,
        bandwidth = bandwidth,
        connectionStatus = connection,
        signal = signal,
        band = band,
        subscriptionId = subId,
        timestamp = timestamp,
        aggregatedBands = aggregatedBands
    )
}

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
internal fun CellSignalStrengthLte.mapSignal(): SignalLte {
    val rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rssi
    } else {
        Reflection.intFieldOrNull(Reflection.LTE_RSSI, this)?.let { main ->
            // Back in old days RSSI was a big mess...
            if (main in RSSI_ASU_RANGE) { // in ASU
                main.toDbm()
            } else if (main > 31 && main != 99) { // in RSRP ASU
                -113 + main
            } else if (main >= -140 && main <= -40) { // in DBM
                main
            } else null
        }
    }?.inRangeOrNull(SignalLte.RSSI_RANGE)

    val rsrp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        rsrp
    } else {
        Reflection.intFieldOrNull(Reflection.LTE_RSRP, this)
    }?.let {
        var rsrp = it.toDouble()
        // RSRP was a bit bigger mess than RSSI though
        // Sony E2003, HUAWEI TIT-L01 reported '464' = -46.4
        // samsung SM-G935V reported '1025' = -102.5
        if (abs(rsrp) > 140 && it != Integer.MAX_VALUE) {
            do {
                rsrp /= 10
            } while (rsrp / 10 > 140)

            if (rsrp in 1.0..43.0) {
                rsrp += -140
            }

        } else if (rsrp in 1.0..43.0) {
            rsrp += -140
        }

        if (rsrp > 0) {
            rsrp *= -1
        }

        rsrp
    }?.inRangeOrNull(SignalLte.RSRP_RANGE)

    val rawRsrq = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        rsrq
    } else {
        Reflection.intFieldOrNull(Reflection.LTE_RSRQ, this)
    }

    val rsrq = rawRsrq?.let {
        var rsrq = it.toDouble()
        // Some devices (Sony E2003) report RSRQ with one decimal place -> division by 10 required
        // Usually RSRQ is in rage from -3 to -14 hence '25' starting bound

        if ((abs(rsrq) > 25) && it != Integer.MAX_VALUE) {
            rsrq /= 10
        }

        if (rsrq > 0) {
            rsrq *= -1
        }

        rsrq
    }?.inRangeOrNull(SignalLte.RSRQ_RANGE)

    val snr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        rssnr
    } else {
        Reflection.intFieldOrNull(Reflection.LTE_SNR, this)
    }?.let {
        // SNR in range from 0 to 3 means basically no signal and occurs rarely on Android devices
        // On older devices (ASUS_Z00AD) this value has 1 decimal place
        var snr = it.toDouble()
        if (snr > 30) {
            snr /= 10
        }

        snr
    }?.inRangeOrNull(SignalLte.SNR_RANGE)?.nullIf { it == 0.0 } // Samsung uses 0.0 as N/A value

    val cqi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        cqi
    } else {
        Reflection.intFieldOrNull(Reflection.LTE_CQI, this)
    }?.inRangeOrNull(SignalLte.CQI_RANGE)

    val ta = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        timingAdvance
    } else {
        Reflection.intFieldOrNull(Reflection.LTE_TA, this)
    }?.inRangeOrNull(SignalLte.TIMING_ADVANCE_RANGE)

    return SignalLte(
        rssi = rssi,
        rsrp = rsrp,
        rsrq = rsrq,
        cqi = cqi,
        snr = snr,
        timingAdvance = ta
    )
}

/**
 * [CellIdentityLte] -> [Network]
 */
@Suppress("DEPRECATION")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
internal fun CellIdentityLte.mapNetwork(): Network? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Network.map(mccString, mncString)
    } else {
        Network.map(mcc, mnc)
    }

@Suppress("DEPRECATION")
internal fun GsmCellLocation.mapLte(subId: Int, signalStrength: SignalStrength?, network: Network?): ICell? {
    val ci = cid.inRangeOrNull(CellLte.CID_RANGE)
    val tac = lac.inRangeOrNull(CellLte.TAC_RANGE)

    val signal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        signalStrength?.getCellSignalStrengths(CellSignalStrengthLte::class.java)
            ?.firstOrNull()
            ?.mapSignal() ?: SignalLte(null, null, null, null, null, null)
    } else {
        val signalMain = Reflection.intFieldOrNull(Reflection.SS_LTE_RSSI, signalStrength)
        val signalGsm = signalStrength?.gsmSignalStrength

        // Some devices do report signal strength on LTE as GSM signal strength
        val rssi =
            (if (signalGsm != null && signalGsm in RSSI_ASU_RANGE && (signalMain == null || signalMain !in RSSI_ASU_RANGE)) {
                signalGsm.toDbm()
            } else if (signalMain != null && signalMain in RSSI_ASU_RANGE) {
                signalMain.toDbm()
            } else {
                signalMain
            })?.inRangeOrNull(SignalLte.RSSI_RANGE)

        val rsrp = Reflection.intFieldOrNull(Reflection.SS_LTE_RSRP, signalStrength)?.toDouble()
            ?.inRangeOrNull(SignalLte.RSRP_RANGE)

        val rsrq = Reflection.intFieldOrNull(Reflection.SS_LTE_RSRQ, signalStrength)?.toDouble()
            ?.inRangeOrNull(SignalLte.RSRQ_RANGE)

        val snr = Reflection.intFieldOrNull(Reflection.SS_LTE_SNR, signalStrength)
            ?.let {
                // SNR in range from 0 to 3 means basically no signal and occurs rarely on Android devices
                // On older devices (ASUS_Z00AD) this value has 1 decimal place
                var snr = it.toDouble()
                if (snr > 30) {
                    snr /= 10
                }

                snr
            }?.inRangeOrNull(SignalLte.SNR_RANGE)?.nullIf { it == 0.0 } // Samsung uses 0.0 as N/A value

        val cqi = Reflection.intFieldOrNull(Reflection.SS_LTE_CQI, signalStrength)
            ?.inRangeOrNull(SignalLte.CQI_RANGE)

        SignalLte(
            rssi = rssi,
            rsrp = rsrp,
            rsrq = rsrq,
            cqi = cqi,
            snr = snr,
            timingAdvance = null,
        )
    }

    return if (ci != null) {
        CellLte(
            network = network,
            eci = ci,
            tac = tac,
            pci = null,
            band = null,
            bandwidth = null,
            signal = signal,
            connectionStatus = PrimaryConnection(),
            subscriptionId = subId,
            timestamp = null,
            aggregatedBands = emptyList(),
        )
    } else null
}