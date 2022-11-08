package cz.mroczis.netmonster.core.feature.merge

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.db.BandTableLte
import cz.mroczis.netmonster.core.db.BandTableNr
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.model.signal.SignalNr
import io.kotlintest.shouldBe

class ComplexMergerTest : SdkTest(Build.VERSION_CODES.R) {

    private val registrationMerger = CellNetworkRegistrationMerger()
    private val signalMerger = CellSignalMerger()

    init {
        "Dual SIM, 1st SIM NR NSA, 2nd SIM LTE. LTE cells missing in new api for 1st SIM." {
            val newApi = listOf(
                CellNr(
                    connectionStatus = NoneConnection(),
                    subscriptionId = 1,
                    nci = null,
                    tac = null,
                    pci = 221,
                    band = BandTableNr.map(645312),
                    network = Network.map(222, 10),
                    signal = SignalNr(
                        csiRsrp = null,
                        csiRsrq = null,
                        csiSinr = null,
                        ssRsrp = null,
                        ssRsrq = null,
                        ssSinr = 18
                    ),
                    timestamp = null,
                ),
                CellLte(
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 2,
                    eci = 100001,
                    tac = 1005,
                    pci = 400,
                    band = BandTableLte.map(6200),
                    aggregatedBands = emptyList(),
                    bandwidth = null,
                    network = Network.map(222, 99),
                    signal = SignalLte(
                        rsrp = -90.0,
                        rsrq = -7.0,
                        rssi = -80,
                        snr = 10.0,
                        cqi = null,
                        timingAdvance = null
                    ),
                    timestamp = null,
                )
            )

            val registration = listOf(
                CellLte(
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 1,
                    eci = 2022412,
                    tac = 1001,
                    pci = 231,
                    band = BandTableLte.map(6400),
                    aggregatedBands = emptyList(),
                    bandwidth = null,
                    network = Network.map(222, 10),
                    signal = SignalLte.EMPTY,
                    timestamp = null
                ),
                CellLte(
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 2,
                    eci = 100001,
                    tac = 1005,
                    pci = 400,
                    band = BandTableLte.map(6200),
                    aggregatedBands = emptyList(),
                    bandwidth = null,
                    network = Network.map(222, 99),
                    signal = SignalLte.EMPTY,
                    timestamp = null
                ),
            )

            val signalApi = listOf(
                CellNr(
                    connectionStatus = SecondaryConnection(false),
                    subscriptionId = 1,
                    nci = null,
                    tac = null,
                    pci = null,
                    band = null,
                    network = null,
                    signal = SignalNr(
                        csiRsrp = null,
                        csiRsrq = null,
                        csiSinr = null,
                        ssRsrp = -101,
                        ssRsrq = -9,
                        ssSinr = 19
                    ),
                    timestamp = null,
                ),
                CellLte(
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 1,
                    eci = null,
                    tac = null,
                    pci = null,
                    band = null,
                    aggregatedBands = emptyList(),
                    bandwidth = null,
                    network = null,
                    signal = SignalLte(
                        rsrp = -83.0,
                        rsrq = -8.0,
                        rssi = -69,
                        snr = 6.0,
                        cqi = 12,
                        timingAdvance = 3
                    ),
                    timestamp = null
                )
            )

            val registrationMerged = registrationMerger.merge(newApi, registration)
            registrationMerged.size shouldBe 3
            val signalMerged = signalMerger.merge(registrationMerged, signalApi)
            signalMerged.size shouldBe 3

            val sims = signalMerged.groupBy { it.subscriptionId }
            // Two sims
            sims.size shouldBe 2
            sims[1]?.let {
                it.size shouldBe 2
                (it[0] as CellNr).apply {
                    pci shouldBe 221
                    band shouldBe BandTableNr.map(645312)
                    network shouldBe Network.map(222, 10)
                    signal.csiRsrp shouldBe null
                    signal.csiRsrq shouldBe null
                    signal.csiSinr shouldBe null
                    signal.ssRsrp shouldBe -101
                    signal.ssRsrq shouldBe -9
                    signal.ssSinr shouldBe 18
                    connectionStatus shouldBe SecondaryConnection(false)
                }
                (it[1] as CellLte).apply {
                    eci shouldBe 2022412
                    pci shouldBe 231
                    band shouldBe BandTableLte.map(6400)
                    network shouldBe Network.map(222, 10)
                    signal.rsrp shouldBe -83.0
                    signal.rsrq shouldBe -8.0
                    signal.snr shouldBe 6.0
                    signal.cqi shouldBe 12
                    signal.timingAdvance shouldBe 3
                    connectionStatus shouldBe PrimaryConnection()
                }
            }
            sims[2]?.let {
                it.size shouldBe 1
                // CellLte must be equal to the one from new api
                (it[0] as CellLte) shouldBe newApi[1]
            }
        }

        "Dual SIM, 1st SIM LTE, 2nd SIM NR NSA. NR cells missing in new api." {
            val newApi = listOf(
                CellLte(
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 1,
                    eci = 200001,
                    tac = 2005,
                    pci = 200,
                    band = BandTableLte.map(1850),
                    aggregatedBands = emptyList(),
                    bandwidth = null,
                    network = Network.map(222, 10),
                    signal = SignalLte(
                        rsrp = -101.0,
                        rsrq = -16.0,
                        rssi = -84,
                        snr = 3.2,
                        cqi = 5,
                        timingAdvance = 7
                    ),
                    timestamp = null
                ),
                CellLte(
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 2,
                    eci = 506279,
                    tac = 1111,
                    pci = 200,
                    band = BandTableLte.map(1650),
                    aggregatedBands = emptyList(),
                    bandwidth = null,
                    network = Network.map(222, 50),
                    signal = SignalLte(
                        rsrp = -90.0,
                        rsrq = -7.0,
                        rssi = -80,
                        snr = 10.0,
                        cqi = null,
                        timingAdvance = null
                    ),
                    timestamp = null
                )
            )

            val registration = listOf(
                CellLte(
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 1,
                    eci = 200001,
                    tac = 2005,
                    pci = 200,
                    band = BandTableLte.map(1850),
                    aggregatedBands = emptyList(),
                    bandwidth = null,
                    network = Network.map(222, 10),
                    signal = SignalLte.EMPTY,
                    timestamp = null
                ),
                CellLte(
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 2,
                    eci = 506279,
                    tac = 1111,
                    pci = 200,
                    band = BandTableLte.map(1650),
                    aggregatedBands = emptyList(),
                    bandwidth = null,
                    network = Network.map(222, 50),
                    signal = SignalLte.EMPTY,
                    timestamp = null
                ),
            )

            val signalApi = listOf(
                CellNr(
                    connectionStatus = SecondaryConnection(false),
                    subscriptionId = 2,
                    nci = null,
                    tac = null,
                    pci = null,
                    band = null,
                    network = null,
                    signal = SignalNr(
                        csiRsrp = null,
                        csiRsrq = null,
                        csiSinr = null,
                        ssRsrp = -130,
                        ssRsrq = -5,
                        ssSinr = 28
                    ),
                    timestamp = null,
                ),
                CellLte(
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 2,
                    eci = null,
                    tac = null,
                    pci = null,
                    band = null,
                    aggregatedBands = emptyList(),
                    bandwidth = null,
                    network = null,
                    signal = SignalLte(
                        rsrp = -83.0,
                        rsrq = -8.0,
                        rssi = -69,
                        snr = 6.0,
                        cqi = 12,
                        timingAdvance = 3
                    ),
                    timestamp = null
                )
            )

            val registrationMerged = registrationMerger.merge(newApi, registration)
            // Registration must not add any lte cell
            registrationMerged.size shouldBe 2

            val signalMerged = signalMerger.merge(registrationMerged, signalApi)
            signalMerged.size shouldBe 3

            val sims = signalMerged.groupBy { it.subscriptionId }
            // Two sims
            sims.size shouldBe 2
            sims[1]?.let {
                it.size shouldBe 1
                // CellLte must be equal to the one from new api
                (it[0] as CellLte) shouldBe newApi[0]
            }
            sims[2]?.let {
                it.size shouldBe 2
                // CellLte must be equal to the one from new api
                (it[0] as CellLte) shouldBe newApi[1]

                // CellNr must be equal to the one from signal api
                (it[1] as CellNr) shouldBe signalApi[0]
            }
        }
    }
}