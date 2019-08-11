package cz.mroczis.netmonster.core.util

/**
 * Returns the first element matching the given [predicate], or `null` if element was not found.
 * Also removes that first element from source collection.
 */
inline fun <T> MutableIterable<T>.removeFirstOrNull(predicate: (T) -> Boolean): T? {
    iterator().let {
        while (it.hasNext()) {
            val element = it.next()
            if (predicate(element)) {
                it.remove()
                return element
            }
        }
    }

    return null
}