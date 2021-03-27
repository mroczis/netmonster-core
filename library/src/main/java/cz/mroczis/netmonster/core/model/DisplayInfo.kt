package cz.mroczis.netmonster.core.model

import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType

data class DisplayInfo(
    /**
     * Current network type from telephony API.
     */
    val networkType: NetworkType = NetworkTypeTable.get(NetworkType.UNKNOWN),
    /**
     * Override network type giving more granular information about current connection.
     * Works just for LTE and NR networks.
     */
    val overrideNetworkType: NetworkOverrideType = NetworkOverrideType.NONE,
) {
    
    enum class NetworkOverrideType {
        /**
         * No override
         */
        NONE,

        /**
         * LTE Carrier aggregation is active
         */
        LTE_CA,

        /**
         * LTE in so called advanced mode. Usually is means that this device is connected to
         * cell which has at least 20 MHz bandwidth. This does not imply that carrier
         * aggregation is active
         */
        LTE_ADVANCED,

        /**
         * LTE + NR in NSA mode
         */
        NR_NSA,

        /**
         * Could be NR in mmWaves, usually this implies that device is connected to NR
         * cell with higher bandwidth
         */
        NR_ADVANCED,
    }

}