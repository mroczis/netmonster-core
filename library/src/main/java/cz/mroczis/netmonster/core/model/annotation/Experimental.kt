package cz.mroczis.netmonster.core.model.annotation

/**
 * Custom note for class / method that it's experimental and its results
 * might not be 100 % reliable to reflect current state of the reality.
 *
 * Use with caution.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
annotation class Experimental(val why: String)