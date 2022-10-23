package cz.mroczis.netmonster.core.telephony.mapper.cell

import android.annotation.TargetApi
import android.os.Build
import android.telephony.*
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.*

/**
 * Extracts serving cells without signal indicator from [ServiceState].
 * This source can enrich data for NR NSA when old cell api is not used / implemented on device - it adds LTE serving cell (but without signal)
 */
internal fun ServiceState.toCells(subscriptionId: Int): List<ICell> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        networkRegistrationInfoList.mapNotNull { it.toCell(subscriptionId) }.distinct()
    } else {
        emptyList()
    }

/**
 * Converts to [ICell]
 */
@TargetApi(Build.VERSION_CODES.R)
internal fun NetworkRegistrationInfo.toCell(subscriptionId: Int): ICell? = when (val identity = cellIdentity) {
    is CellIdentityGsm -> identity.mapCell(subscriptionId, connection, SignalGsm.EMPTY, plmn = Network.map(registeredPlmn))
    is CellIdentityWcdma -> identity.mapCell(subscriptionId, connection, SignalWcdma.EMPTY, plmn = Network.map(registeredPlmn))
    is CellIdentityLte -> identity.mapCell(subscriptionId, connection, SignalLte.EMPTY, plmn = Network.map(registeredPlmn))
    is CellIdentityNr -> identity.mapCell(subscriptionId, connection, SignalNr.EMPTY, plmn = Network.map(registeredPlmn))
    is CellIdentityCdma -> identity.mapCell(subscriptionId, connection, SignalCdma.EMPTY, plmn = Network.map(registeredPlmn))
    is CellIdentityTdscdma -> identity.mapCell(subscriptionId, connection, SignalTdscdma.EMPTY, plmn = Network.map(registeredPlmn))
    else -> null
}

@get:TargetApi(Build.VERSION_CODES.R)
private val NetworkRegistrationInfo.connection
    get() = if (isRegistered) {
        PrimaryConnection()
    } else {
        NoneConnection()
    }