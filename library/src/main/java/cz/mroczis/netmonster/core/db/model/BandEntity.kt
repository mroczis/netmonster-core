package cz.mroczis.netmonster.core.db.model

/**
 * Internal band representation
 */
interface IBandEntity{
    /**
     * Channel number range. Corresponds to range that defines band and
     * value that should belong to range is returned from Android SDK
     *
     * Minimal lower bound: 0
     * Maximal upper bound: [Integer.MAX_VALUE]
     */
    val channelRange: IntRange
    
    /**
     * In most cases approximate frequency (850, 900, 2100, 2600, ...) or 
     * commonly used shortcut (DCS, PCS, AWS)
     */
    val name: String
    
    /**
     * Unique identifier of band as defined in 3GPP.
     * In case of GSM this represents integer value of [name].
     */
    val number: Int?
}

internal data class BandEntity(
    override val channelRange: IntRange,
    override val name: String,
    override val number: Int?,
    /**
     * Applicable only for NR SA networks. Acts as a predicted SCS value based on a band number.
     * Real value is not known and depends on network configuration.
     *
     * Not applicable for other network types, in LTE world always 15 kHz but this method returns null.
     *
     * Unit: kHz
     */
    internal val scs: Int? = null,
) : IBandEntity