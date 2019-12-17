package cz.mroczis.netmonster.sample.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import cz.mroczis.netmonster.core.model.cell.*

class CellView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
    }

    private val transformer = object : ICellProcessor<Unit> {

        override fun processLte(cell: CellLte) {
            cell.network?.let { addView("NET", "LTE ${it.mcc} ${it.mnc} (${it.iso}) ${cell.connectionStatus.javaClass.simpleName}") }
            cell.band?.let { band ->
                band.channelNumber.let { addView("FREQ", "$it (#${band.number}, ${band.name})") }
            }

            cell.eci?.let { addView("ECI", it) }
            cell.enb?.let { addView("eNb", it) }
            cell.cid?.let { addView("CID", it) }
            cell.tac?.let { addView("TAC", it) }
            cell.pci?.let { addView("PCI", it) }
            cell.bandwidth?.let { addView("BW", it) }

            cell.signal.let { signal ->
                signal.rssi?.let { addView("RSSI", it) }
                signal.rsrp?.let { addView("RSRP", it) }
                signal.rsrq?.let { addView("RSRQ", it) }
                signal.cqi?.let { addView("CQI", it) }
                signal.timingAdvance?.let { addView("TA", it) }
                signal.snr?.let { addView("SNR", it) }
            }
        }

        override fun processCdma(cell: CellCdma) {
            addView("SID", cell.sid)
            cell.nid?.let { addView("NID", it) }
            cell.bid?.let { addView("BID", it) }
            cell.lat?.let { addView("LAT", it) }
            cell.lon?.let { addView("LON", it) }

            cell.signal.let { signal ->
                signal.cdmaEcio?.let { addView("CD EC/IO", it) }
                signal.cdmaRssi?.let { addView("CD RSSI", it) }
                signal.evdoEcio?.let { addView("EV EC/IO", it) }
                signal.evdoRssi?.let { addView("EV RSSI", it) }
                signal.evdoSnr?.let { addView("EV SNR", it) }
            }
        }

        override fun processGsm(cell: CellGsm) {
            cell.network?.let { addView("NET", "GSM ${it.mcc} ${it.mnc} (${it.iso}) ${cell.connectionStatus.javaClass.simpleName}") }
            cell.band?.let { band ->
                band.channelNumber.let { addView("FREQ", "$it (#${band.number}, ${band.name})") }
            }

            cell.cid?.let { addView("CID", it) }
            cell.lac?.let { addView("LAC", it) }
            cell.bsic?.let { addView("BSIC", it) }

            cell.signal.let { signal ->
                signal.rssi?.let { addView("RSSI", it) }
                signal.bitErrorRate?.let { addView("BER", it) }
                signal.timingAdvance?.let { addView("TA", it) }
            }
        }

        override fun processNr(cell: CellNr) {
            cell.network?.let { addView("NET", "NR ${it.mcc} ${it.mnc} (${it.iso}) ${cell.connectionStatus.javaClass.simpleName}") }
            cell.band?.let { band ->
                band.channelNumber.let { addView("FREQ", "$it (#${band.number}, ${band.name})") }
            }

            cell.nci?.let { addView("NCI", it) }
            cell.tac?.let { addView("TAC", it) }
            cell.pci?.let { addView("PCI", it) }

            cell.signal.let { signal ->
                signal.csiRsrp?.let { addView("CSI RSRP", it) }
                signal.csiRsrq?.let { addView("CSI RSRQ", it) }
                signal.csiSinr?.let { addView("CSI SINR", it) }
                signal.ssRsrp?.let { addView("SS RSRP", it) }
                signal.ssRsrq?.let { addView("SS RSRQ", it) }
                signal.ssSinr?.let { addView("SS SINR", it) }
            }
        }

        override fun processTdscdma(cell: CellTdscdma) {
            cell.network?.let { addView("NET", "TDS-CDMA ${it.mcc} ${it.mnc} (${it.iso}) ${cell.connectionStatus.javaClass.simpleName}") }
            cell.band?.let { band ->
                band.channelNumber.let { addView("FREQ", "$it (#${band.number}, ${band.name})") }
            }

            cell.ci?.let { addView("CI", it) }
            cell.rnc?.let { addView("RNC", it) }
            cell.cid?.let { addView("CID", it) }
            cell.lac?.let { addView("LAC", it) }
            cell.cpid?.let { addView("CPID", it) }

            cell.signal.let { signal ->
                signal.rssi?.let { addView("RSSI", it) }
                signal.bitErrorRate?.let { addView("BER", it) }
                signal.rscp?.let { addView("RSCP", it) }
            }
        }

        override fun processWcdma(cell: CellWcdma) {
            cell.network?.let { addView("NET", "WCDMA ${it.mcc} ${it.mnc} (${it.iso}) ${cell.connectionStatus.javaClass.simpleName}") }
            cell.band?.let { band ->
                band.channelNumber.let { addView("FREQ", "$it (#${band.number}, ${band.name})") }
            }

            cell.ci?.let { addView("CI", it) }
            cell.rnc?.let { addView("RNC", it) }
            cell.cid?.let { addView("CID", it) }
            cell.lac?.let { addView("LAC", it) }
            cell.psc?.let { addView("PSC", it) }

            cell.signal.let { signal ->
                signal.rssi?.let { addView("RSSI", it) }
                signal.bitErrorRate?.let { addView("BER", it) }
                signal.rscp?.let { addView("RSCP", it) }
                signal.ecio?.let { addView("ECIO", it) }
                signal.ecno?.let { addView("ECNO", it) }
            }
        }

    }

    fun bind (cell: ICell) {
        removeAllViews()
        cell.let(transformer)
    }

    private fun addView(title: String, message: Any) {
        val view = CellItemSimple(context).apply {
            bind(title, message.toString())
        }

        addView(view)
    }
}