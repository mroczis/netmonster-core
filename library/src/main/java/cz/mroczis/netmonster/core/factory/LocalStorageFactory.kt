package cz.mroczis.netmonster.core.factory

import android.content.Context
import cz.mroczis.netmonster.core.db.local.DummyStorage
import cz.mroczis.netmonster.core.db.local.ILocalStorage
import cz.mroczis.netmonster.core.db.local.LocalStorage
import cz.mroczis.netmonster.core.model.NetMonsterConfig

/**
 * Internal factory producing [ILocalStorage]
 */
internal object LocalStorageFactory {

    fun get(context: Context, config: NetMonsterConfig): ILocalStorage = when (config.stateful) {
        true -> LocalStorage.getInstance(context = context)
        false -> DummyStorage
    }

}