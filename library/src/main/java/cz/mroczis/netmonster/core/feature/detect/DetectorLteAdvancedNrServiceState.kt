package cz.mroczis.netmonster.core.feature.detect

import android.Manifest
import android.os.Build
import android.telephony.ServiceState
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.INetMonster
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat

/**
 * Attempts to detect LTE Advanced / LTE Carrier aggregation and NR in NSA mode
 */
class DetectorLteAdvancedNrServiceState : INetworkDetector {

    private val nsaNr = DetectorNsaNr()
    private val lteCa = DetectorLteAdvancedServiceState()

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        ]
    )
    @SinceSdk(Build.VERSION_CODES.O)
    @Deprecated("Refer to DetectorLteAdvancedServiceState or DetectorNsaNr for more details")
    override fun detect(netmonster: INetMonster, telephony: ITelephonyManagerCompat): NetworkType? {
        val nsaNr = nsaNr.detect(netmonster, telephony)
        val lteCa = lteCa.detect(netmonster, telephony)

        return if (nsaNr != null && lteCa != null) {
            // NSA is deployed but probably not in use hence LTE-CA is active
            nsaNr.copy(technology = NetworkType.LTE_CA_NR)
        } else nsaNr ?: lteCa
    }


}