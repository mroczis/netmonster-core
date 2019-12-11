package cz.mroczis.netmonster.core

import android.os.Build
import io.kotlintest.specs.FreeSpec
import java.lang.reflect.Field
import java.lang.reflect.Modifier

abstract class SdkTest(
    sdkInt: Int
) : FreeSpec() {

    init {
        // Option 1 - create class for each SDK change in lib's source code which would work nicely
        // with tests but complexity of library would be enormously big
        // Option 2 - mock SDK_INT
        // You can guess which option won ^_^
        setFinalStatic(Build.VERSION::class.java.getField("SDK_INT"), sdkInt)
    }

    @Throws(Exception::class)
    fun setFinalStatic(field: Field, newValue: Any) {
        field.isAccessible = true

        val modifiersField = Field::class.java.getDeclaredField("modifiers")
        modifiersField.isAccessible = true
        modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())

        field.set(null, newValue)
    }
}