package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellCdma
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalCdma
import cz.mroczis.netmonster.core.model.signal.SignalLte
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class CdmaPlmnPostProcessorTest : FreeSpec({

    val postprocessor = CdmaPlmnPostprocessor()

    val plmn = Network.map("23001")

    val lte23001 = CellLte(
        network = plmn,
        eci = 218155868,
        tac = 29510,
        pci = 345,
        band = null,
        bandwidth = null,
        signal = SignalLte(-51, -87.0, null, null, null, null),
        connectionStatus = PrimaryConnection(),
        subscriptionId = 0
    )

    val lte23001neighbour = CellLte(
        network = plmn,
        eci = null,
        tac = null,
        pci = 2,
        band = null,
        bandwidth = null,
        signal = SignalLte(-51, -87.0, null, null, null, null),
        connectionStatus = NoneConnection(),
        subscriptionId = 0
    )

    val lte23011 = CellLte(
        network = Network.map("23011"),
        eci = 218155868,
        tac = 29510,
        pci = 345,
        band = null,
        bandwidth = null,
        signal = SignalLte(-51, -87.0, null, null, null, null),
        connectionStatus = PrimaryConnection(),
        subscriptionId = 0
    )

    val cdmaWithoutPlmn = CellCdma(
        network = null,
        nid = 1,
        bid = 2,
        sid = 3,
        lat = 1.0,
        lon = 2.0,
        signal = SignalCdma(null, null, null, null, null),
        connectionStatus = PrimaryConnection(),
        subscriptionId = 0
    )

    val cdmaWithPlmn = cdmaWithoutPlmn.copy(network = plmn)

    "Just CDMA cell and neighbour" {
        val cells = listOf(lte23001neighbour, cdmaWithoutPlmn)
        val result = postprocessor.postprocess(cells)
        result shouldBe cells // No change cause there is just one neighbouring cell
    }

    "Serving LTE + neighbouring LTE, one CDMA" {
        val cells = listOf(lte23001, lte23001neighbour, cdmaWithoutPlmn)
        val result = postprocessor.postprocess(cells)
        result shouldBe listOf(lte23001, lte23001neighbour, cdmaWithPlmn)
    }

    "Two serving cells with different PLMN" {
        val cells = listOf(lte23001, lte23011, lte23001neighbour, cdmaWithoutPlmn)
        val result = postprocessor.postprocess(cells)
        result shouldBe cells // No change cause there are two candidates
    }
})