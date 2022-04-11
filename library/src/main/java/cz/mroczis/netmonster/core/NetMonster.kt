package cz.mroczis.netmonster.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.local.ILocalStorage
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.factory.LocalStorageFactory
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.feature.config.PhysicalChannelConfigSource
import cz.mroczis.netmonster.core.feature.detect.*
import cz.mroczis.netmonster.core.feature.merge.CellMerger
import cz.mroczis.netmonster.core.feature.merge.CellSignalMerger
import cz.mroczis.netmonster.core.feature.merge.CellSource
import cz.mroczis.netmonster.core.feature.postprocess.*
import cz.mroczis.netmonster.core.model.NetMonsterConfig
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.config.PhysicalChannelConfig
import cz.mroczis.netmonster.core.subscription.ISubscriptionManagerCompat
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat
import cz.mroczis.netmonster.core.telephony.mapper.cell.toCells
import cz.mroczis.netmonster.core.util.isDisplayOn

internal class NetMonster(
    private val context: Context,
    private val subscription: ISubscriptionManagerCompat,
    config: NetMonsterConfig
) : INetMonster {

    private val oldAndNewCellMerger = CellMerger()
    private val newAndSignalCellMerger = CellSignalMerger()
    private val physicalChannelSource by lazy { PhysicalChannelConfigSource() }
    private val storage: ILocalStorage = LocalStorageFactory.get(context, config)

    /**
     * Postprocessors that try to fix / add behaviour to [ITelephonyManagerCompat.getAllCellInfo]
     */
    @SuppressLint("MissingPermission")
    private val postprocessors = listOf(
        SamsungInvalidValuesPostprocessor(),
        MocnNetworkPostprocessor(subscription) { subId ->
            getTelephony(subId).getNetworkOperator()
        }, // fix PLMNs
        InvalidCellsPostprocessor(), // get rid of false-positive cells
        PrimaryCellPostprocessor(), // mark 1st cell as Primary if required
        SubDuplicitiesPostprocessor(subscription) { subId ->
            getTelephony(subId).getNetworkOperator()
        }, // filter out duplicities, only Dual SIMs
        PlmnPostprocessor(), // guess PLMNs when channels match
        CdmaPlmnPostprocessor(), // guess PLMN for CDMA cells
        SignalStrengthPostprocessor { subId ->
            getTelephony(subId).getCellLocation().firstOrNull()
        }, // might add more signal strength indicators
        CellBandwidthPostprocessor { subId ->
            getTelephony(subId).getServiceState()
        },
        PhysicalChannelPostprocessor { subId ->
            getPhysicalChannelConfiguration(subId)
        },
        SamsungEndiannessPostprocessor(
            getCellSkeleton = { getTelephony(it).getTelephonyManager().cellSkeleton },
            setEnabled = { storage.locationAreaEndiannessIncorrect = true },
            isEnabled = { storage.locationAreaEndiannessIncorrect },
        ),
    )

    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    override fun getCells(): List<ICell> = getCells(
        CellSource.CELL_LOCATION, CellSource.ALL_CELL_INFO, CellSource.NEIGHBOURING_CELLS, CellSource.SIGNAL_STRENGTH
    )

    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    override fun getCells(vararg sources: CellSource): List<ICell> {
        val subscriptions = subscription.getActiveSubscriptionIds()
        val oldApi = mutableListOf<ICell>().apply {
            if (sources.contains(CellSource.CELL_LOCATION)) {
                val serving = subscriptions.map { subId ->
                    getTelephony(subId).getCellLocation()
                }.flatten().toSet()

                addAll(serving)
            }

            if (sources.contains(CellSource.NEIGHBOURING_CELLS)) {
                val neighbouring = subscriptions.map { subId ->
                    getTelephony(subId).getNeighboringCellInfo()
                }.flatten().toSet()

                addAll(neighbouring)
            }
        }

        val newApi = if (sources.contains(CellSource.ALL_CELL_INFO)) {
            var allCells = subscriptions.map { subId ->
                getTelephony(subId).getAllCellInfo()
            }.flatten().toSet().toList()

            postprocessors.forEach { allCells = it.postprocess(allCells) }
            allCells
        } else emptyList()

        val signalApi = if (sources.contains(CellSource.SIGNAL_STRENGTH)) {
            subscriptions.mapNotNull { subId ->
                getTelephony(subId).getSignalStrength()?.toCells(subId)
            }.flatten().toSet().toList()
        } else emptyList()

        val mergedOldNew = oldAndNewCellMerger.merge(oldApi, newApi, context.isDisplayOn())
        return newAndSignalCellMerger.merge(mergedOldNew, signalApi)
    }

    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    override fun getNetworkType(subId: Int): NetworkType = getNetworkType(
        subId,
        DetectorHspaDc(),
        DetectorLteAdvancedNrServiceState(),
        DetectorLteAdvancedPhysicalChannel(),
        DetectorLteAdvancedCellInfo(),
        DetectorAosp() // best to keep last when all other strategies fail
    ) ?: NetworkTypeTable.get(NetworkType.UNKNOWN)

    override fun getNetworkType(subId: Int, vararg detectors: INetworkDetector): NetworkType? {
        val telephony = getTelephony(subId)
        return detectors.firstNotNullOfOrNull { detector ->
            detector.detect(this, telephony)
        }
    }

    override fun getPhysicalChannelConfiguration(subId: Int): List<PhysicalChannelConfig> =
        getTelephony(subId).getTelephonyManager()?.let {
            physicalChannelSource.get(it, subId)
        } ?: emptyList()

    private fun getTelephony(subId: Int): ITelephonyManagerCompat {
        return NetMonsterFactory.getTelephony(context, subId)
    }

}