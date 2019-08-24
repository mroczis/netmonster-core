package cz.mroczis.netmonster.core.model.config

import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class PhysicalChannelConfigTest : FreeSpec() {

    init {

        "Parsing, Android Q" {
            // Source - Pixel 3 XL, Android Q beta 5

            val primaryRaw = "{mConnectionStatus=PrimaryServing,mCellBandwidthDownlinkKhz=10000,mRat=0,mFrequencyRange=-1,mChannelNumber=2147483647,mContextIds=[I@2c4d2e6,mPhysicalCellId=2147483647}"
            val secondaryRaw = "{mConnectionStatus=SecondaryServing,mCellBandwidthDownlinkKhz=10000,mRat=0,mFrequencyRange=-1,mChannelNumber=2147483647,mContextIds=[I@9f41027,mPhysicalCellId=2147483647}"

            PhysicalChannelConfig.fromString(primaryRaw).apply {
                connectionStatus shouldBe PrimaryConnection()
                bandwidth shouldBe 10_000
                channelNumber shouldBe null
                pci shouldBe null
            }

            PhysicalChannelConfig.fromString(secondaryRaw).apply {
                connectionStatus shouldBe SecondaryConnection(isGuess = false)
                bandwidth shouldBe 10_000
                channelNumber shouldBe null
                pci shouldBe null
            }

        }


        "Parsing, Android P" {
            // Source - ASUS Zenfone ASUS_Z01RD (ZS620KL), Android P

            val primaryRaw = "{mConnectionStatus=PrimaryServing,mCellBandwidthDownlinkKhz=10000}"
            val secondaryRaw = "{mConnectionStatus=SecondaryServing,mCellBandwidthDownlinkKhz=20000}"

            PhysicalChannelConfig.fromString(primaryRaw).apply {
                connectionStatus shouldBe PrimaryConnection()
                bandwidth shouldBe 10_000
                channelNumber shouldBe null
                pci shouldBe null
            }

            PhysicalChannelConfig.fromString(secondaryRaw).apply {
                connectionStatus shouldBe SecondaryConnection(isGuess = false)
                bandwidth shouldBe 20_000
                channelNumber shouldBe null
                pci shouldBe null
            }


        }
    }

}