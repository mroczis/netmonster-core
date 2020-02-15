package cz.mroczis.netmonster.core.mapper

import android.os.Build
import android.telephony.*
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.applyNonNull
import cz.mroczis.netmonster.core.letNonNull
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellGsm
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalGsm
import cz.mroczis.netmonster.core.telephony.mapper.CellInfoMapper
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapCell
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapConnection
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapSignal
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockkClass

/**
 * Testing conversion [CellInfoWcdma] -> [CellWcdma]
 */
class CellMapperWcdmaTest29 : SdkTest(Build.VERSION_CODES.Q) {

    companion object {
        const val CID = 203757467
        const val LAC = 29410
        const val PSC = 42
        const val UARFCN = 10737
        const val MCC = 230
        const val MNC = 11

        const val BIT_ERROR = 3
        const val RSSI = -79
        const val RSCP = -71
        const val ECNO = -3
    }

    init {
        "Standard WCDMA cell" {
            val cell = mockValidCell().let {
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())
            }

            cell.applyNonNull {
                ci shouldBe CID
                cid shouldBe 6043
                rnc shouldBe 3109
                lac shouldBe LAC
                psc shouldBe PSC
                cgi shouldBe "230112941006043"
                network shouldBe Network.map(MCC, MNC)
                connectionStatus shouldBe PrimaryConnection()

                band.applyNonNull {
                    downlinkUarfcn shouldBe UARFCN
                    name shouldBe "2100"
                    number shouldBe 1
                }

                signal.apply {
                    bitErrorRate shouldBe BIT_ERROR
                    dbm shouldBe RSSI
                    rssi shouldBe RSSI
                    rssiAsu shouldBe 17
                    rscp shouldBe RSCP
                    rscpAsu shouldBe 49
                    ecno shouldBe ECNO
                    ecio shouldBe null
                }
            }
        }

        "Samsung neighbouring cell" {
            // Samsung phones report LAC = 0 and sequence od CIs from 1 (step 1) when data
            // are not valid. We are here to fix it!
            mockValidCell().let {
                every { it.identity.cid } returns 1
                every { it.identity.lac } returns 0
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal()).letNonNull {
                    it.ci shouldBe null
                    it.lac shouldBe null
                    it.psc shouldBe PSC
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun mockValidCell() : Wrapper {
        val id = mockkClass(CellIdentityWcdma::class)
        every { id.cid } returns CID
        every { id.lac } returns LAC
        every { id.psc } returns PSC
        every { id.uarfcn } returns UARFCN
        every { id.mcc } returns MCC
        every { id.mccString } returns MCC.toString()
        every { id.mnc } returns MNC
        every { id.mncString } returns MNC.toString()

        val signal = mockkClass(CellSignalStrengthWcdma::class)
        every { signal.dbm } returns RSSI
        every { signal.toString() } returns
                "CellSignalStrengthWcdma: ss=$RSSI ber=$BIT_ERROR rscp=$RSCP ecno=$ECNO level=2"

        val info = mockkClass(CellInfoWcdma::class)
        every { info.isRegistered } returns true
        every { info.cellConnectionStatus } returns CellInfo.CONNECTION_PRIMARY_SERVING
        every { info.cellIdentity } returns id
        every { info.cellSignalStrength } returns signal

        return Wrapper(id, signal, info)
    }

    private class Wrapper(
        val identity: CellIdentityWcdma,
        val signal: CellSignalStrengthWcdma,
        val info: CellInfoWcdma
    )

}