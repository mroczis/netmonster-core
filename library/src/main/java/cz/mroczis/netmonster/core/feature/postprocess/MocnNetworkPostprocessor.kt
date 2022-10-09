package cz.mroczis.netmonster.core.feature.postprocess

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.subscription.ISubscriptionManagerCompat

/**
 * The problem - when current network is MOCN (Multi-Operator Core Network) some Android phones
 * pick first entry from offered PLMN list ignoring the flag for currently active one.
 *
 * This issue is only present in entries returned from [android.telephony.TelephonyManager.getAllCellInfo]
 * but correct PLMNs is stored in [android.telephony.ServiceState] or [android.telephony.SubscriptionInfo].
 * This class aims to fix the issues by modifying PLMN assigned to cells.
 *
 * Postprocessor does not work if phone is in "Emergency calls only" state since those two sources
 * do not return valid data -> incorrect PLMN will stay incorrect.
 *
 * This should be LTE exclusive cause cause the bug is present only in LTE networks.
 * However it is also used to fix PLMN when it's completely invalid.
 *
 * References: [AOSP bug tracker](https://issuetracker.google.com/issues/73130708)
 */
class MocnNetworkPostprocessor(
    private val subscription: ISubscriptionManagerCompat,
    private val networkOperatorGetter: (subId: Int) -> Network?,
    private val serviceStateGetter: (subId: Int) -> ServiceState?,
) : ICellPostprocessor {

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun postprocess(list: List<ICell>): List<ICell> {
        val subscriptions = subscription.getActiveSubscriptionIds().associateWith {
            PlmnHint(
                networkOperator = networkOperatorGetter.invoke(it),
                networkRegistrations = serviceStateGetter.invoke(it)?.networkRegistrationInfo
            )
        }

        return list.map { cell ->
            if (cell.connectionStatus is PrimaryConnection) { // other connections are handled by PlmnPostprocessor
                val suggestedPlmn = subscriptions[cell.subscriptionId]?.getNetworkOperator(cell = cell)
                if ((cell is CellLte || cell.network == null) && suggestedPlmn != null && suggestedPlmn != cell.network) {
                    cell.let(PlmnSwitcher(suggestedPlmn))
                } else cell
            } else {
                cell
            }

        }
    }

    private val ServiceState.networkRegistrationInfo
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            networkRegistrationInfoList
        } else {
            emptyList()
        }

    /**
     * Changes PLMN of given cells to [plmn].
     */
    private class PlmnSwitcher(
        private val plmn: Network
    ) : ICellProcessor<ICell> {

        override fun processCdma(cell: CellCdma): ICell = cell.copy(network = plmn)
        override fun processGsm(cell: CellGsm): ICell = cell.copy(network = plmn)
        override fun processLte(cell: CellLte): ICell = cell.copy(network = plmn)
        override fun processNr(cell: CellNr): ICell = cell.copy(network = plmn)
        override fun processTdscdma(cell: CellTdscdma): ICell = cell.copy(network = plmn)
        override fun processWcdma(cell: CellWcdma): ICell = cell.copy(network = plmn)
    }

    private data class PlmnHint(
        /**
         * PLMN from getNetworkOperator
         */
        private val networkOperator: Network?,
        /**
         * List of network registrations that can help us spot data inconsistency.
         * In such case MOCN fixing should not be applied
         */
        private val networkRegistrations: List<NetworkRegistrationInfo>?
    ) {

        @TargetApi(Build.VERSION_CODES.R)
        val plmnHints = networkRegistrations?.mapNotNull {
            val rPlmn = Network.map(it.registeredPlmn)
            when (val c = it.cellIdentity) {
                is CellIdentityGsm -> (rPlmn ?: Network.map(c.mccString, c.mncString))?.let { plmn ->
                    SuggestedPlmn(
                        plmn = plmn,
                        channelNumber = null, // For GSM full CID match is required
                        cid = c.cid.toLong(),
                    )
                }
                is CellIdentityWcdma -> (rPlmn ?: Network.map(c.mccString, c.mncString))?.let { plmn ->
                    SuggestedPlmn(
                        plmn = plmn,
                        channelNumber = c.uarfcn,
                        cid = c.cid.toLong(),
                    )
                }
                is CellIdentityLte -> (rPlmn ?: Network.map(c.mccString, c.mncString))?.let { plmn ->
                    SuggestedPlmn(
                        plmn = plmn,
                        channelNumber = c.earfcn,
                        cid = c.ci.toLong(),
                    )
                }
                is CellIdentityNr -> (rPlmn ?: Network.map(c.mccString, c.mncString))?.let { plmn ->
                    SuggestedPlmn(
                        plmn = plmn,
                        channelNumber = c.nrarfcn,
                        cid = c.nci,
                    )
                }
                else -> null
            }
        }?.distinct()?.takeIf { it.isNotEmpty() }

        /**
         * LTE has a special treatment.
         * getAllCellInfo tends to return broken PLMN but correct one is hidden in networkRegistrationList
         * which was added later to AOSP. This methods prioritizes new APIs and falls back
         * to older implementation in case such APIs are not implemented or return ambiguous data
         */
        fun getNetworkOperator(cell: ICell): Network? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !plmnHints.isNullOrEmpty()) {
                val info = cell.let(CellBasicInfoExtractor)

                val possiblePlmns = plmnHints.filter { it.plmn.mcc == info?.mcc }
                if (info?.mcc != null && possiblePlmns.size == 1) {
                    possiblePlmns[0].plmn
                } else {
                    null
                }
            } else {
                networkOperator
            }

    }

    /**
     * Extracts metadata needed to match [ICell] instance with [SuggestedPlmn]
     */
    private object CellBasicInfoExtractor : ICellProcessor<MatchKey?> {
        override fun processCdma(cell: CellCdma): MatchKey? = null

        override fun processGsm(cell: CellGsm) = MatchKey(
            channelNumber = cell.band?.channelNumber,
            cid = cell.cid?.toLong(),
            mcc = cell.network?.mcc,
        )

        override fun processLte(cell: CellLte) = MatchKey(
            channelNumber = cell.band?.channelNumber,
            cid = cell.cid?.toLong(),
            mcc = cell.network?.mcc,
        )

        override fun processNr(cell: CellNr) = MatchKey(
            channelNumber = cell.band?.channelNumber,
            cid = cell.nci,
            mcc = cell.network?.mcc,
        )

        override fun processTdscdma(cell: CellTdscdma) = MatchKey(
            channelNumber = cell.band?.channelNumber,
            cid = cell.ci?.toLong(),
            mcc = cell.network?.mcc,
        )

        override fun processWcdma(cell: CellWcdma) = MatchKey(
            channelNumber = cell.band?.channelNumber,
            cid = cell.cid?.toLong(),
            mcc = cell.network?.mcc,
        )

    }

    private data class MatchKey(
        val channelNumber: Int?,
        val cid: Long?,
        val mcc: String?,
    )

    private data class SuggestedPlmn(
        val channelNumber: Int?,
        val cid: Long,
        val plmn: Network,
    )

}