package cz.mroczis.netmonster.core.model.annotation

/**
 * Denotes that the annotated element should be an double in given range.
 * Inclusive for both bounds.
 */
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.LOCAL_VARIABLE
)
annotation class DoubleRange(val from: Double, val to: Double)