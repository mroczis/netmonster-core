package cz.mroczis.netmonster.core.model.cell

import android.os.Build
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.model.band.IBand
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.signal.ISignal

interface ICell {

    /**
     * Current state of connection to cell - connected and serving or neighbouring.
     */
    val connectionStatus: IConnection

    /**
     * Band of cell containing downlink & uplink frequencies (if applicable)
     */
    @SinceSdk(Build.VERSION_CODES.N)
    val band: IBand?

    /**
     * Signal of this cell
     */
    val signal: ISignal?

    /**
     * Using visitor pattern invokes one method of [processor]
     * with proper [ICell] instance.
     *
     * Use this to map NetMonster's instances into yours.
     *
     * @param processor class that manages transformation from this object to another
     */
    fun <T> let(processor: ICellProcessor<T>) : T

}