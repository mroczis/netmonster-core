package cz.mroczis.netmonster.core

import io.kotlintest.shouldNotBe

fun <T> T?.applyNonNull(block: T.() -> Unit): T {
    this shouldNotBe null; this!!
    block.invoke(this)
    return this
}

fun <T, Q> T?.letNonNull(block: (T) -> Q): Q {
    this shouldNotBe null; this!!
    return block.invoke(this)
}