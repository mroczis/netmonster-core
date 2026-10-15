package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.db.BandTableNr
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.band.BandNr
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.model.signal.SignalNr
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class NrBandPostprocessorTest : FreeSpec({

    val postprocessor = NrBandPostprocessor()

    fun lte(subId: Int = 1) = CellLte(
        network = Network.map("23002"),
        eci = 234637414,
        tac = 1826,
        pci = 282,
        band = null,
        bandwidth = 15000,
        signal = SignalLte(-91, -100.0, -10.0, null, 10.0, 15),
        connectionStatus = PrimaryConnection(),
        subscriptionId = subId,
        timestamp = null,
        aggregatedBands = emptyList(),
    )

    fun nr(band: BandNr, network: Network? = null, subId: Int = 1) = CellNr(
        network = network,
        nci = null,
        tac = null,
        pci = 108,
        band = band,
        signal = SignalNr(null, null, null, -89, -12, 7, 16),
        connectionStatus = NoneConnection(),
        subscriptionId = subId,
        timestamp = null,
    )

    "Neighbour hinted n77 gets n78 by MCC of serving LTE" {
        val neighbour = nr(BandTableNr.map(644_640, intArrayOf(77)))
        neighbour.band?.number shouldBe 77

        val serving = lte()
        val result = postprocessor.postprocess(listOf(serving, neighbour))
        val cell = result.filterIsInstance<CellNr>().single()
        cell.band?.number shouldBe 78
        cell.network shouldBe null
        result.first() shouldBe serving
    }

    "Neighbour ambiguous without MCC gets band by MCC of serving LTE" {
        val neighbour = nr(BandTableNr.map(632_000))
        neighbour.band?.number shouldBe null

        val cell = postprocessor.postprocess(listOf(lte(), neighbour)).filterIsInstance<CellNr>().single()
        cell.band?.number shouldBe 78
    }

    "Hint that does not contradict region is kept" {
        val neighbour = nr(BandTableNr.map(289_962, intArrayOf(75)))
        val cell = postprocessor.postprocess(listOf(lte(), neighbour)).filterIsInstance<CellNr>().single()
        cell.band?.number shouldBe 75
    }

    "Serving cell on other subscription is not used" {
        val serving = lte()
        val neighbour = nr(BandTableNr.map(644_640, intArrayOf(77)), subId = 2)
        postprocessor.postprocess(listOf(serving, neighbour)) shouldBe listOf(serving, neighbour)
    }

    "NR with own PLMN is kept" {
        val serving = lte()
        val nr = nr(BandTableNr.map(644_640, intArrayOf(77)), network = Network.map("23002"))
        postprocessor.postprocess(listOf(serving, nr)) shouldBe listOf(serving, nr)
    }
})
