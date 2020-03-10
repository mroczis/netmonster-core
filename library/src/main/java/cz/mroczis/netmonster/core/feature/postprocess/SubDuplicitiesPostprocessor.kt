package cz.mroczis.netmonster.core.feature.postprocess

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.subscription.ISubscriptionManagerCompat

/**
 * Solves the case for Dual SIM phones when [android.telephony.TelephonyManager.getAllCellInfo]
 * returns list with same data totally ignoring the subscription id that is bound to the instance.
 *
 * Since Android 10 TelephonyManager allows to request update and that delivers subscription-bound
 * data. On older versions we usually get 2 equal lists and we need to decide which cell belongs to
 * which subscription. Fortunately data are in 90 % cases sorted meaning that cells bound to 1st
 * subscription are first in the list, then serving cell for 2nd subscription ar listed etc.
 *
 * Using this predicate we can filter duplicities effectively - in this postprocessors we get
 * list of (PLMN, subscription id) and filter out cells that belong to different subscription
 *
 * Complexity: O(s * c), where 's' is size to subscriptions list and 'c' size of all cells list
 */
class SubDuplicitiesPostprocessor(
    private val subscription: ISubscriptionManagerCompat,
    private val networkOperatorGetter: (subId: Int) -> Network?
) : ICellPostprocessor {

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun postprocess(list: List<ICell>): List<ICell> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            list
        } else {
            val subscriptionsIds = subscription.getActiveSubscriptionIds()

            if (subscriptionsIds.size <= 1) {
                // Not applicable for single SIM devices
                // and on Android 10+ cause delivered results are always connected to subscription id
                // so we do not need to filter duplicates
                list
            } else {
                val subCells = list.groupBy { it.subscriptionId }

                subscriptionsIds
                    .map { subId ->
                        val currentNetwork = networkOperatorGetter.invoke(subId)

                        // Cells assigned to subscription id for current iteration
                        val cells = subCells[subId] ?: emptyList()

                        // Positions of cells that are marked as serving
                        val indexesOfPrimaryCells = cells.mapIndexedNotNull { i, cell ->
                            if (cell.connectionStatus is PrimaryConnection) {
                                i
                            } else null
                        }

                        // Cell that has the same network & subscription id
                        // Indexes in 'cells' variable where subscription id and operators match
                        val matchingIndexes = indexesOfPrimaryCells.filter { index ->
                            cells[index].network == currentNetwork && cells[index].subscriptionId == subId
                        }

                        // Now let's get interval for sublist that we'll extract
                        // Start = place where is serving cell with correct network & sub id
                        // End = place where's next serving cell (exclusive) or end of the list if there's none
                        val start: Int? = when {
                            matchingIndexes.size == 1 -> {
                                // Usual situation, dual SIM, two different PLMNs
                                matchingIndexes[0]
                            }
                            matchingIndexes.size > 1 -> {
                                // Case: user has more SIM cards that are connected to same network
                                // In this case we grab index of subscription that SHOULD match with position
                                // of serving cell
                                matchingIndexes.getOrNull(subscriptionsIds.indexOf(subId))
                            }
                            else -> null
                        }
                        val end: Int? = if (start != null) {
                            // End = where next serving cell is or at the end of the list
                            indexesOfPrimaryCells.firstOrNull { it > start } ?: cells.size
                        } else {
                            null
                        }

                        if (start != null && end != null) {
                            cells.subList(start, end)
                        } else {
                            emptyList()
                        }

                    }.flatten()
            }
        }

}