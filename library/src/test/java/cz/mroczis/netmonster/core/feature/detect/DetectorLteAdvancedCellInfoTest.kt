package cz.mroczis.netmonster.core.feature.detect

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.db.BandTableLte
import cz.mroczis.netmonster.core.db.BandTableWcdma
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.model.signal.SignalWcdma
import io.kotlintest.shouldBe

class DetectorLteAdvancedCellInfoTest : SdkTest(Build.VERSION_CODES.N) {

    private val detector = DetectorLteAdvancedCellInfo()

    init {

        "Same band, same frequency" {
            val primary = CellLte(
                network = Network.map("21032"),
                eci = 345672,
                tac = 1,
                pci = 1,
                band = BandTableLte.map(6_200),
                bandwidth = 10_000,
                signal = SignalLte(null, null, null, null, null, null),
                connectionStatus = PrimaryConnection(),
                subscriptionId = 0
            )

            val secondary = primary.copy(
                eci = null,
                tac = null,
                pci = 2,
                connectionStatus = NoneConnection()
            )

            val cells = mutableListOf<ICell>().apply {
                add(primary)
                add(secondary)
            }

            detector.detect(cells) shouldBe null
        }


        "Same band, different frequency" {
            val primary = CellLte(
                network = Network.map("21032"),
                eci = 345672,
                tac = 1,
                pci = 1,
                band = BandTableLte.map(6_200),
                bandwidth = 10_000,
                signal = SignalLte(null, null, null, null, null, null),
                connectionStatus = PrimaryConnection(),
                subscriptionId = 0
            )

            val secondary = primary.copy(
                eci = null,
                tac = null,
                pci = 2,
                band = BandTableLte.map(6_300),
                bandwidth = 10_000,
                connectionStatus = NoneConnection(),
                subscriptionId = 0
            )

            val cells = mutableListOf<ICell>().apply {
                add(primary)
                add(secondary)
            }

            detector.detect(cells) shouldBe null
        }

        "Different band, different frequency" {
            val primary = CellLte(
                network = Network.map("21032"),
                eci = 345672,
                tac = 1,
                pci = 1,
                band = BandTableLte.map(6_200),
                bandwidth = 10_000,
                signal = SignalLte(null, null, null, null, null, null),
                connectionStatus = PrimaryConnection(),
                subscriptionId = 0
            )

            val secondary = primary.copy(
                eci = null,
                tac = null,
                pci = 2,
                band = BandTableLte.map(1_849),
                bandwidth = 15_000,
                connectionStatus = NoneConnection(),
                subscriptionId = 0
            )

            val cells = mutableListOf<ICell>().apply {
                add(primary)
                add(secondary)
            }

            detector.detect(cells) shouldBe NetworkTypeTable.get(NetworkType.LTE_CA)
        }

        "Different band, different frequency + invalid band" {
            val primary = CellLte(
                network = Network.map("21032"),
                eci = 345672,
                tac = 1,
                pci = 1,
                band = BandTableLte.map(6_200),
                bandwidth = 10_000,
                signal = SignalLte(null, null, null, null, null, null),
                connectionStatus = PrimaryConnection(),
                subscriptionId = 0
            )

            // Classic in Android N+
            val secondaryA = primary.copy(
                eci = null,
                tac = null,
                pci = 2,
                band = BandTableLte.map(1_849),
                bandwidth = 15_000,
                connectionStatus = NoneConnection()
            )

            // Classic in Android M
            val secondaryB = primary.copy(
                eci = null,
                tac = null,
                pci = 3,
                band = null,
                connectionStatus = NoneConnection()
            )

            val cells = mutableListOf<ICell>().apply {
                add(primary)
                add(secondaryA)
                add(secondaryB)
            }

            // Android M + Android N results SHOULD NOT clash cause user cannot run both versions on AOSP at once
            detector.detect(cells) shouldBe null
        }

        "Band, + invalid cell from M" {
            val primary = CellLte(
                network = Network.map("21032"),
                eci = 345672,
                tac = 1,
                pci = 1,
                band = BandTableLte.map(6_200),
                bandwidth = 10_000,
                signal = SignalLte(null, null, null, null, null, null),
                connectionStatus = PrimaryConnection(),
                subscriptionId = 0
            )

            val secondary = primary.copy(
                eci = null,
                tac = null,
                pci = 3,
                band = null,
                bandwidth = null,
                connectionStatus = NoneConnection()
            )

            val cells = mutableListOf<ICell>().apply {
                add(primary)
                add(secondary)
            }

            detector.detect(cells) shouldBe NetworkTypeTable.get(NetworkType.LTE_CA)
        }
    }

}