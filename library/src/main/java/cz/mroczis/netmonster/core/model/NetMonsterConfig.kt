package cz.mroczis.netmonster.core.model

/**
 * Configuration that affects [cz.mroczis.netmonster.core.INetMonster]'s behaviour
 */
data class NetMonsterConfig(
    /**
     * Indicates if NetMonster should persist some metadata
     * and use them in order to serve more precise scan
     * results.
     *
     * Once off the whole processing becomes *stateless* meaning that no
     * data will be stored between sub-sequent method calls.
     *
     * Required to:
     *  - Detect if TAC endianness is incorrect + fix it
     */
    val stateful: Boolean = true
)