package cz.mroczis.netmonster.core.model.annotation

import android.os.Build

/**
 * Detonates that given feature is available in NetMonster Core, however it will always return
 * invalid value / null (depending on return value of function or field nullability) till current
 * [Build.VERSION.SDK_INT] if greater or equal to [sdkInt].
 *
 * If this annotation is applied to a type then it means that not a single instance of it will appear
 * till device has at least [sdkInt].
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR
)
annotation class SinceSdk(val sdkInt: Int)