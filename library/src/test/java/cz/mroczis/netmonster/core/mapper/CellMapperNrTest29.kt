package cz.mroczis.netmonster.core.mapper

import android.os.Build
import android.telephony.*
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.applyNonNull
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapCell
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapConnection
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapSignal
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockkClass

/**
 * Testing conversion [CellInfoNr] -> [CellNr]
 */
class CellMapperNrTest29 : SdkTest(Build.VERSION_CODES.Q) {

    companion object {
        const val NCI = Integer.MAX_VALUE + 1L
        const val TAC = 29410
        const val PCI = 42
        const val ARFCN = 158_000
        const val MCC = 230
        const val MNC = 11

        const val SS_RSRP = -112
        const val SS_RSRQ = -9
        const val SS_SINR = 2

        const val CSI_RSRP = -68
        const val CSI_RSRQ = -4
        const val CSI_SINR = -5
    }

    init {
        "Standard NR cell" {
            val cell = mockValidCell().let {
                it.identity.mapCell(0, it.info.mapConnection(), it.signal.mapSignal())
            }

            cell.applyNonNull {
                nci shouldBe NCI
                tac shouldBe TAC
                pci shouldBe PCI
                network shouldBe Network.map(MCC, MNC)
                connectionStatus shouldBe PrimaryConnection()

                band.applyNonNull {
                    downlinkArfcn shouldBe ARFCN
                    downlinkFrequency shouldBe 790_000
                    name shouldBe "700"
                    number shouldBe 28
                }

                signal.apply {
                    dbm shouldBe CSI_RSRP
                    csiRsrp shouldBe CSI_RSRP
                    csiRsrq shouldBe CSI_RSRQ
                    csiSinr shouldBe CSI_SINR
                    ssRsrp shouldBe SS_RSRP
                    ssRsrq shouldBe SS_RSRQ
                    ssSinr shouldBe SS_SINR

                    csiRsrpAsu shouldBe CSI_RSRP + 140
                    ssRsrpAsu shouldBe SS_RSRP + 140
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun mockValidCell() : Wrapper {
        val id = mockkClass(CellIdentityNr::class)
        every { id.nci } returns NCI
        every { id.tac } returns TAC
        every { id.pci } returns PCI
        every { id.nrarfcn } returns ARFCN
        every { id.mccString } returns MCC.toString()
        every { id.mncString } returns MNC.toString()

        val signal = mockkClass(CellSignalStrengthNr::class)
        every { signal.dbm } returns CSI_RSRP
        every { signal.csiRsrp } returns CSI_RSRP
        every { signal.csiRsrq } returns CSI_RSRQ
        every { signal.csiSinr } returns CSI_SINR
        every { signal.ssRsrp } returns SS_RSRP
        every { signal.ssRsrq } returns SS_RSRQ
        every { signal.ssSinr } returns SS_SINR

        val info = mockkClass(CellInfoNr::class)
        every { info.isRegistered } returns true
        every { info.cellConnectionStatus } returns CellInfo.CONNECTION_PRIMARY_SERVING
        every { info.cellIdentity } returns id
        every { info.cellSignalStrength } returns signal

        return Wrapper(id, signal, info)
    }

    private class Wrapper(
        val identity: CellIdentityNr,
        val signal: CellSignalStrengthNr,
        val info: CellInfoNr
    )

}