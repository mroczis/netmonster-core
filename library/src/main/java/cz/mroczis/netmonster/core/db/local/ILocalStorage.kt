package cz.mroczis.netmonster.core.db.local

import cz.mroczis.netmonster.core.SubscriptionId

interface ILocalStorage {

    /**
     * Checks if TAC / LAC endianness in GSM, WCDMA, LTE networks should be flipped
     * no matter what. Applies only for new cell API
     *
     * @see cz.mroczis.netmonster.core.feature.postprocess.SamsungEndiannessPostprocessor
     */
    var locationAreaEndiannessIncorrect: Boolean

    /**
     * True if this device is capable of returning LTE bandwidth for a primary
     * LTE cell directly via getAllCellInfo() method
     */
    var reportsLteBandwidthDirectly: Boolean

    /**
     * Changes if this device reposted at least timing advance in LTE network
     * higher than 0 for given subscription ([id])
     */
    fun setReportsLteTimingAdvance(id: SubscriptionId, reports: Boolean)

    /**
     * True if this device reposted at least timing advance in LTE network
     * higher than 0 for given subscription ([id])
     */
    fun getReportsLteTimingAdvance(id: SubscriptionId): Boolean
}