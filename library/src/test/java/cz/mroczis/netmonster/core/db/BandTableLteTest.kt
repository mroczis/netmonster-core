package cz.mroczis.netmonster.core.db

import cz.mroczis.netmonster.core.applyNonNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

/**
 * Conversion from EARFCN to more complex [BandTableLte]
 */
class BandTableLteTest : FreeSpec() {

    init {

        "European LTE 800 DD" {
            BandTableLte.map(6_200).applyNonNull {
                downlinkEarfcn shouldBe 6_200
                number shouldBe 20
                name shouldBe "800"
            }
        }
    }
}