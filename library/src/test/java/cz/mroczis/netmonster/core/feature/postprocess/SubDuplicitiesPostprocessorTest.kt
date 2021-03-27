package cz.mroczis.netmonster.core.feature.postprocess

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.SubscribedNetwork
import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.subscription.ISubscriptionManagerCompat
import cz.mroczis.netmonster.core.util.SubscriptionModifier
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class SubDuplicitiesPostprocessorTest : SdkTest(Build.VERSION_CODES.P) {

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

        // Managers returning subscription A, B or AB
        val aManager = object : ISubscriptionManagerCompat {
            override fun getActiveSubscriptionIds(): List<Int> =
                getActiveSubscriptions().map { it.subscriptionId }

            override fun getActiveSubscriptions(): List<SubscribedNetwork> =
                listOf(aSubscription)
        }

        val abManager = object : ISubscriptionManagerCompat {
            override fun getActiveSubscriptionIds(): List<Int> =
                getActiveSubscriptions().map { it.subscriptionId }

            override fun getActiveSubscriptions(): List<SubscribedNetwork> =
                listOf(aSubscription, bSubscription)
        }

        "One subscription" {

            val postprocessor = SubDuplicitiesPostprocessor(aManager) { subId ->
                aManager.getActiveSubscriptions().first { it.subscriptionId == subId }.network
            }

            val cells = mutableListOf<ICell>(
                aServing1,
                aNeighbour1,
                aNeighbour2
            )

            // Should not affect result at all
            val result = postprocessor.postprocess(cells)
            result shouldBe cells
        }

        "Two subscriptions - dual sim till Android 10" {
            val postprocessor = SubDuplicitiesPostprocessor(abManager) { subId ->
                abManager.getActiveSubscriptions().first { it.subscriptionId == subId }.network
            }

            // This should be the result we want to obtain
            val cells = mutableListOf<ICell>(
                aServing1,
                aNeighbour1,
                aNeighbour2,
                bServing1,
                bNeighbour1
            )

            // Simulate RIL output -> 2 same lists with with different subscriptions ids
            val subTo1 = SubscriptionModifier(SUB_A)
            val subTo2 = SubscriptionModifier(SUB_B)
            val input = listOf(
                cells.map { it.let(subTo1) },
                cells.map { it.let(subTo2) }
            ).flatten()

            val result = postprocessor.postprocess(input)

            // We expect that code will filer out duplicities
            result shouldBe cells
        }

        "Two subscriptions - same PLMN" {
            val cManager = object : ISubscriptionManagerCompat {
                override fun getActiveSubscriptionIds(): List<Int> =
                    getActiveSubscriptions().map { it.subscriptionId }

                override fun getActiveSubscriptions(): List<SubscribedNetwork> =
                    listOf(
                        SubscribedNetwork(0, SUB_A, Network.map(800, 88)),
                        SubscribedNetwork(1, SUB_B, Network.map(800, 88))
                    )
            }

            val postprocessor = SubDuplicitiesPostprocessor(cManager) { subId ->
                cManager.getActiveSubscriptions().first { it.subscriptionId == subId }.network
            }

            val cells = mutableListOf<ICell>(
                aServing1,
                aNeighbour1,
                aNeighbour2,
                bServing1.copy(network = Network.map(800, 88)),
                bNeighbour1
            )

            val subTo1 = SubscriptionModifier(SUB_A)
            val subTo2 = SubscriptionModifier(SUB_B)
            val input = listOf(
                cells.map { it.let(subTo1) },
                cells.map { it.let(subTo2) }
            ).flatten()

            val result = postprocessor.postprocess(input)
            result shouldBe cells
        }

    }

}