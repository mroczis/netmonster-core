package cz.mroczis.netmonster.core.model.connection

/**
 * Device is not connected to this cell was only detected nearby or it's
 * present in handover list of currently serving
 */
class NoneConnection : IConnection {

    override fun toString(): String {
        return "NoneConnection()"
    }

    override fun equals(other: Any?) = other?.javaClass == javaClass
    override fun hashCode() = toString().hashCode()

}