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
     * 
     * Note that this is so-called marketing network type. It can be adjusted by manufacturer or
     * carrier to emulate different network type. In most of the cases they intend to
     * show 5G even when it's not in use, but only deployed on site.
     * 
     * Do not use this field as source of truth whilst detecting 5G.
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