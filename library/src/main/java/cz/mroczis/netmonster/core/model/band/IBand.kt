package cz.mroczis.netmonster.core.model.band

interface IBand {

    /**
     * Current 'channelNumber' value reported by Android system
     *  - GSM: ARFCN
     *  - WCDMA: downlink UARFCN
     *  - LTE: downlink EARFCN
     *  - TD-SCDMA: downlink UARFCN
     *  - NR: downlink ARFCN
     */
    val channelNumber: Int

    /**
     * Band number. Can be used as unique band identifier for non-GSM implementation
     */
    val number: Int?

    /**
     * Name of band - for example: PCS, DCS, 900, 800, AWS, ...
     */
    val name: String?
}