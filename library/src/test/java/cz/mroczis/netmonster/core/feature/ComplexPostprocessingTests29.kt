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
import io.kotlintest.specs.StringSpec

class ComplexPostprocessingTests29 : SdkTest(Build.VERSION_CODES.Q) {

    init {
        "Dual SIM, Android 10, both having neighbouring cells, different technologies, same newtwork" {
            val cells = mutableListOf<ICell>().apply {
                add(
                    CellGsm(
                        network = Network.map(203, 3),
                        cid = 4234,
                        lac = 1231,
                        bsic = 9,
                        band = BandTableGsm.map(114, "203"),
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
                        network = Network.map(203, 3),
                        cid = 4673,
                        lac = 1231,
                        bsic = 8,
                        band = BandTableGsm.map(46, "203"),
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
                        network = Network.map(203, 3),
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
                        network = Network.map(203, 3),
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
                        network = Network.map(203, 3),
                        cid = null,
                        lac = null,
                        bsic = 255,
                        band = BandTableGsm.map(0, "203"),
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
                        SubscribedNetwork(0,1, Network.map(203, 3)),
                        SubscribedNetwork(1,3, Network.map(203, 3))
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

            var res: List<ICell> = cells
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