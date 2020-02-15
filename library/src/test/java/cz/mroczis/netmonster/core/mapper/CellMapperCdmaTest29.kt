package cz.mroczis.netmonster.core.mapper

import android.os.Build
import android.telephony.CellIdentityCdma
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellSignalStrengthCdma
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.applyNonNull
import cz.mroczis.netmonster.core.model.cell.CellCdma
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapCell
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapConnection
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapSignal
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockkClass

/**
 * Testing conversion [CellInfoCdma] -> [CellCdma]
 */
class CellMapperCdmaTest29 : SdkTest(Build.VERSION_CODES.Q) {

    companion object {
        const val BID = 1234
        const val SID = 32767
        const val NID = 2
        const val LAT = 648_000
        const val LON = -957_600

        const val CDMA_RSSI = -79
        const val CDMA_ECIO = -92

        const val EVDO_RSSI = -93
        const val EVDO_ECIO = -123
        const val EVDO_SNR = 6
    }

    init {
        "Standard CDMA cell" {
            val cell = mockValidCell().let {
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())
            }

            cell.applyNonNull {
                sid shouldBe SID
                nid shouldBe NID
                bid shouldBe BID
                lat shouldBe 45.0
                lon shouldBe -66.5

                connectionStatus shouldBe PrimaryConnection()
                band shouldBe null

                signal.apply {
                    cdmaRssi shouldBe CDMA_RSSI
                    cdmaEcio shouldBe CDMA_ECIO / 10.0

                    evdoRssi shouldBe EVDO_RSSI
                    evdoEcio shouldBe EVDO_ECIO / 10.0
                    evdoSnr shouldBe EVDO_SNR

                    dbm shouldBe CDMA_RSSI
                }
            }
        }

        // samsung SM-J737V
        "Invalid CDMA cell" {
            mockValidCell().let {
                every { it.identity.networkId } returns 65535
                every { it.identity.systemId } returns 65535
                every { it.identity.basestationId } returns 0
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal()) shouldBe null
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun mockValidCell() : Wrapper {
        val id = mockkClass(CellIdentityCdma::class)
        every { id.systemId } returns SID
        every { id.basestationId } returns BID
        every { id.networkId } returns NID
        every { id.latitude } returns LAT
        every { id.longitude } returns LON

        val signal = mockkClass(CellSignalStrengthCdma::class)
        every { signal.cdmaDbm } returns CDMA_RSSI
        every { signal.cdmaEcio } returns CDMA_ECIO
        every { signal.evdoDbm } returns EVDO_RSSI
        every { signal.evdoEcio } returns EVDO_ECIO
        every { signal.evdoSnr } returns EVDO_SNR

        val info = mockkClass(CellInfoCdma::class)
        every { info.isRegistered } returns true
        every { info.cellConnectionStatus } returns CellInfo.CONNECTION_PRIMARY_SERVING
        every { info.cellIdentity } returns id
        every { info.cellSignalStrength } returns signal

        return Wrapper(id, signal, info)
    }

    private class Wrapper(
        val identity: CellIdentityCdma,
        val signal: CellSignalStrengthCdma,
        val info: CellInfoCdma
    )

}