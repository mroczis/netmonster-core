package cz.mroczis.netmonster.core.model

/**
 * Composite model for SubscriptionManager
 */
data class SubscribedNetwork(
    val simSlotIndex: Int,
    val subscriptionId: Int,
    val network: Network?,
    /**
     * Registration of the SIM, voice or data, whichever is better - mirror of [android.telephony.ServiceState.getState].
     * Out of service only fresh cells of it are reported, some modems keep reporting the last cells they had.
     */
    val registrationState: RegistrationState = RegistrationState.IN_SERVICE,
) {

    /**
     * Mirror of ServiceState.STATE_* constants
     */
    enum class RegistrationState {
        /**
         * Normal operation condition, the phone is registered with an operator either in home network or in roaming.
         * [android.telephony.ServiceState.STATE_IN_SERVICE]
         */
        IN_SERVICE,

        /**
         * Phone is not registered with any operator, the phone can be currently searching a new operator to register
         * to, or not searching to registration at all, or registration is denied, or radio signal is not available.
         * Active only since Android 10, such SIMs are left out before.
         * [android.telephony.ServiceState.STATE_OUT_OF_SERVICE]
         */
        OUT_OF_SERVICE,

        /**
         * The phone is registered and locked. Only emergency numbers are allowed.
         * [android.telephony.ServiceState.STATE_EMERGENCY_ONLY]
         */
        EMERGENCY_ONLY,

        /**
         * Radio of telephony is explicitly powered off. Such SIMs are never active.
         * [android.telephony.ServiceState.STATE_POWER_OFF]
         */
        POWER_OFF,
    }
}
