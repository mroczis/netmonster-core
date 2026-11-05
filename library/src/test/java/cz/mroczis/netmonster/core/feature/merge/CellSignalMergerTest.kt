package cz.mroczis.netmonster.core.feature.merge

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.db.BandTableNr
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.signal.SignalNr
import io.kotlintest.shouldBe

class CellSignalMergerTest : SdkTest(Build.VERSION_CODES.Q) {

    private val merger = CellSignalMerger()

    init {
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
                    ssSinr = 18,
                    timingAdvance = null,
                ),
                timestamp = null,
            )
        )

        val signalApi = listOf(
            CellNr(
                connectionStatus = NoneConnection(),
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
                    ssSinr = 19,
                    timingAdvance = null,
                ),
                timestamp = null,
            )
        )

        val merged = merger.merge(newApi, signalApi)
        merged.size shouldBe 1
        (merged[0] as CellNr).apply {
            pci shouldBe 221
            band shouldBe BandTableNr.map(645312)
            network shouldBe Network.map(222, 10)
            signal.csiRsrp shouldBe null
            signal.csiRsrq shouldBe null
            signal.csiSinr shouldBe null
            signal.ssRsrp shouldBe -101
            signal.ssRsrq shouldBe -9
            signal.ssSinr shouldBe 18
        }

        // Two SIMs on NR, the signal belongs to the second one - the first SIM's cell must stay as it is
        fun nr(subscriptionId: Int, pci: Int?, ssRsrp: Int?) = CellNr(
            connectionStatus = NoneConnection(),
            subscriptionId = subscriptionId,
            nci = null,
            tac = null,
            pci = pci,
            band = if (pci != null) BandTableNr.map(645312) else null,
            network = null,
            signal = SignalNr(
                csiRsrp = null,
                csiRsrq = null,
                csiSinr = null,
                ssRsrp = ssRsrp,
                ssRsrq = null,
                ssSinr = null,
                timingAdvance = null,
            ),
            timestamp = null,
        )

        val dualSim = merger.merge(
            newApi = listOf(nr(subscriptionId = 1, pci = 100, ssRsrp = -90), nr(subscriptionId = 2, pci = 200, ssRsrp = null)),
            signalApi = listOf(nr(subscriptionId = 2, pci = null, ssRsrp = -105)),
        ).map { it as CellNr }

        dualSim.size shouldBe 2
        dualSim.single { it.subscriptionId == 1 }.apply {
            pci shouldBe 100
            signal.ssRsrp shouldBe -90
        }
        dualSim.single { it.subscriptionId == 2 }.apply {
            pci shouldBe 200
            signal.ssRsrp shouldBe -105
        }
    }

}