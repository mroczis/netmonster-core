package cz.mroczis.netmonster.core.db.local

import cz.mroczis.netmonster.core.SubscriptionId

/**
 * Represents local storage implementation whose values are immutable constants
 */
object DummyStorage : ILocalStorage {

    override var locationAreaEndiannessIncorrect: Boolean
        get() = false
        set(_) {}

    override var reportsLteBandwidthDirectly: Boolean
        get() = false
        set(_) {}

    override fun getReportsLteTimingAdvance(id: SubscriptionId): Boolean = true
    override fun setReportsLteTimingAdvance(id: SubscriptionId, reports: Boolean) = Unit
}