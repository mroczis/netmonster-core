package cz.mroczis.netmonster.core.db.model

import androidx.annotation.IntDef

@IntDef(
    value = [
        NetworkType.UNKNOWN,
        NetworkType.GPRS,
        NetworkType.EDGE,
        NetworkType.UMTS,
        NetworkType.CDMA,
        NetworkType.EVDO_0,
        NetworkType.EVDO_A,
        NetworkType.ONExRTT,
        NetworkType.HSDPA,
        NetworkType.HSUPA,
        NetworkType.HSPA,
        NetworkType.IDEN,
        NetworkType.EVDO_B,
        NetworkType.LTE,
        NetworkType.EHRPD,
        NetworkType.HSPAP,
        NetworkType.GSM,
        NetworkType.TD_SCDMA,
        NetworkType.IWLAN,
        NetworkType.LTE_CA,
        NetworkType.NR
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class Network

@IntDef(
    value = [
        NetworkType.GPRS,
        NetworkType.EDGE,
        NetworkType.GSM
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class NetworkGsm

@IntDef(
    value = [
        NetworkType.CDMA,
        NetworkType.EVDO_0,
        NetworkType.EVDO_A,
        NetworkType.EVDO_B,
        NetworkType.ONExRTT
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class NetworkCdma

@IntDef(
    value = [
        NetworkType.UMTS,
        NetworkType.HSDPA,
        NetworkType.HSUPA,
        NetworkType.HSPA,
        NetworkType.HSPAP,
        NetworkType.HSPA_DC
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class NetworkWcdma

@IntDef(
    value = [
        NetworkType.LTE,
        NetworkType.LTE_CA,
        NetworkType.IWLAN
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class NetworkLte

@IntDef(
    value = [
        NetworkType.TD_SCDMA
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class NetworkTdscdma

@IntDef(
    value = [
        NetworkType.NR,
        NetworkType.LTE_NR,
        NetworkType.LTE_CA_NR
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class NetworkNr

@IntDef(
    value = [
        NetworkType.IDEN,
        NetworkType.UNKNOWN,
        NetworkType.EHRPD // LTE or CDMA
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class NetworkUnknown