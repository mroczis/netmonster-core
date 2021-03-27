package cz.mroczis.netmonster.core.feature

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.db.BandTableGsm
import cz.mroczis.netmonster.core.db.BandTableLte
import cz.mroczis.netmonster.core.db.BandTableWcdma
import cz.mroczis.netmonster.core.feature.postprocess.*
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.SubscribedNetwork
import cz.mroczis.netmonster.core.model.cell.CellGsm
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalGsm
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.model.signal.SignalWcdma
import cz.mroczis.netmonster.core.subscription.ISubscriptionManagerCompat
import cz.mroczis.netmonster.core.util.SubscriptionModifier
import io.kotlintest.shouldBe

class ComplexPostprocessingTests : SdkTest(Build.VERSION_CODES.P) {

    init {
        "Different MNC for same network provider" {
            // Happens for example in Sri Lanka, Italy
            // ServiceState & CellInfo returns one type of pair
            // SubscriptionInfo & getNetworkType return another one

            val pairA = Network.map(413, 8)
            val pairB = Network.map(413, 9)

            val subManager = object : ISubscriptionManagerCompat {
                override fun getActiveSubscriptionIds(): List<Int> =
                    getActiveSubscriptions().map { it.subscriptionId }

                override fun getActiveSubscriptions(): List<SubscribedNetwork> =
                    listOf(
                        SubscribedNetwork(0,1, Network.map(413, 2)),
                        SubscribedNetwork(1,2, pairA)
                    )
            }

            val serviceStateSimulation: (Int) -> Network? = { subId ->
                when (subId) {
                    1 -> Network.map(413, 2)
                    2 -> pairB
                    else -> null
                }
            }

            val postprocessors = listOf(
                MocnNetworkPostprocessor(subManager, serviceStateSimulation),
                InvalidCellsPostprocessor(),
                PrimaryCellPostprocessor(),
                SubDuplicitiesPostprocessor(subManager, serviceStateSimulation),
                PlmnPostprocessor()
            )

            val cells = mutableListOf<ICell>().apply {
                add(
                    CellLte(
                        network = Network.map(413, 2),
                        eci = 46223,
                        tac = 3453,
                        pci = 235,
                        band = BandTableLte.map(1650),
                        signal = SignalLte(
                            rssi = -92,
                            rsrp = -108.0,
                            rsrq = -12.0,
                            snr = null,
                            cqi = null,
                            timingAdvance = null
                        ),
                        bandwidth = null,
                        subscriptionId = 1,
                        connectionStatus = PrimaryConnection()
                    )
                )

                add(
                    CellWcdma(
                        network = pairB,
                        ci = 523423,
                        lac = 23241,
                        psc = 23,
                        band = BandTableWcdma.map(10663),
                        signal = SignalWcdma(
                            rssi = -97,
                            bitErrorRate = null,
                            ecio = null,
                            ecno = null,
                            rscp = null
                        ),
                        subscriptionId = 2,
                        connectionStatus = PrimaryConnection()
                    )
                )

                add(
                    CellWcdma(
                        network = null,
                        ci = null,
                        lac = null,
                        psc = 0,
                        band = null,
                        signal = SignalWcdma(
                            rssi = -99,
                            bitErrorRate = null,
                            ecio = null,
                            ecno = null,
                            rscp = null
                        ),
                        subscriptionId = 2,
                        connectionStatus = NoneConnection()
                    )
                )

            }

            var res = cells.toList()
            postprocessors.forEach { res = it.postprocess(res) }
            res.size shouldBe 3
            // In this case postprocessing does not remove anything
        }


        "Dual SIM, Android 5.1 - 9, both having neighbouring cells, different technologies" {
            val cells = mutableListOf<ICell>().apply {
                add(
                    CellGsm(
                        network = Network.map(203, 2),
                        cid = 4234,
                        lac = 1231,
                        bsic = 9,
                        band = BandTableGsm.map(114, "230"),
                        signal = SignalGsm(
                            rssi = -81,
                            bitErrorRate = null,
                            timingAdvance = null
                        ),
                        subscriptionId = 1,
                        connectionStatus = PrimaryConnection()
                    )
                )

                add(
                    CellGsm(
                        network = Network.map(203, 2),
                        cid = 4673,
                        lac = 1231,
                        bsic = 8,
                        band = BandTableGsm.map(46, "230"),
                        signal = SignalGsm(
                            rssi = -93,
                            bitErrorRate = null,
                            timingAdvance = null
                        ),
                        subscriptionId = 1,
                        connectionStatus = NoneConnection()
                    )
                )

                add(
                    CellLte(
                        network = Network.map(203, 1),
                        eci = 213733608,
                        tac = 29230,
                        pci = 333,
                        band = BandTableLte.map(6200),
                        signal = SignalLte(
                            rssi = -77,
                            rsrp = -101.0,
                            rsrq = -6.0,
                            snr = null,
                            cqi = null,
                            timingAdvance = null
                        ),
                        bandwidth = null,
                        subscriptionId = 3,
                        connectionStatus = PrimaryConnection()
                    )
                )

                add(
                    CellLte(
                        network = Network.map(null, null),
                        eci = null,
                        tac = null,
                        pci = 334,
                        band = null,
                        signal = SignalLte(
                            rssi = -91,
                            rsrp = -116.0,
                            rsrq = -16.0,
                            snr = null,
                            cqi = null,
                            timingAdvance = null
                        ),
                        bandwidth = null,
                        subscriptionId = 3,
                        connectionStatus = NoneConnection()
                    )
                )

                add(
                    CellGsm(
                        network = Network.map(null, null),
                        cid = null,
                        lac = null,
                        bsic = 255,
                        band = BandTableGsm.map(0, "230"),
                        signal = SignalGsm(
                            rssi = -113,
                            bitErrorRate = null,
                            timingAdvance = null
                        ),
                        subscriptionId = 3,
                        connectionStatus = NoneConnection()
                    )
                )
            }

            val subManager = object : ISubscriptionManagerCompat {
                override fun getActiveSubscriptionIds(): List<Int> =
                    getActiveSubscriptions().map { it.subscriptionId }

                override fun getActiveSubscriptions(): List<SubscribedNetwork> =
                    listOf(
                        SubscribedNetwork(0,1, Network.map(203, 2)),
                        SubscribedNetwork(1, 3, Network.map(203, 1))
                    )
            }

            val serviceStateSimulation: (Int) -> Network? = { subId ->
                subManager.getActiveSubscriptions().first { it.subscriptionId == subId }.network
            }

            val postprocessors = listOf(
                MocnNetworkPostprocessor(subManager, serviceStateSimulation),
                InvalidCellsPostprocessor(),
                PrimaryCellPostprocessor(),
                SubDuplicitiesPostprocessor(subManager, serviceStateSimulation),
                PlmnPostprocessor()
            )

            val sub1 = cells.map { it.let(SubscriptionModifier(1)) }
            val sub3 = cells.map { it.let(SubscriptionModifier(3)) }

            var res = listOf(sub1, sub3).flatten()
            postprocessors.forEach { res = it.postprocess(res) }

            // Postprocessors should be able to identify and separate subscription ids properly
            res.size shouldBe 5
            res.groupBy { it.subscriptionId }.apply {
                size shouldBe 2
                get(1)!!.size shouldBe 2
                get(3)!!.size shouldBe 3
            }
        }
    }
}