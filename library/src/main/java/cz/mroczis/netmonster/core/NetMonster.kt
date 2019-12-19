package cz.mroczis.netmonster.core

import android.Manifest
import android.content.Context
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.feature.config.PhysicalChannelConfigSource
import cz.mroczis.netmonster.core.feature.detect.*
import cz.mroczis.netmonster.core.feature.merge.CellMerger
import cz.mroczis.netmonster.core.feature.merge.CellSource
import cz.mroczis.netmonster.core.feature.postprocess.ICellPostprocessor
import cz.mroczis.netmonster.core.feature.postprocess.PlmnPostprocessor
import cz.mroczis.netmonster.core.feature.postprocess.PrimaryCellPostprocessor
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.config.PhysicalChannelConfig
import cz.mroczis.netmonster.core.subscription.ISubscriptionManagerCompat
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat
import cz.mroczis.netmonster.core.util.isDisplayOn

internal class NetMonster(
    private val context: Context,
    private val telephony: ITelephonyManagerCompat,
    private val subscription: ISubscriptionManagerCompat
) : INetMonster {

    private val merger = CellMerger()
    private val physicalChannelSource by lazy { PhysicalChannelConfigSource() }

    /**
     * Postprocessors that try to fix / add behaviour to [ITelephonyManagerCompat.getAllCellInfo]
     */
    private val postprocessors = mutableListOf<ICellPostprocessor>().apply {
        add(PrimaryCellPostprocessor())
        add(PlmnPostprocessor())
    }

    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    override fun getCells(): List<ICell> = getCells(
        CellSource.CELL_LOCATION, CellSource.ALL_CELL_INFO, CellSource.NEIGHBOURING_CELLS
    )

    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    override fun getCells(vararg sources: CellSource): List<ICell> {
        val oldApi = mutableListOf<ICell>().apply {
            if (sources.contains(CellSource.CELL_LOCATION)) {
                val serving = subscription.getActiveSubscriptionIds().map { subId ->
                    NetMonsterFactory.getTelephony(context, subId).getCellLocation()
                }.flatten().toSet()

                addAll(serving)
            }

            if (sources.contains(CellSource.NEIGHBOURING_CELLS)) {
                val neighbouring = subscription.getActiveSubscriptionIds().map { subId ->
                    NetMonsterFactory.getTelephony(context, subId).getNeighboringCellInfo()
                }.flatten().toSet()

                addAll(neighbouring)
            }
        }

        val newApi = if (sources.contains(CellSource.ALL_CELL_INFO)) {
            var allCells = telephony.getAllCellInfo()
            postprocessors.forEach { allCells = it.postprocess(allCells) }
            allCells
        } else emptyList()

        return merger.merge(oldApi, newApi, context.isDisplayOn())
    }

    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    override fun getNetworkType(): NetworkType = getNetworkType(
        DetectorHspaDc(),
        DetectorLteAdvancedServiceState(),
        DetectorLteAdvancedPhysicalChannel(),
        DetectorLteAdvancedCellInfo(),
        DetectorAosp() // best to keep last when all other strategies fail
    ) ?: NetworkTypeTable.get(NetworkType.UNKNOWN)

    override fun getNetworkType(vararg detectors: INetworkDetector): NetworkType? {
        for (detector in detectors) {
            detector.detect(this, telephony)?.let {
                return it
            }
        }

        return null
    }

    override fun getPhysicalChannelConfiguration(): List<PhysicalChannelConfig> =
        telephony.getTelephonyManager()?.let {
            physicalChannelSource.get(it, telephony.getSubscriberId())
        } ?: emptyList()


}