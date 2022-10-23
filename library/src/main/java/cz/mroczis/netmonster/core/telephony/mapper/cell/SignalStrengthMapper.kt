package cz.mroczis.netmonster.core.telephony.mapper.cell

import android.os.Build
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.SignalStrength
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection

/**
 * Attempts to detect NR in NSA mode. Requires valid LTE and NR signal
 */
internal fun SignalStrength.toCells(subscriptionId: Int): List<CellNr> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val lteSignal = getCellSignalStrengths(CellSignalStrengthLte::class.java)
        val nrSignal = getCellSignalStrengths(CellSignalStrengthNr::class.java)

        val timestamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            timestampMillis
        } else {
            null
        }

        if (lteSignal.isNotEmpty() && nrSignal.isNotEmpty()) {
            // When we have LTE & NR signal then this could be NR in NSA
            val mappedSignal = nrSignal[0].mapSignal()
            val cell = CellNr(
                network = null,
                nci = null,
                tac = null,
                pci = null,
                band = null,
                signal = mappedSignal,
                connectionStatus = SecondaryConnection(isGuess = false),
                subscriptionId = subscriptionId,
                timestamp = timestamp
            )

            listOf(cell)
        } else emptyList()
    } else emptyList()
