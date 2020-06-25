package cz.mroczis.netmonster.core.feature.postprocess

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.db.BandTableLte
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.config.PhysicalChannelConfig
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte
import io.kotlintest.shouldBe

class PhysicalChannelPostprocessorTest : SdkTest(Build.VERSION_CODES.Q) {

    companion object {
        private val SUB_ID = 1
        private val LTE_SIGNAL = SignalLte(null, null, null, null, null, null)
    }

    init {

        "Same PCIs twice, non-transitive" {
            val pccProvider: (Int) -> List<PhysicalChannelConfig> = { _ ->
                listOf(
                    PhysicalChannelConfig(connectionStatus = PrimaryConnection(), bandwidth = 20_000, channelNumber = null, pci = 149),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 20_000, channelNumber = null, pci = 149)
                )
            }

            val cells = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 149, band = BandTableLte.map(2950), bandwidth = 20_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 149, band = BandTableLte.map(1275), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 224, band = BandTableLte.map(1275), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 63, band = BandTableLte.map(2950), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            val result = PhysicalChannelPostprocessor(pccProvider).postprocess(cells)

            val expected = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 149, band = BandTableLte.map(2950), bandwidth = 20_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = SecondaryConnection(isGuess = false), eci = null, tac = null, pci = 149, band = BandTableLte.map(1275), bandwidth = 20_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 224, band = BandTableLte.map(1275), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 63, band = BandTableLte.map(2950), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            result shouldBe expected
        }

        "Same PCI twice, transitive through EARFCN" {
            val pccProvider: (Int) -> List<PhysicalChannelConfig> = { _ ->
                listOf(
                    PhysicalChannelConfig(connectionStatus = PrimaryConnection(), bandwidth = 20_000, channelNumber = null, pci = 149),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 10_000, channelNumber = null, pci = 149),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 20_000, channelNumber = null, pci = 63)
                )
            }

            val cells = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 149, band = BandTableLte.map(2950), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 149, band = BandTableLte.map(1275), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 224, band = BandTableLte.map(1275), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 63, band = BandTableLte.map(2950), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            val result = PhysicalChannelPostprocessor(pccProvider).postprocess(cells)

            val expected = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 149, band = BandTableLte.map(2950), bandwidth = 20_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = SecondaryConnection(isGuess = false), eci = null, tac = null, pci = 149, band = BandTableLte.map(1275), bandwidth = 10_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 224, band = BandTableLte.map(1275), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 63, band = BandTableLte.map(2950), bandwidth = 20_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            result shouldBe expected
        }

        "No PCI" {
            val pccProvider: (Int) -> List<PhysicalChannelConfig> = { _ ->
                listOf(
                    PhysicalChannelConfig(connectionStatus = PrimaryConnection(), bandwidth = 20_000, channelNumber = null, pci = null),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 10_000, channelNumber = null, pci = null)
                )
            }

            val cells = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 149, band = BandTableLte.map(2950), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 149, band = BandTableLte.map(1275), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 224, band = BandTableLte.map(1275), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 63, band = BandTableLte.map(2950), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            val result = PhysicalChannelPostprocessor(pccProvider).postprocess(cells)

            val expected = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 149, band = BandTableLte.map(2950), bandwidth = 20_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 149, band = BandTableLte.map(1275), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 224, band = BandTableLte.map(1275), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 63, band = BandTableLte.map(2950), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            result shouldBe expected
        }

        "Do not mark as secondarily serving when primary has same EARFCN" {
            val pccProvider: (Int) -> List<PhysicalChannelConfig> = { _ ->
                listOf(
                    PhysicalChannelConfig(connectionStatus = PrimaryConnection(), bandwidth = 20_000, channelNumber = null, pci = 149),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 10_000, channelNumber = null, pci = 63)
                )
            }

            val cells = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 149, band = BandTableLte.map(2950), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 63, band = BandTableLte.map(2950), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            val result = PhysicalChannelPostprocessor(pccProvider).postprocess(cells)

            val expected = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 149, band = BandTableLte.map(2950), bandwidth = 20_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 63, band = BandTableLte.map(2950), bandwidth = 10_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            result shouldBe expected
        }

        "One neighbour + two configs -> same PCI, no channel" {
            val pccProvider: (Int) -> List<PhysicalChannelConfig> = { _ ->
                listOf(
                    PhysicalChannelConfig(connectionStatus = PrimaryConnection(), bandwidth = 10_000, channelNumber = null, pci = 118),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 10_000, channelNumber = null, pci = 94),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 10_000, channelNumber = null, pci = 94)
                )
            }

            val cells = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 118, band = BandTableLte.map(6200), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 94, band = BandTableLte.map(251), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 105, band = BandTableLte.map(3000), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            val result = PhysicalChannelPostprocessor(pccProvider).postprocess(cells)

            val expected = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 118, band = BandTableLte.map(6200), bandwidth = 10_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = SecondaryConnection(isGuess = false), eci = null, tac = null, pci = 94, band = BandTableLte.map(251), bandwidth = 10_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 105, band = BandTableLte.map(3000), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            result shouldBe expected

        }

        "Two neighbours + two configs -> same PCI, no channel" {
            val pccProvider: (Int) -> List<PhysicalChannelConfig> = { _ ->
                listOf(
                    PhysicalChannelConfig(connectionStatus = PrimaryConnection(), bandwidth = 10_000, channelNumber = null, pci = 118),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 10_000, channelNumber = null, pci = 94),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 10_000, channelNumber = null, pci = 94)
                )
            }

            val cells = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 118, band = BandTableLte.map(6200), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 94, band = BandTableLte.map(251), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 94, band = BandTableLte.map(3000), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 105, band = BandTableLte.map(3000), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            val result = PhysicalChannelPostprocessor(pccProvider).postprocess(cells)

            val expected = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 118, band = BandTableLte.map(6200), bandwidth = 10_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = SecondaryConnection(isGuess = false), eci = null, tac = null, pci = 94, band = BandTableLte.map(251), bandwidth = 10_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = SecondaryConnection(isGuess = false), eci = null, tac = null, pci = 94, band = BandTableLte.map(3000), bandwidth = 10_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 105, band = BandTableLte.map(3000), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            result shouldBe expected

        }

        "Three neighbours + two configs -> same PCI, no channel" {
            val pccProvider: (Int) -> List<PhysicalChannelConfig> = { _ ->
                listOf(
                    PhysicalChannelConfig(connectionStatus = PrimaryConnection(), bandwidth = 10_000, channelNumber = null, pci = 118),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 10_000, channelNumber = null, pci = 94),
                    PhysicalChannelConfig(connectionStatus = SecondaryConnection(isGuess = false), bandwidth = 10_000, channelNumber = null, pci = 94)
                )
            }

            val cells = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 118, band = BandTableLte.map(6200), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 94, band = BandTableLte.map(251), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 94, band = BandTableLte.map(3000), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 94, band = BandTableLte.map(1579), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 105, band = BandTableLte.map(3000), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            val result = PhysicalChannelPostprocessor(pccProvider).postprocess(cells)

            // Do not merge cause we have 2 possible configs but 3 candidates
            val expected = listOf(
                CellLte(connectionStatus = PrimaryConnection(), eci = 1, tac = 1, pci = 118, band = BandTableLte.map(6200), bandwidth = 10_000, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 94, band = BandTableLte.map(251), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 94, band = BandTableLte.map(3000), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 94, band = BandTableLte.map(1579), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null),
                CellLte(connectionStatus = NoneConnection(), eci = null, tac = null, pci = 105, band = BandTableLte.map(3000), bandwidth = null, signal = LTE_SIGNAL, subscriptionId = SUB_ID, network = null)
            )

            result shouldBe expected

        }
    }

}