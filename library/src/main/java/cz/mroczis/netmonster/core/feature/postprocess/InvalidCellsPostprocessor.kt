package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.util.isHuawei
import cz.mroczis.netmonster.core.util.isMediatek
import cz.mroczis.netmonster.core.util.isSamsung

/**
 * Some cells seem to pass all validations and act like they are correct but based on
 * several observations we know that those data are certainly invalid.
 * We will filter them out here.
 */
class InvalidCellsPostprocessor : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> = list.toMutableList().asSequence()
        // Google Pixel phones in LTE use this to say that cell is nearby but it's data will appear in a near future
        .filterNot { it is CellLte && it.pci == 0 && it.signal.rssi == -51 && it.signal.rsrp == null && it.signal.rsrq == null }
        // Google Pixel phones in GSM when they are loosing signal return GSM cells just with 0 ARFCN and 0 BSIC
        .filterNot { it is CellGsm && it.bsic == 0 && it.band?.arfcn == 0 && it.signal.rssi == null && it.cid == null }
        // Samsung phones on LTE return invalid neighbouring cells. Those are probably badly marked WCDMA / GSM cells
        // The problem is that each phone acts differently; this generalized rule is based on:
        // - a7y18ltexx, API 28
        // - beyond1lteeea, API 29
        .filterNot { it is CellLte && isSamsung() && it.band?.number == 1 && it.connectionStatus !is PrimaryConnection && it.signal.rsrp != null && it.signal.rsrq == null }
        // Mediatek devices return these fake NR (primary) cells, in NSA mode, most likely when they attempt to connect to an NR cell or have just disconnected.
        // The only valid values are PCI, PLMN and NR-ARFCN, NCI is 0 or 268435455 (filtered out by CellMapperNR), RSRP -44 and RSRQ -3
        .filterNot { it is CellNr && isMediatek() && it.nci == null && it.signal.ssRsrp == -44 && it.signal.ssRsrq == -3 && it.signal.csiRsrp == -44 && it.signal.csiRsrq == -3 }
        // Pixel 7 (Pro) when searching for a network whilst on VoWiFi
        .filterNot { it is CellWcdma && it.cid == null && it.psc == 0 && it.signal.rssi == -113 }
        .toList()

}