package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellGsm
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalGsm
import cz.mroczis.netmonster.core.model.signal.SignalLte
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class PlmnPostprocessorTest : FreeSpec({

    val postprocessor = PlmnPostprocessor()

    "LTE neighbour" {
        val cells = mutableListOf<ICell>().apply {
            add(
                CellLte(
                    network = Network.map("23001"),
                    eci = 218155868,
                    tac = 29510,
                    pci = 345,
                    band = null,
                    bandwidth = null,
                    signal = SignalLte(-51, -87.0, null, null, null, null),
                    connectionStatus = NoneConnection(),
                    subscriptionId = 0
                )
            )

            add(
                CellLte(
                    network = null,
                    eci = null,
                    tac = null,
                    pci = 346,
                    band = null,
                    bandwidth = null,
                    signal = SignalLte(-63, -101.0, null, null, null, null),
                    connectionStatus = NoneConnection(),
                    subscriptionId = 0
                )
            )
        }

        val result = postprocessor.postprocess(cells)
            .filterIsInstance(CellLte::class.java)
        result.size shouldBe 2

        val plmns = result.map { it.network }.distinct()
        plmns.size shouldBe 1
        plmns[0] shouldBe Network.map("23001")
    }

    "GSM using LAC" {
        val cells = mutableListOf<ICell>().apply {
            // Primary cells - 2 providers
            add(
                CellGsm(
                    network = Network.map("23001"),
                    cid = 1,
                    lac = 1,
                    bsic = 1,
                    band = null,
                    signal = SignalGsm(-51, null, null),
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 0
                )
            )

            add(
                CellGsm(
                    network = Network.map("23002"),
                    cid = 1000,
                    lac = 1000,
                    bsic = 55,
                    band = null,
                    signal = SignalGsm(-51, null, null),
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 0
                )
            )

            // Neighbouring cells - 2 different LACs
            add(
                CellGsm(
                    network = null,
                    cid = 2,
                    lac = 1,
                    bsic = 2,
                    band = null,
                    signal = SignalGsm(-51, null, null),
                    connectionStatus = NoneConnection(),
                    subscriptionId = 0
                )
            )

            add(
                CellGsm(
                    network = null,
                    cid = 3,
                    lac = 2,
                    bsic = 2,
                    band = null,
                    signal = SignalGsm(-51, null, null),
                    connectionStatus = NoneConnection(),
                    subscriptionId = 0
                )
            )
        }

        val result = postprocessor.postprocess(cells)
            .filterIsInstance(CellGsm::class.java)
        result.size shouldBe 4

        result.filter { it.network == Network.map("23001") }.size shouldBe 2
        result.filter { it.network == Network.map("23001") && it.cid == 2 }.size shouldBe 1
        result.filter { it.network == Network.map("23002") }.size shouldBe 1
        result.filter { it.network == null }.size shouldBe 1

        val networks = result.map { it.network }.distinct()
        networks.distinct().size shouldBe 3 // 23001, 23002, null

    }

})