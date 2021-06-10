package cz.mroczis.netmonster.core.feature

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.telephony.ServiceState
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import cz.mroczis.netmonster.core.INetMonster
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection
import cz.mroczis.netmonster.core.model.nr.NrNsaState
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat
import java.util.*

/**
 * AOSP documentation (Android 10):
 * The device is camped on an LTE cell that supports E-UTRA-NR Dual Connectivity(EN-DC) and
 * also connected to at least one 5G cell as a secondary serving cell.
 *
 * NR_STATE_CONNECTED / 3
 *
 * Android 10 and some Android P devices - nrStatus=CONNECTED
 * Huawei Android P - nsaState=5 - Tested on Mate 20X 5G
 * LG Android P - EnDc=true and 5G Allocated=true - Not tested on a real LG 5G device
 */
class NrNsaStateParser {

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun parse(netmonster: INetMonster, telephony: ITelephonyManagerCompat): NrNsaState? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && telephony.getNetworkType() is NetworkType.Lte) {
            telephony.getTelephonyManager()?.serviceState?.let { serviceState ->
                parse(serviceState, netmonster.getCells())
            }
        } else null

    /**
     * Detects NSA NR using
     *  - [ServiceState] - scans for hidden fields that appear when [toString] method is invoked
     *  - [ICell]s - starting from Android S some data in [ServiceState] are hidden and this second parameter brings more clarity
     */
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    @TargetApi(Build.VERSION_CODES.P)
    fun parse(serviceState: ServiceState, cells: List<ICell>): NrNsaState =
        parse(serviceState.toString(), cells)

    @VisibleForTesting
    internal fun parse(serviceState: String, cells: List<ICell>): NrNsaState {
        // Android standard way to detect connection
        val nrState = getStringField(serviceState, "nrState")
            /* Should be upper case by default; just in case, manufacturers tend to surprise all the time */
            .map { it.toUpperCase(Locale.getDefault()) }

        val nsaStateConnected = getIntField(serviceState, "nsaState").contains(5) // Huawei Android P
        val nsaStateAvailable = getIntField(serviceState, "nsaState").any { it in 2..4 } // Huawei Android P
        val enDc = getBooleanField(serviceState, "EnDc").contains(true) // LG Android P
        val allocated5g = getBooleanField(serviceState, "5G Allocated").contains(true) // LG Android P

        val dcNrRestricted = getBooleanField(serviceState, "isDcNrRestricted").contains(true)
        val nrAvailable = getBooleanField(serviceState, "isNrAvailable").contains(true)
        val enDcAvailable = getBooleanField(serviceState, "isEnDcAvailable").contains(true)

        return NrNsaState(
            enDcAvailable = enDc || enDcAvailable || nsaStateConnected,
            nrAvailable = nrAvailable || nsaStateConnected || nsaStateAvailable,
            connection = when {
                // Manufacturer-specific rules ~ Android P
                enDc && allocated5g -> NrNsaState.Connection.Connected // LG
                nsaStateConnected -> NrNsaState.Connection.Connected // Huawei

                // Generally working ~ Android P, Q, R
                nrState.contains("CONNECTED") -> NrNsaState.Connection.Connected
                nrState.contains("NOT_RESTRICTED") -> NrNsaState.Connection.Rejected(
                    NrNsaState.RejectionReason.NOT_RESTRICTED
                )
                nrState.contains("RESTRICTED") || dcNrRestricted -> NrNsaState.Connection.Rejected(NrNsaState.RejectionReason.RESTRICTED)
                nrState.contains("NONE") -> NrNsaState.Connection.Disconnected

                // When nrState is now masked '****' ~ Android S+
                nrAvailable && parse(cells) -> NrNsaState.Connection.Connected
                nrAvailable && dcNrRestricted -> NrNsaState.Connection.Rejected(NrNsaState.RejectionReason.RESTRICTED)
                nrAvailable && !dcNrRestricted -> NrNsaState.Connection.Rejected(NrNsaState.RejectionReason.NOT_RESTRICTED)
                nrAvailable || enDcAvailable -> NrNsaState.Connection.Rejected(NrNsaState.RejectionReason.UNKNOWN)
                else -> NrNsaState.Connection.Disconnected
            }
        )

    }

    /**
     * Detects NSA NR using only [ICell] - searches for primary LTE and secondary NR cell
     */
    private fun parse(cells: List<ICell>) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val servingLte = cells.any { it is CellLte && it.connectionStatus is PrimaryConnection }
            val secondaryNr = cells.any { it is CellNr && it.connectionStatus is SecondaryConnection }

            servingLte && secondaryNr
        } else {
            false
        }

    /**
     * Same as [getFieldValue] but applies general [STRING] regex for use-cases
     * in this class converts output to [String]
     */
    private fun getStringField(source: String, delimiter: String): List<String> =
        getFieldValue(source, delimiter, STRING)

    /**
     * Same as [getFieldValue] but applies general [NUMBER] regex for use-cases
     * in this class converts output to [Int]
     */
    private fun getIntField(source: String, delimiter: String): List<Int> =
        getFieldValue(source, delimiter, NUMBER).mapNotNull { it.toIntOrNull() }

    /**
     * Same as [getFieldValue] but applies general [BOOLEAN] regex for use-cases
     * in this class converts output to [Boolean]
     */
    private fun getBooleanField(source: String, name: String): List<Boolean> =
        getFieldValue(source, name, BOOLEAN).map { it == "true" }

    /**
     * Extracts from [source] fragments that follow [delimiter] and match [regex]
     * throwing away empty strings
     */
    private fun getFieldValue(source: String, delimiter: String, regex: Regex): List<String> =
        source.split(delimiter).mapNotNull {
            regex.find(it)?.groups?.get(1)?.value?.takeIf { value ->
                value.isNotBlank()
            }
        }

    private companion object {
        private val STRING = "^ ?=? ?([a-zA-Z*]*)".toRegex()
        private val NUMBER = "^ ?=? ?([0-9]*)".toRegex()
        private val BOOLEAN = "^ ?=? ?(true|false)".toRegex()
    }
}