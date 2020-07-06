package cz.mroczis.netmonster.core.mapper

import android.os.Build
import android.telephony.*
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.applyNonNull
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.telephony.mapper.CellInfoMapper
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapCell
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapConnection
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapSignal
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockkClass

/**
 * Testing conversion [CellInfoLte] -> [CellLte]
 */
class CellMapperLteTest29 : SdkTest(Build.VERSION_CODES.Q) {

    companion object {
        const val CI = 523524
        const val TAC = 29410
        const val PCI = 42
        const val EARFCN = 6200
        const val MCC = 230
        const val MNC = 11
        const val BANDWIDTH = 10_000

        const val RSSI = -81
        const val RSRP = -112
        const val RSRQ = -9
        const val CQI = 5
        const val SNR = 20.5
        const val TA = 3
    }

    init {
        "Standard LTE cell" {
            val cell = mockValidCell().let {
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal()!!)
            }

            cell.applyNonNull {
                eci shouldBe CI
                cid shouldBe 4
                enb shouldBe 2045
                tac shouldBe TAC
                pci shouldBe PCI
                ecgi shouldBe "230110000523524"
                bandwidth shouldBe BANDWIDTH
                network shouldBe Network.map(MCC, MNC)
                connectionStatus shouldBe PrimaryConnection()

                band.applyNonNull {
                    downlinkEarfcn shouldBe EARFCN
                    name shouldBe "800"
                    number shouldBe 20
                }

                signal.apply {
                    dbm shouldBe RSSI
                    rssi shouldBe RSSI
                    rssiAsu shouldBe 16
                    rsrp shouldBe RSRP.toDouble()
                    rsrpAsu shouldBe 28
                    rsrq shouldBe RSRQ.toDouble()
                    cqi shouldBe CQI
                    snr shouldBe SNR
                    timingAdvance shouldBe TA
                }
            }
        }

        "Reflection - RSRP" {
            // Signal strength for LTE was once a big mess, we try to fix it

            // Samsung SM-G935V
            mockValidCell().let {
                every { it.signal.rsrp } returns 1025
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())?.signal?.rsrp shouldBe -102.5
            }

            // Sony E2003
            mockValidCell().let {
                every { it.signal.rsrp } returns 464
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())?.signal?.rsrp shouldBe -46.4
            }

            // Device name I forgot
            mockValidCell().let {
                every { it.signal.rsrp } returns 25
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())?.signal?.rsrp shouldBe (-140.0 + 25.0)
            }
        }

        "Reflection - RSRQ" {
            // Sony E2003
            mockValidCell().let {
                every { it.signal.rsrq } returns 135
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())?.signal?.rsrq shouldBe -13.5
            }
        }

        "Reflection - SNR" {
            mockValidCell().let {
                every { it.signal.rssnr } returns 300
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())?.signal?.snr shouldBe null
            }

            mockValidCell().let {
                every { it.signal.rssnr } returns 30
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())?.signal?.snr shouldBe null
            }

            mockValidCell().let {
                every { it.signal.rssnr } returns 263
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())?.signal?.snr shouldBe 26.3
            }
        }

    }

    @Suppress("DEPRECATION")
    private fun mockValidCell() : Wrapper {
        val id = mockkClass(CellIdentityLte::class)
        every { id.ci } returns CI
        every { id.tac } returns TAC
        every { id.pci } returns PCI
        every { id.earfcn } returns EARFCN
        every { id.mcc } returns MCC
        every { id.mccString } returns MCC.toString()
        every { id.mnc } returns MNC
        every { id.mncString } returns MNC.toString()
        every { id.bandwidth } returns BANDWIDTH

        val signal = mockkClass(CellSignalStrengthLte::class)
        every { signal.dbm } returns RSSI
        every { signal.rssi } returns RSSI
        every { signal.rsrp } returns RSRP
        every { signal.rsrq } returns RSRQ
        every { signal.rssnr } returns (SNR * 10).toInt()
        every { signal.cqi } returns CQI
        every { signal.timingAdvance } returns TA

        val info = mockkClass(CellInfoLte::class)
        every { info.isRegistered } returns true
        every { info.cellConnectionStatus } returns CellInfo.CONNECTION_PRIMARY_SERVING
        every { info.cellIdentity } returns id
        every { info.cellSignalStrength } returns signal

        return Wrapper(id, signal, info)
    }

    private class Wrapper(
        val identity: CellIdentityLte,
        val signal: CellSignalStrengthLte,
        val info: CellInfoLte
    )

}