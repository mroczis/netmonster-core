package cz.mroczis.netmonster.core.feature.detect

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.db.BandTableWcdma
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalWcdma
import io.kotlintest.shouldBe

class DetectorHspaDcTest : SdkTest(Build.VERSION_CODES.N) {

    private val detector = DetectorHspaDc()

    init {

        "Same band, different frequency" {
            val primary = CellWcdma(
                network = Network.map("21032"),
                ci = 345672,
                lac = 1,
                psc = 1,
                band = BandTableWcdma.map(10_838),
                signal = SignalWcdma(null, null, null, null, null),
                connectionStatus = PrimaryConnection(),
                subscriptionId = 0
            )

            val secondary = CellWcdma(
                network = null,
                ci = null,
                lac = null,
                psc = 1,
                band = BandTableWcdma.map(10_588),
                signal = SignalWcdma(null, null, null, null, null),
                connectionStatus = NoneConnection(),
                subscriptionId = 0
            )

            val cells = mutableListOf<ICell>().apply {
                add(primary)
                add(secondary)
            }

            detector.detect(cells) shouldBe NetworkTypeTable.get(NetworkType.HSPA_DC)

        }


        "Different band, different frequency" {
            val primary = CellWcdma(
                network = Network.map("21032"),
                ci = 345672,
                lac = 1,
                psc = 1,
                band = BandTableWcdma.map(2_937),
                signal = SignalWcdma(null, null, null, null, null),
                connectionStatus = PrimaryConnection(),
                subscriptionId = 0
            )

            val secondary = CellWcdma(
                network = null,
                ci = null,
                lac = null,
                psc = 1,
                band = BandTableWcdma.map(10_588),
                signal = SignalWcdma(null, null, null, null, null),
                connectionStatus = NoneConnection(),
                subscriptionId = 0
            )

            val cells = mutableListOf<ICell>().apply {
                add(primary)
                add(secondary)
            }

            detector.detect(cells) shouldBe null

        }

        "Same band, same frequency" {
            val primary = CellWcdma(
                network = Network.map("21032"),
                ci = 345672,
                lac = 1,
                psc = 1,
                band = BandTableWcdma.map(2_937),
                signal = SignalWcdma(null, null, null, null, null),
                connectionStatus = PrimaryConnection(),
                subscriptionId = 0
            )

            val secondary = primary.copy(
                network = null,
                ci = null,
                lac = null,
                connectionStatus = NoneConnection()
            )

            val cells = mutableListOf<ICell>().apply {
                add(primary)
                add(secondary)
            }

            detector.detect(cells) shouldBe null

        }
    }

}