package cz.mroczis.netmonster.core.db.local

/**
 * Represents local storage implementation whose values are immutable constants
 */
object DummyStorage : ILocalStorage {

    override var locationAreaEndiannessIncorrect: Boolean
        get() = false
        set(_) {}
}