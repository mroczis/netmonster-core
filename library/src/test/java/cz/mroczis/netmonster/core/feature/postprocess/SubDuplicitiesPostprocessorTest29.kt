package cz.mroczis.netmonster.core.feature.postprocess

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.SubscribedNetwork
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.subscription.ISubscriptionManagerCompat
import cz.mroczis.netmonster.core.util.SubscriptionModifier
import io.kotlintest.shouldBe

class SubDuplicitiesPostprocessorTest29 : SdkTest(Build.VERSION_CODES.Q) {

    companion object {
        const val SUB_A = 4
        const val SUB_B = 20
    }

    init {
        // Cells for Subscription A
        val aServing1 = CellLte(
            network = Network.map("80088"),
            eci = 218155868,
            tac = 29510,
            pci = 345,
            band = null,
            bandwidth = null,
            signal = SignalLte(-51, -87.0, null, null, null, null),
            connectionStatus = PrimaryConnection(),
            subscriptionId = SUB_A
        )

        val aNeighbour1 = CellLte(
            network = null,
            eci = null,
            tac = null,
            pci = 346,
            band = null,
            bandwidth = null,
            signal = SignalLte(-63, -101.0, null, null, null, null),
            connectionStatus = NoneConnection(),
            subscriptionId = SUB_A
        )

        val aNeighbour2 = aNeighbour1.copy(pci = 424)

        // Cells for Subscription B
        val bServing1 = aServing1.copy(network = Network.map("70077"), subscriptionId = SUB_B)
        val bNeighbour1 = aNeighbour1.copy(pci = 234, subscriptionId = SUB_B)

        val aSubscription = SubscribedNetwork(0, SUB_A, Network.map(800, 88))
        val bSubscription = SubscribedNetwork(1, SUB_B, Network.map(700, 77))

        val abManager = object : ISubscriptionManagerCompat {
            override fun getActiveSubscriptionIds(): List<Int> =
                getActiveSubscriptions().map { it.subscriptionId }

            override fun getActiveSubscriptions(): List<SubscribedNetwork> =
                listOf(aSubscription, bSubscription)
        }


        "Two subscriptions - dual sim since Android 10" {
            // Android 10+ returns just cells that are bound to subscriptions
            // so assume that data are already filtered
            val postprocessor = SubDuplicitiesPostprocessor(abManager) { subId ->
                abManager.getActiveSubscriptions().first { it.subscriptionId == subId }.network
            }

            val cells = mutableListOf<ICell>(
                aServing1,
                aNeighbour1,
                aNeighbour2,
                bServing1,
                bNeighbour1
            )

            val result = postprocessor.postprocess(cells)
            result shouldBe cells
        }
    }


}