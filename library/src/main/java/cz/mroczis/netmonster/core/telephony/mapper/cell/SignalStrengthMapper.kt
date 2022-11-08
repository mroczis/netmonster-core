package cz.mroczis.netmonster.core.telephony.mapper.cell

import android.os.Build
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.SignalStrength
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection

/**
 * Attempts to detect NR in NSA mode. Requires valid LTE and NR signal.
 * Returns NR cell and primary LTE cell (only signal)
 */
internal fun SignalStrength.toCells(subscriptionId: Int): List<ICell> =
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
            val mappedSignalNr = nrSignal[0].mapSignal()
            val mappedSignalLte = lteSignal[0].mapSignal()

            val cellNr = CellNr(
                network = null,
                nci = null,
                tac = null,
                pci = null,
                band = null,
                signal = mappedSignalNr,
                connectionStatus = SecondaryConnection(isGuess = false),
                subscriptionId = subscriptionId,
                timestamp = timestamp
            )

            val cellLte = CellLte(
                network = null,
                eci = null,
                tac = null,
                pci = null,
                band = null,
                aggregatedBands = emptyList(),
                bandwidth = null,
                signal = mappedSignalLte,
                connectionStatus = PrimaryConnection(),
                subscriptionId = subscriptionId,
                timestamp = timestamp,
            )

            listOf(cellNr, cellLte)
        } else emptyList()
    } else emptyList()
