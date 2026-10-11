package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.model.signal.SignalNr
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class NrIdentityFromLtePostprocessorTest : FreeSpec({

    val postprocessor = NrIdentityFromLtePostprocessor()

    val lte = CellLte(
        network = Network.map("23002"),
        eci = 234637414,
        tac = 1826,
        pci = 282,
        band = null,
        bandwidth = 15000,
        signal = SignalLte(-91, -100.0, -10.0, null, 10.0, 15),
        connectionStatus = PrimaryConnection(),
        subscriptionId = 1,
        timestamp = null,
        aggregatedBands = emptyList(),
    )

    fun nr(nci: Long?, tac: Int?, subId: Int = 1) = CellNr(
        network = Network.map("23002"),
        nci = nci,
        tac = tac,
        pci = 108,
        band = null,
        signal = SignalNr(null, null, null, -89, -12, 7, 16),
        connectionStatus = PrimaryConnection(),
        subscriptionId = subId,
        timestamp = null,
    )

    "Xiaomi; NR NSA with LTE identity" {
        val result = postprocessor.postprocess(listOf(lte, nr(234637414, 1826)))
        val cell = result.filterIsInstance<CellNr>().single()
        cell.nci shouldBe null
        cell.tac shouldBe 1826
        cell.connectionStatus shouldBe SecondaryConnection(isGuess = false)
        result.filterIsInstance<CellLte>().single() shouldBe lte
    }

    "NR with different identity is kept" {
        val nr = nr(15016772067, 1826)
        postprocessor.postprocess(listOf(lte, nr)) shouldBe listOf(lte, nr)
    }

    "NR on other subscription is kept" {
        val nr = nr(234637414, 1826, subId = 2)
        postprocessor.postprocess(listOf(lte, nr)).contains(nr) shouldBe true
    }
})
