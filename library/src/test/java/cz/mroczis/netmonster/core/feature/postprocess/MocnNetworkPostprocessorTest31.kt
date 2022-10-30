package cz.mroczis.netmonster.core.feature.postprocess

import android.os.Build
import android.telephony.CellIdentity
import android.telephony.CellIdentityLte
import android.telephony.NetworkRegistrationInfo
import android.telephony.ServiceState
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.db.BandTableLte
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.SubscribedNetwork
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.subscription.ISubscriptionManagerCompat
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockkClass

class MocnNetworkPostprocessorTest31 : SdkTest(Build.VERSION_CODES.R) {
    companion object {
        const val CI = 218155868
        const val TAC = 29510
        const val PCI = 345
        const val EARFCN = 1650
        const val MCC = 222
        const val MNC = 88
        const val MNC2 = 50
        const val RSSI = -81
        const val RSRP = -112
    }

    init {
        val pairA = Network.map(MCC, MNC)
        val pairB = Network.map(MCC, MNC2)

        val subManager = object : ISubscriptionManagerCompat {
            override fun getActiveSubscriptionIds(): List<Int> =
                getActiveSubscriptions().map { it.subscriptionId }

            override fun getActiveSubscriptions(): List<SubscribedNetwork> =
                listOf(
                    SubscribedNetwork(0, 1, pairB),
                    SubscribedNetwork(0, 2, pairA),
                )
        }

        val networkOperatorSimulation: (Int) -> Network? = { null }

        /* At least one device, Sony 5 III XQ-BQ52 reports registrationinfo.cellidentity
         * with the same (incorrect) PLMN as ALL_CELL_INFO.
         * So we use this scenario in this test
         */
        val serviceStateSimulation: (Int) -> ServiceState = { subId ->
            object : ServiceState() {
                override fun getNetworkRegistrationInfoList(): MutableList<NetworkRegistrationInfo> {
                    return mutableListOf(
                        mockRegistrationInfo(
                            when (subId) {
                                1 -> pairB
                                2 -> pairA
                                else -> null
                            }?.toPlmn(),
                            mockCellIdentity()
                        )
                    )
                }
            }
        }

        val postprocessor = MocnNetworkPostprocessor(
            subManager, networkOperatorSimulation, serviceStateSimulation
        )

        "Single SIM MOCN network, first PLMN != registered PLMN" {
            val cells = mutableListOf<ICell>().apply {
                add(
                    CellLte(
                        network = pairA,
                        eci = CI,
                        tac = TAC,
                        pci = PCI,
                        band = BandTableLte.map(EARFCN),
                        bandwidth = null,
                        signal = SignalLte(RSSI, RSRP.toDouble(), null, null, null, null),
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
                        eci = CI,
                        tac = TAC,
                        pci = PCI,
                        band = BandTableLte.map(EARFCN),
                        bandwidth = null,
                        signal = SignalLte(RSSI, RSRP.toDouble(), null, null, null, null),
                        connectionStatus = PrimaryConnection(),
                        subscriptionId = 1,
                        timestamp = null,
                        aggregatedBands = emptyList(),
                    )
                )
                add(
                    CellLte(
                        network = pairA,
                        eci = CI,
                        tac = TAC,
                        pci = PCI,
                        band = BandTableLte.map(EARFCN),
                        bandwidth = null,
                        signal = SignalLte(RSSI, RSRP.toDouble(), null, null, null, null),
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

    @Suppress("DEPRECATION")
    private fun mockRegistrationInfo(
        registeredPlmn: String?,
        cellIdentity: CellIdentity?
    ): NetworkRegistrationInfo {
        return mockkClass(NetworkRegistrationInfo::class).also {
            every { it.registeredPlmn } returns registeredPlmn
            every { it.cellIdentity } returns cellIdentity
        }
    }

    @Suppress("DEPRECATION")
    private fun mockCellIdentity(): CellIdentity {
        return mockkClass(CellIdentityLte::class).also {
            every { it.ci } returns CI
            every { it.tac } returns TAC
            every { it.pci } returns PCI
            every { it.earfcn } returns EARFCN
            every { it.bands } returns intArrayOf(20)
            every { it.mcc } returns MCC
            every { it.mccString } returns MCC.toString()
            every { it.mnc } returns MNC
            every { it.mncString } returns MNC.toString()
        }
    }
}