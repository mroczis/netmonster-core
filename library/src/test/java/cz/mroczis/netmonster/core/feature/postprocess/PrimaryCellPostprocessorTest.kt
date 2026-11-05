package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.model.signal.SignalWcdma
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class PrimaryCellPostprocessorTest : FreeSpec({

    val postprocessor = PrimaryCellPostprocessor()

    "Emergency calls; LTE" {
        val cells = mutableListOf<ICell>().apply {
            add(CellLte(
                network = Network.map("23001"),
                eci = 218155868,
                tac = 29510,
                pci = 345,
                band = null,
                bandwidth = null,
                signal = SignalLte(-51, -87.0, null, null, null, null),
                connectionStatus = NoneConnection(),
                subscriptionId = 0,
                timestamp = null,
                aggregatedBands = emptyList(),
            ))

            add(CellLte(
                network = null,
                eci = null,
                tac = null,
                pci = 346,
                band = null,
                bandwidth = null,
                signal = SignalLte(-63, -101.0, null, null, null, null),
                connectionStatus = NoneConnection(),
                subscriptionId = 0,
                timestamp = null,
                aggregatedBands = emptyList(),
            ))
        }

        val result = postprocessor.postprocess(cells)
        result.size shouldBe 2
        result.filter { it.connectionStatus is PrimaryConnection }.size shouldBe 1
        result.filter { it.connectionStatus is NoneConnection }.size shouldBe 1
    }

    "Emergency calls; WCDMA" {
        val cells = mutableListOf<ICell>().apply {
            add(CellWcdma(
                network = Network.map("23001"),
                ci = 218155868,
                lac = 29510,
                psc = 345,
                band = null,
                signal = SignalWcdma(-51, null, null, null, null),
                connectionStatus = NoneConnection(),
                subscriptionId = 0,
                timestamp = null,
            ))

            add(CellWcdma(
                network = null,
                ci = null,
                lac = null,
                psc = 346,
                band = null,
                signal = SignalWcdma(-51, null, null, null, null),
                connectionStatus = NoneConnection(),
                subscriptionId = 0,
                timestamp = null,
            ))
        }

        val result = postprocessor.postprocess(cells)
        result.size shouldBe 2
        result.filter { it.connectionStatus is PrimaryConnection }.size shouldBe 1
        result.filter { it.connectionStatus is NoneConnection }.size shouldBe 1
    }

    "Dual SIM; one SIM in service, the other out of service camped on a cell reported as a neighbour" {
        fun lte(eci: Int?, tac: Int?, pci: Int, connection: IConnection, subscriptionId: Int) = CellLte(
            network = Network.map("23001"),
            eci = eci,
            tac = tac,
            pci = pci,
            band = null,
            bandwidth = null,
            signal = SignalLte(-63, -101.0, null, null, null, null),
            connectionStatus = connection,
            subscriptionId = subscriptionId,
            timestamp = null,
            aggregatedBands = emptyList(),
        )

        val cells = listOf<ICell>(
            lte(eci = 218155868, tac = 29510, pci = 345, connection = PrimaryConnection(), subscriptionId = 1),
            lte(eci = null, tac = null, pci = 346, connection = NoneConnection(), subscriptionId = 1),
            lte(eci = null, tac = null, pci = 12, connection = NoneConnection(), subscriptionId = 2),
            lte(eci = 218155999, tac = 29510, pci = 13, connection = NoneConnection(), subscriptionId = 2),
        )

        val result = postprocessor.postprocess(cells)
        result.size shouldBe 4
        result.filter { it.connectionStatus is PrimaryConnection }.map { (it as CellLte).pci } shouldBe listOf(345, 13)
        // The cell of the second SIM is the first of its cells
        result.map { (it as CellLte).pci } shouldBe listOf(345, 346, 13, 12)
    }

})
