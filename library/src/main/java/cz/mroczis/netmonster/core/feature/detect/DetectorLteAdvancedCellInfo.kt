package cz.mroczis.netmonster.core.feature.detect

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.INetMonster
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.model.annotation.Experimental
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat


/**
 * Attempts to detect LTE Advanced / LTE Carrier aggregation
 *
 * Based on different EARFCNs & bands in scan result.
 */
@Experimental("Attempts to guess if LTE is installed. Cannot guarantee correctness.")
@Deprecated(
    message = "This class provides detection of LTE-CA only, use more general DetectorCellInfo",
    replaceWith = ReplaceWith(
        "DetectorCellInfo",
        "cz.mroczis.netmonster.core.feature.detect.DetectorCellInfo"
    )
)
class DetectorLteAdvancedCellInfo : INetworkDetector {

    private val detectorCellInfo = DetectorCellInfo(
        detectLteCa = true,
        detectNrNsa = false
    )

    @SinceSdk(Build.VERSION_CODES.N)
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        ]
    )
    override fun detect(netmonster: INetMonster, telephony: ITelephonyManagerCompat): NetworkType? =
        detectorCellInfo.detect(netmonster, telephony)

}