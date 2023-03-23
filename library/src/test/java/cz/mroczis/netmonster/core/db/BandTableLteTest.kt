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

        "EARFCN overflow band fix" {
            BandTableLte.map(1_275, mcc = null).number shouldBe 3
            BandTableLte.map(1_275, mcc = "230").number shouldBe 3
            BandTableLte.map(1_275, mcc = "302").number shouldBe 66
            BandTableLte.map(3_050, mcc = "310").number shouldBe 71
            BandTableLte.map(1_275, mcc = "302").number shouldBe 7
        }
    }
}
