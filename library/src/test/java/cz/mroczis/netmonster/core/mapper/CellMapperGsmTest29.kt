package cz.mroczis.netmonster.core.mapper

import android.os.Build
import android.telephony.CellIdentityGsm
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellSignalStrengthGsm
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.applyNonNull
import cz.mroczis.netmonster.core.letNonNull
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellGsm
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalGsm
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapCell
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapConnection
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapSignal
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockkClass

/**
 * Testing conversion [CellInfoGsm] -> [CellGsm]
 */
class CellMapperGsmTest29 : SdkTest(Build.VERSION_CODES.Q) {

    companion object {
        const val CID = 10362
        const val LAC = 29410
        const val BSIC = 34
        const val ARFCN = 11
        const val MCC = 230
        const val MNC = 11

        const val BIT_ERROR = 3
        const val TA = 4
        const val RSSI = -79
    }

    init {
        "Standard GSM cell" {
            val cell = mockValidCell().let {
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())
            }

            cell.applyNonNull {
                cid shouldBe CID
                lac shouldBe LAC
                bsic shouldBe BSIC
                bcc shouldBe 4
                ncc shouldBe 3
                cgi shouldBe "230112941010362"
                network shouldBe Network.map(MCC, MNC)
                connectionStatus shouldBe PrimaryConnection()

                band.applyNonNull {
                    arfcn shouldBe ARFCN
                    name shouldBe "900"
                    number shouldBe 900
                }

                signal.apply {
                    bitErrorRate shouldBe BIT_ERROR
                    timingAdvance shouldBe TA
                    getDistanceToCell() shouldBe TA * SignalGsm.ONE_TA_IN_METERS
                    dbm shouldBe RSSI
                    rssi shouldBe RSSI
                    asu shouldBe 17
                }
            }
        }

        // Primary cell must have valid CID
        "Invalid CID" {
            mockValidCell().let {
                arrayOf( // Array of invalid CIDs we've detected across all devices
                    65535, // Some old phone
                    65536, // Some old phone
                    -1, // Samsung
                    Integer.MAX_VALUE // AOSP
                ).forEach { cid ->
                    every { it.identity.cid } returns cid
                    it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal()) shouldBe null
                }
            }
        }

        // Primary cell must have valid LAC
        "Invalid LAC" {
            mockValidCell().let {
                arrayOf( // Array of invalid LACs we've detected across all devices
                    65535, // OPPO A33w, TCL 6062W, OPPO CPH1859
                    -1, // Samsung
                    0, // Samsung SM-G960F
                    Integer.MAX_VALUE // AOSP
                ).forEach { lac ->
                    every { it.identity.lac } returns lac
                    it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal()) shouldBe null
                }
            }
        }

        "Invalid BSIC" {
            mockValidCell().let {
                arrayOf( // Array of invalid BSICs we've detected across all devices
                    0xFF, // Essential Products PH-1
                    0x7fffffff, // Sony G8441, motorola moto e5 plus
                    Integer.MAX_VALUE // AOSP
                ).forEach { bsic ->
                    every { it.identity.bsic } returns bsic
                    it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal()).letNonNull { cell ->
                        cell.bsic shouldBe null
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun mockValidCell() : Wrapper {
        val gsmId = mockkClass(CellIdentityGsm::class)
        every { gsmId.cid } returns CID
        every { gsmId.lac } returns LAC
        every { gsmId.bsic } returns BSIC
        every { gsmId.arfcn } returns ARFCN
        every { gsmId.mcc } returns MCC
        every { gsmId.mccString } returns MCC.toString()
        every { gsmId.mnc } returns MNC
        every { gsmId.mncString } returns MNC.toString()

        val gsmSignal = mockkClass(CellSignalStrengthGsm::class)
        every { gsmSignal.bitErrorRate } returns BIT_ERROR
        every { gsmSignal.timingAdvance } returns TA
        every { gsmSignal.dbm } returns RSSI

        val gsmInfo = mockkClass(CellInfoGsm::class)
        every { gsmInfo.isRegistered } returns true
        every { gsmInfo.cellConnectionStatus } returns CellInfo.CONNECTION_PRIMARY_SERVING
        every { gsmInfo.cellIdentity } returns gsmId
        every { gsmInfo.cellSignalStrength } returns gsmSignal

        return Wrapper(gsmId, gsmSignal, gsmInfo)
    }

    private class Wrapper(
        val identity: CellIdentityGsm,
        val signal: CellSignalStrengthGsm,
        val info: CellInfoGsm
    )

}