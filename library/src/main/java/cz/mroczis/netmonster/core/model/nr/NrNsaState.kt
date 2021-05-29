package cz.mroczis.netmonster.core.model.nr

data class NrNsaState(
    /**
     * True the primary serving cell is LTE cell and the plmn-InfoList-r15 is present in SIB2 and
     * at least one bit in this list is true, otherwise this value should be false.
     */
    val enDcAvailable: Boolean = false,
    /**
     * Detects whether currently serving LTE cell signalises that NSA NR is available.
     * This does not necessarily mean that current device is properly configured to consume NR.
     *
     * Source: PLMN-InfoList-r15
     */
    val nrAvailable: Boolean = false,
    /**
     * More detailed information about connection
     */
    val connection: Connection = Connection.Disconnected
) {


    /**
     * Class describing connection. It's possible to jump from one connection instance to another.
     *
     * For example:
     *  - [Connected] -> [Rejected] when NR signal becomes too weak,
     *  - [Disconnected] -> [Rejected] when bearing LTE cell changes and NR signal is not strong enough.
     */
    sealed class Connection {

        /**
         * NSA NR is connected
         */
        object Connected : Connection() {
            override fun toString() = "Connected"
        }

        /**
         * NSA NR is disconnected
         */
        object Disconnected : Connection() {
            override fun toString() = "Disconnected"
        }

        /**
         * Current LTE cells signalises that NR is deployed but it is not connected for some reason.
         * Depending on configuration of device at some point this will might change to [Connected]
         */
        data class Rejected(
            val reason: RejectionReason
        ) : Connection()

    }

    enum class RejectionReason {
        /**
         * The device is camped on an LTE cell that supports E-UTRA-NR Dual Connectivity(EN-DC)
         * but either the use of dual connectivity with NR(DCNR) is restricted or NR is not supported
         * by the selected PLMN.
         *
         * This basically means that carrier does not allow device to use NR services.
         *
         * @see [android.telephony.NetworkRegistrationInfo.NR_STATE_RESTRICTED]
         */
        RESTRICTED,

        /**
         * The device is camped on an LTE cell that supports E-UTRA-NR Dual Connectivity(EN-DC) and
         * both the use of dual connectivity with NR(DCNR) is not restricted and NR is supported
         * by the selected PLMN.
         *
         * This means that carrier would serve NR if everything was properly configured and NR reachable.
         * Common causes of rejection:
         *  - weak NR signal (NR might operate on different frequencies),
         *  - NR is disabled by manufacturer (or not properly configured, enabled, ...)
         *
         * @see [android.telephony.NetworkRegistrationInfo.NR_STATE_NOT_RESTRICTED]
         */
        NOT_RESTRICTED,

        /**
         * Cannot distinguish between [RESTRICTED] and [NOT_RESTRICTED].
         *
         * Connection was rejected due to lack of proper configuration, weak signal or
         * simply carrier disallowed access.
         */
        UNKNOWN,
    }


}