package cz.mroczis.netmonster.core.model.signal

interface ISignal {

    /**
     * Main indicator for signal measurement. Range of this value depends on current network type.
     * In most cases you'll get RSSI value (if present)
     */
    val dbm: Int?

}