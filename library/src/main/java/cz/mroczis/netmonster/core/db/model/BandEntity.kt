package cz.mroczis.netmonster.core.db.model

/**
 * Internal band representation with
 */
internal data class BandEntity(
    /**
     * Channel number range. Corresponds to range that defines band and
     * value that should belong to range is returned from Android SDK
     *
     * Minimal lower bound: 0
     * Maximal upper bound: [Integer.MAX_VALUE]
     */
    val channelRange: IntRange,
    
    /**
     * In most cases approximate frequency (850, 900, 2100, 2600, ...) or 
     * commonly used shortcut (DCS, PCS, AWS)
     */
    val name: String,
    
    /**
     * Unique identifier of band as defined in 3GPP.
     * In case of GSM this represents integer value of [name].
     */
    val number: Int
)