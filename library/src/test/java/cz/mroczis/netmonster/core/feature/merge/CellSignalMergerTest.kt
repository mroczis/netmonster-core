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
                    ssSinr = 18
                )
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
                    ssSinr = 19
                )
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
    }

}