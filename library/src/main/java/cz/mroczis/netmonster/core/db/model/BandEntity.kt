package cz.mroczis.netmonster.core.db.model

/**
 * Internal band representation with
 */
internal data class BandEntity(
    val range: IntRange,
    val name: String,
    val number: Int
)