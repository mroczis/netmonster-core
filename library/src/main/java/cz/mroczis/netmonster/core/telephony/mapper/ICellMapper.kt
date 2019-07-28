package cz.mroczis.netmonster.core.telephony.mapper

import cz.mroczis.netmonster.core.model.cell.ICell

/**
 * Mapper calls transforms AOSP's representation of model into ours
 */
interface ICellMapper<T> {

    /**
     * Map method
     * @return list of cells that are valid or empty list if nothing is correct
     */
    fun map(model: T) : List<ICell>

}