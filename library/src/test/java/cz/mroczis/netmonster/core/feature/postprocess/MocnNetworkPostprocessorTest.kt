package cz.mroczis.netmonster.core.feature.postprocess

import android.telephony.ServiceState
import cz.mroczis.netmonster.core.SubscriptionId
import cz.mroczis.netmonster.core.db.BandTableLte
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.SubscribedNetwork
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.subscription.ISubscriptionManagerCompat
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class MocnNetworkPostprocessorTest : FreeSpec({
    val pairA = Network.map(222, 88)
    val pairB = Network.map(222, 50)

    val subManager = object : ISubscriptionManagerCompat {
        override fun getActiveSubscriptionIds(): List<Int> =
            getActiveSubscriptions().map { it.subscriptionId }

        override fun getActiveSubscriptions(): List<SubscribedNetwork> =
            listOf(
                SubscribedNetwork(0, 1, pairB),
                SubscribedNetwork(0, 2, pairA),
            )
    }

    val networkOperatorSimulation: (SubscriptionId) -> Network? = { subId ->
        when (subId) {
            1 -> pairB
            2 -> pairA
            else -> null
        }
    }

    val serviceStateSimulation: (Int) -> ServiceState = { ServiceState() }

    val postprocessor = MocnNetworkPostprocessor(
        subManager, networkOperatorSimulation, serviceStateSimulation
    )

    "Single SIM MOCN network, first PLMN != registered PLMN" {
        val cells = mutableListOf<ICell>().apply {
            add(
                CellLte(
                    network = pairA,
                    eci = 218155868,
                    tac = 29510,
                    pci = 345,
                    band = BandTableLte.map(1650),
                    bandwidth = null,
                    signal = SignalLte(-51, -87.0, null, null, null, null),
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 1,
                    timestamp = null,
                    aggregatedBands = emptyList(),
                )
            )
        }

        val result = postprocessor.postprocess(cells)

        val plmns = result.map { it.network }.distinct()
        plmns.size shouldBe 1
        plmns[0] shouldBe pairB
    }

    "Dual SIM MOCN network, same cell different PLMN" {
        val cells = mutableListOf<ICell>().apply {
            add(
                CellLte(
                    network = pairA,
                    eci = 218155868,
                    tac = 29510,
                    pci = 345,
                    band = BandTableLte.map(1650),
                    bandwidth = null,
                    signal = SignalLte(-51, -87.0, null, null, null, null),
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 1,
                    timestamp = null,
                    aggregatedBands = emptyList(),
                )
            )
            add(
                CellLte(
                    network = pairA,
                    eci = 218155868,
                    tac = 29510,
                    pci = 345,
                    band = BandTableLte.map(1650),
                    bandwidth = null,
                    signal = SignalLte(-51, -87.0, null, null, null, null),
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 2,
                    timestamp = null,
                    aggregatedBands = emptyList(),
                )
            )
        }

        val result = postprocessor.postprocess(cells)
        result.size shouldBe 2
        val plmns = result.map { it.network }.distinct()
        plmns.size shouldBe 2
        plmns shouldBe listOf(pairB, pairA)
    }
}
)