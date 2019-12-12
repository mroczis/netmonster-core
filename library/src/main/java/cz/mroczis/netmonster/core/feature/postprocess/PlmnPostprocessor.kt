package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.*

/**
 * Attempts to assign valid PLMN ([Network]) to cells which do not have valid value.
 */
class PlmnPostprocessor : ICellPostprocessor {

    private val plmnExtractor = PlmnExtractor()

    override fun postprocess(list: List<ICell>): List<ICell> {
        val plmns: Map<NetworkGeneration, List<PlmnNetwork>> = list.map { it.let(plmnExtractor) }
            .filterNotNull()
            .distinct()
            .groupBy { it.generation }

        val plmnStacker = PlmnStacker(plmns)
        return list.map { it.let(plmnStacker) }
    }

    /**
     * Extracts data required to assign PLMN to cells that to not have valid PLMN
     */
    private class PlmnExtractor : ICellProcessor<PlmnNetwork?> {
        override fun processCdma(cell: CellCdma): PlmnNetwork? =
            null

        override fun processGsm(cell: CellGsm) =
            cell.network?.let { PlmnNetwork(it, NetworkGeneration.GSM, null, cell.lac) }

        override fun processWcdma(cell: CellWcdma) =
            cell.network?.let { PlmnNetwork(it, NetworkGeneration.WCDMA, cell.band?.channelNumber) }

        override fun processLte(cell: CellLte) =
            cell.network?.let { PlmnNetwork(it, NetworkGeneration.LTE, cell.band?.channelNumber) }

        override fun processTdscdma(cell: CellTdscdma) =
            cell.network?.let {
                PlmnNetwork(
                    it,
                    NetworkGeneration.TDSCDMA,
                    cell.band?.channelNumber
                )
            }

        override fun processNr(cell: CellNr) =
            cell.network?.let { PlmnNetwork(it, NetworkGeneration.NR, cell.band?.channelNumber) }
    }

    /**
     * Assigns PLMN to [ICell] from its [dictionary].
     * Processing differs for each [NetworkGeneration].
     *  - CDMA is not supported
     *  - For WCDMA, LTE, NR and TDSCDMA copies PLMN of serving cell or takes
     *  PLMN of cell that has same channel number
     *  - For GSM takes PLMN of serving cell only if there are no other serving cells
     *  (in dual SIM phones neighbours of both SIMs usually appear). In second case we take look
     *  on LAC that can sometimes help (when it equals, we also grab that PLMN)
     */
    private class PlmnStacker(
        private val dictionary: Map<NetworkGeneration, List<PlmnNetwork>>
    ) : ICellProcessor<ICell> {

        override fun processCdma(cell: CellCdma) = cell

        override fun processGsm(cell: CellGsm) = dictionary[NetworkGeneration.GSM]?.let { plmns ->
            if (plmns.size == 1 && dictionary.size == 1) {
                cell.copy(network = plmns[0].network)
            } else {
                val lacPlmn = plmns.firstOrNull { it.areaCode == cell.lac }
                if (lacPlmn != null) {
                    cell.copy(network = lacPlmn.network)
                } else {
                    cell
                }
            }
        } ?: cell

        override fun processLte(cell: CellLte) =
            findByChannel(NetworkGeneration.LTE, cell.band?.channelNumber)?.let {
                cell.copy(network = it)
            } ?: cell

        override fun processNr(cell: CellNr) =
            findByChannel(NetworkGeneration.NR, cell.band?.channelNumber)?.let {
                cell.copy(network = it)
            } ?: cell

        override fun processTdscdma(cell: CellTdscdma) =
            findByChannel(NetworkGeneration.TDSCDMA, cell.band?.channelNumber)?.let {
                cell.copy(network = it)
            } ?: cell


        override fun processWcdma(cell: CellWcdma)=
            findByChannel(NetworkGeneration.WCDMA, cell.band?.channelNumber)?.let {
                cell.copy(network = it)
            } ?: cell

        private fun findByChannel(gen: NetworkGeneration, channel: Int?): Network? =
            dictionary[gen]?.let { plmns ->
                if (plmns.size == 1) {
                    plmns[0].network
                } else {
                    plmns.firstOrNull { it.channelNumber == channel }?.network
                }
            }
    }

    /**
     * Internal data structure that holds all attributes we need to assign PLMN properly
     */
    private data class PlmnNetwork(
        val network: Network,
        val generation: NetworkGeneration,
        val channelNumber: Int? = null,
        val areaCode: Int? = null
    )

    /**
     * Network generations supported by this algorithm
     */
    private enum class NetworkGeneration {
        GSM, LTE, NR, TDSCDMA, WCDMA
    }
}