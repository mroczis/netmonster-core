package cz.mroczis.netmonster.core.feature.postprocess

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.db.BandTableWcdma
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalWcdma
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class SignalStrengthPostprocessorTest : SdkTest(Build.VERSION_CODES.Q){

    init {
        
        "WCDMA field" {
            // Source device OnePlus GM1900 (OnePlus7) /  API 29

            val cells = listOf(
                CellWcdma(
                    network = Network.map("80888"),
                    ci = 33333,
                    lac = 1,
                    psc = 1,
                    band = BandTableWcdma.map(2_937),
                    signal = SignalWcdma(-79, null, null, null, null),
                    connectionStatus = PrimaryConnection(),
                    subscriptionId = 0
                )
            )

            val pairedCell = CellWcdma(
                network = Network.map("80888"),
                ci = 33333,
                lac = 1,
                psc = null,
                band = null,
                signal = SignalWcdma(-81, null, -4, -92, null),
                connectionStatus = PrimaryConnection(),
                subscriptionId = 0
            )

            val result = SignalStrengthPostprocessor {
                pairedCell
            }.postprocess(cells)

            result.size shouldBe 1
            (result[0] as CellWcdma).signal shouldBe SignalWcdma(-79, null, -4, -92, null)

        }
    }
}