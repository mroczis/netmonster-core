package cz.mroczis.netmonster.core.util

/**
 * Checks if this number is in specified [range].
 * @return this if this is in [range]
 */
fun Int.inRangeOrNull(range: IntRange) =
    if (range.contains(this)) {
        this
    } else null

/**
 * Checks if this number is in specified [range].
 * @return this if this is in [range]
 */
fun Int.inRangeOrNull(range: LongRange) =
    if (range.contains(this)) {
        this
    } else null

/**
 * Checks if this number is in specified [range].
 * @return this if this is in [range]
 */
fun Long.inRangeOrNull(range: LongRange) =
    if (range.contains(this)) {
        this
    } else null

/**
 * Checks if this number is in specified [range].
 * @return this if this is in [range]
 */
fun Double.inRangeOrNull(range: ClosedFloatingPointRange<Double>) =
    if (range.contains(this)) {
        this
    } else null