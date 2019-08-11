package cz.mroczis.netmonster.core.util

/**
 * Returns min from two values or first not-null one
 */
fun minOrNotnull(a: Int?, b: Int?) : Int? =
    if (a != null && b != null) {
        kotlin.math.min(a, b)
    } else a ?: b

/**
 * Returns max from two values or first not-null one
 */
fun maxOrNotnull(a: Int?, b: Int?) : Int? =
    if (a != null && b != null) {
        kotlin.math.max(a, b)
    } else a ?: b