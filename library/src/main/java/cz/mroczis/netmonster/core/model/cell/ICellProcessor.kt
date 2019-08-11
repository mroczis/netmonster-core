package cz.mroczis.netmonster.core.model.cell

/**
 * Implement me to use advantages of visitor pattern and map instances of
 * [ICell] to something else.
 */
interface ICellProcessor<T> {

    /**
     * Invokes for CDMA cells
     * @param cell current cell to process and transform
     */
    fun processCdma(cell: CellCdma): T

    /**
     * Invokes for GSM cells
     * @param cell current cell to process and transform
     */
    fun processGsm(cell: CellGsm): T

    /**
     * Invokes for LTE cells
     * @param cell current cell to process and transform
     */
    fun processLte(cell: CellLte): T

    /**
     * Invokes for NR cells
     * @param cell current cell to process and transform
     */
    fun processNr(cell: CellNr): T

    /**
     * Invokes for TD-SCDMA cells
     * @param cell current cell to process and transform
     */
    fun processTdscdma(cell: CellTdscdma): T

    /**
     * Invokes for WCDMA cells
     * @param cell current cell to process and transform
     */
    fun processWcdma(cell: CellWcdma): T

}