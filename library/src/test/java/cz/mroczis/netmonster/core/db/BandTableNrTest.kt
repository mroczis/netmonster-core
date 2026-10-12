package cz.mroczis.netmonster.core.db

import cz.mroczis.netmonster.core.applyNonNull
import cz.mroczis.netmonster.core.model.band.BandGsm
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

/**
 * Conversion from ARFCN to more complex [BandTableNr]
 */
class BandTableNrTest : FreeSpec() {

    init {

        // Tests that if blocks are properly aligned (most cases in Europe) then we can identify the band
        "Band 20,28 overlap" {
            BandTableNr.get(159_200).applyNonNull {
                name shouldBe "800"
                number shouldBe 20
            }

            BandTableNr.get(159_600).applyNonNull {
                name shouldBe "700"
                number shouldBe 28
            }

            BandTableNr.map(159_600).apply {
                name shouldBe "700"
                number shouldBe 28
                downlinkFrequency shouldBe 798_000 // kHz
                downlinkArfcn shouldBe 159_600

            }
        }

        // Multiple bands with different name that have nothing in common
        "Band 77,78 overlap" {
            BandTableNr.get(653_144) shouldBe null
            BandTableNr.get(632_000) shouldBe null
        }

        "Band 77,78 overlap in Europe -> n78" {
            BandTableNr.get(653_144, mcc = "200").applyNonNull {
                name shouldBe "3500"
                number shouldBe 78
            }

            BandTableNr.get(632_000, mcc = "200").applyNonNull {
                name shouldBe "3500"
                number shouldBe 78
            }
        }

        "Band 77,78 overlap in North America -> n77" {
            BandTableNr.get(653_144, mcc = "310").applyNonNull {
                name shouldBe "3700"
                number shouldBe 77
            }

            BandTableNr.get(632_000, mcc = "310").applyNonNull {
                name shouldBe "3700"
                number shouldBe 77
            }
        }

        // Bands that overlap but have common name
        "Band 50,51,75,76 overlap" {
            BandTableNr.get(289_400).applyNonNull {
                name shouldBe "1500"
                number shouldBe null
            }
        }

        // Arfcn that does not follow 5 MHz blocks alignment
        "Band 50,75,92,94 overlap - not aligned" {
            BandTableNr.get(289_962).applyNonNull {
                name shouldBe "1500"
                number shouldBe null
            }
        }

        "Band 50,75,92,94 overlap - not aligned, with hint" {
            BandTableNr.get(289_962, intArrayOf(75)).applyNonNull {
                name shouldBe "1500"
                number shouldBe 75
            }
        }

        "Band 7,38,41 overlap in USA -> n41, in Europe -> n7" {
            BandTableNr.get(529_000, mcc = "310").applyNonNull {
                number shouldBe 41
                scs shouldBe 30
            }

            BandTableNr.get(529_000, mcc = "230").applyNonNull {
                number shouldBe 7
                scs shouldBe 15
            }
        }

        "Band 77,78 overlap without MCC still knows SCS" {
            BandTableNr.map(632_000).apply {
                number shouldBe null
                expectedScs shouldBe 30
            }
        }

        "FR2 ranges and frequency" {
            BandTableNr.get(2_020_000, mcc = "230").applyNonNull {
                name shouldBe "26G"
                number shouldBe 258
            }

            BandTableNr.get(2_079_167, mcc = "310").applyNonNull {
                name shouldBe "28G"
            }

            BandTableNr.map(2_016_667).apply {
                downlinkFrequency shouldBe 24_250_080 // kHz
                expectedScs shouldBe 120
            }

            BandTableNr.get(2_000_000) shouldBe null
        }

        "Band 28 in India" {
            BandTableNr.get(152_600, mcc = "405").applyNonNull {
                number shouldBe 28
            }
        }
    }
}