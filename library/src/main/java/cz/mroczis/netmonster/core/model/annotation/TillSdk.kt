package cz.mroczis.netmonster.core.model.annotation

import android.os.Build

/**
 * Detonates that given feature is available in NetMonster Core, however it will always return
 * invalid value / null (depending on return value of function or field nullability) when current
 * [Build.VERSION.SDK_INT] if greater or equal to [sdkInt].
 *
 * Look to [fallbackBehaviour] description to find out what'll happen on those unsupported SDK versions.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY
)
annotation class TillSdk(val sdkInt: Int, val fallbackBehaviour: String)