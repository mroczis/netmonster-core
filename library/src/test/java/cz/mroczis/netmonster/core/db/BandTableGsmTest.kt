package cz.mroczis.netmonster.core.db

import cz.mroczis.netmonster.core.applyNonNull
import cz.mroczis.netmonster.core.model.band.BandGsm
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

/**
 * Conversion from ARFCN to more complex [BandGsm]
 */
class BandTableGsmTest : FreeSpec() {

    init {
        "GSM 900" {
            BandTableGsm.get(0, "230").applyNonNull {
                name shouldBe "900"
                number shouldBe 900
            }

            BandTableGsm.map(0, "230").apply {
                arfcn shouldBe 0
                channelNumber shouldBe 0
                name shouldBe "900"
                number shouldBe 900
            }
        }

        "DCS / PCS ambiguity" {
            // DCS-only ARFCN
            811.let { arfcn ->
                // Czechia
                BandTableGsm.get(arfcn, "230").applyNonNull {
                    name shouldBe "1800"
                    number shouldBe 1800
                }

                // USA
                BandTableGsm.get(arfcn, "310").applyNonNull {
                    name shouldBe "1800"
                    number shouldBe 1800
                }
            }


            // PCS/DCS ARFCN
            600.let { arfcn ->
                // Czechia -> DCS
                BandTableGsm.get(arfcn, "230").applyNonNull {
                    name shouldBe "1800"
                    number shouldBe 1800
                }

                // USA -> PCS
                BandTableGsm.get(arfcn, "310").applyNonNull {
                    name shouldBe "1900"
                    number shouldBe 1900
                }

                // British Virgin Islands -> Can be both
                BandTableGsm.get(arfcn, "348").applyNonNull {
                    name shouldBe "1800/1900"
                    number shouldBe BandTableGsm.NUMBER_UNKNOWN
                }

                // Costa Rica -> DCS
                BandTableGsm.get(arfcn, "348").applyNonNull {
                    name shouldBe "1800/1900"
                    number shouldBe BandTableGsm.NUMBER_UNKNOWN
                }

                // Non-existing country in South America -> PCS
                BandTableGsm.get(arfcn, "777").applyNonNull {
                    name shouldBe "1900"
                    number shouldBe 1900
                }
            }
        }

    }

}