package cz.mroczis.netmonster.core.model

/**
 * Composite model for SubscriptionManager
 */
data class SubscribedNetwork(
    val simSlotIndex: Int,
    val subscriptionId: Int,
    val network: Network?
)