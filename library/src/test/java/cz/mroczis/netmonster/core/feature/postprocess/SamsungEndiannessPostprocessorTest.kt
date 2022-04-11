package cz.mroczis.netmonster.core.feature.postprocess

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import io.kotlintest.shouldBe

class SamsungEndiannessPostprocessorTest: SdkTest(Build.VERSION_CODES.S) {

    private var enabled = false
    private val postprocessor = SamsungEndiannessPostprocessor(
        getCellSkeleton = { null },
        setEnabled = { enabled = true },
        isEnabled = { enabled }
    )

    init {
        "has flipped endianness" {
            postprocessor.hasFlippedEndianness(5220, 25620) shouldBe true
            postprocessor.hasFlippedEndianness(5221, 25620) shouldBe false
        }

        "flip endianness" {
            postprocessor.flipEndianness(5220) shouldBe 25620
            postprocessor.flipEndianness(25620) shouldBe 5220
        }
    }

}