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

    "Xiaomi 15 / HyperOS 4 (Qualcomm); NR NSA without NCI, PCI+TAC copied from the anchor" {
        // Captured on HyperOS 4.0.4.0 / Android 17: CellIdentityNr reported the LTE
        // anchor's PCI (420, TAC 55383) with nci and PLMN unavailable, while the
        // modem's own QMI GET_SYS_INFO carried the real nR PCI (776) on n41.
        val lteAnchor = lte.copy(pci = 420, tac = 55383, eci = 59515700)
        val nrNoIdentity = nr(nci = null, tac = 55383).copy(pci = 420)

        val result = postprocessor.postprocess(listOf(lteAnchor, nrNoIdentity))
        val cell = result.filterIsInstance<CellNr>().single()
        cell.pci shouldBe null
        cell.tac shouldBe null
        cell.nci shouldBe null
        cell.connectionStatus shouldBe SecondaryConnection(isGuess = false)
        result.filterIsInstance<CellLte>().single() shouldBe lteAnchor
    }

    "NR without NCI but its own PCI is kept" {
        val lteAnchor = lte.copy(pci = 420, tac = 55383, eci = 59515700)
        val nrOwnPci = nr(nci = null, tac = 55383).copy(pci = 776)

        postprocessor.postprocess(listOf(lteAnchor, nrOwnPci)) shouldBe listOf(lteAnchor, nrOwnPci)
    }

    "NR without NCI and same PCI but different TAC is kept" {
        val lteAnchor = lte.copy(pci = 420, tac = 55383, eci = 59515700)
        val nrOtherTac = nr(nci = null, tac = 11111).copy(pci = 420)

        postprocessor.postprocess(listOf(lteAnchor, nrOtherTac)) shouldBe listOf(lteAnchor, nrOtherTac)
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
