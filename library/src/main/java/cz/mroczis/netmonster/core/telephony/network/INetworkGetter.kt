package cz.mroczis.netmonster.core.telephony.network

import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat

/**
 * Interface that allows us obtain [Network].
 */
interface INetworkGetter {

    /**
     * Fetches network from using [telephony]
     * @return network or null
     */
    fun getNetwork(telephony: ITelephonyManagerCompat): Network?
}