package cz.mroczis.netmonster.core.feature.detect

import cz.mroczis.netmonster.core.INetMonster
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat

/**
 * Class that is able to detect current network type
 */
interface INetworkDetector {

    /**
     * Performs search and detects current network type.
     * Returns null if search was not successful or not possible at this moment.
     */
    fun detect(netmonster: INetMonster, telephony: ITelephonyManagerCompat) : NetworkType?

}