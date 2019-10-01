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
        }

        // Bands that overlap but have common name
        "Band 50,51,75,76 overlap" {
            BandTableNr.get(289_400).applyNonNull {
                name shouldBe "1500"
                number shouldBe null
            }
        }

        // Arfcn that does not follow 5 MHz blocks alignment
        "Band 50,51,75,76 overlap - not aligned" {
            BandTableNr.get(289_962) shouldBe null
        }
    }
}