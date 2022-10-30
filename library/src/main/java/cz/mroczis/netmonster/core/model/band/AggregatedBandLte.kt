package cz.mroczis.netmonster.core.model.band

data class AggregatedBandLte(
    /**
     * Band number. Can be used as unique band identifier for non-GSM implementation
     */
    val number: Int?,

    /**
     * Name of band - for example: PCS, DCS, 900, 800, AWS, ...
     */
    val name: String,
)