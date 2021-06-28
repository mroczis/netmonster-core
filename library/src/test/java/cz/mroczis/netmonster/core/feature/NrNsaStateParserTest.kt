package cz.mroczis.netmonster.core.feature

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.db.BandTableLte
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection
import cz.mroczis.netmonster.core.model.nr.NrNsaState
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.model.signal.SignalNr
import io.kotlintest.shouldBe

class NrNsaStateParserTest : SdkTest(Build.VERSION_CODES.P) {

    private val parser = NrNsaStateParser()

    companion object {
        private const val PIXEL_2_XL_RESULT =
            "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=6400, duplexMode()=1, mCellBandwidths=[], mVoiceOperatorAlphaLong=Vodafone CZ, mVoiceOperatorAlphaShort=Vodafone CZ, mDataOperatorAlphaLong=Vodafone CZ, mDataOperatorAlphaShort=Vodafone CZ, isManualNetworkSelection=false(automatic), getRilVoiceRadioTechnology=14(LTE), getRilDataRadioTechnology=14(LTE), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, isUsingCarrierAggregation=true, mLteEarfcnRsrpBoost=0, mNetworkRegistrationInfos=[NetworkRegistrationInfo{ domain=CS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[VOICE,SMS,VIDEO] cellIdentity=CellIdentityLte:{ mCi=263291 mPci=96 mTac=44600 mEarfcn=6400 mBandwidth=2147483647 mMcc=230 mMnc=03 mAlphaLong= mAlphaShort=} voiceSpecificInfo=VoiceSpecificRegistrationInfo { mCssSupported=false mRoamingIndicator=1 mSystemIsInPrl=-1 mDefaultRoamingIndicator=-1} dataSpecificInfo=null nrState=NONE}, NetworkRegistrationInfo{ domain=PS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[DATA] cellIdentity=CellIdentityLte:{ mCi=263291 mPci=96 mTac=44600 mEarfcn=6400 mBandwidth=2147483647 mMcc=230 mMnc=03 mAlphaLong= mAlphaShort=} voiceSpecificInfo=null dataSpecificInfo=android.telephony.DataSpecificRegistrationInfo :{ maxDataCalls = 20 isDcNrRestricted = false isNrAvailable = false isEnDcAvailable = false LteVopsSupportInfo :  mVopsSupport = 1 mEmcBearerSupport = 1 mIsUsingCarrierAggregation = true } nrState=NONE}], mNrFrequencyRange=-1, mOperatorAlphaLongRaw=Vodafone CZ, mOperatorAlphaShortRaw=Vodafone CZ, mIsIwlanPreferred=false}"

        private const val SAMSUNG_S10_5G_RESULT =
            "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=199, duplexMode()=1, mCellBandwidths=[], isManualNetworkSelection=false(automatic), getRilVoiceRadioTechnology=14(LTE), getRilDataRadioTechnology=14(LTE), mCssIndicator=unsupported, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, VoiceRegType=0, ImsVoiceAvail=2, Snap=0, MobileVoice=IN_SERVICE, MobileVoiceRat=Unknown, MobileData=IN_SERVICE, MobileDataRoamingType=home, MobileDataRat=LTE PsOnly=false FemtocellInd=0 SprDisplayRoam=false EndcStatus=1 RestrictDcnr=0 NrBearerStatus=1 5gStatus=1 RRCState=1, mIsEmergencyOnly=false, isUsingCarrierAggregation=false, mLteEarfcnRsrpBoost=0, mNetworkRegistrationInfos=[NetworkRegistrationInfo{ domain=CS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[VOICE,SMS,VIDEO] cellIdentity=CellIdentityLte:{ mCi=1 mPci=2 mTac=3 mEarfcn=199 mBandwidth=0 mMcc=234 mMnc=10 mAlphaLong=O2 - UK mAlphaShort=O2 - UK} voiceSpecificInfo=VoiceSpecificRegistrationInfo { mCssSupported=false mRoamingIndicator=0 mSystemIsInPrl=0 mDefaultRoamingIndicator=0} dataSpecificInfo=null nrState=NONE}, NetworkRegistrationInfo{ domain=PS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[DATA] cellIdentity=CellIdentityLte:{ mCi=1 mPci=2 mTac=3 mEarfcn=4 mBandwidth=0 mMcc=234 mMnc=10 mAlphaLong=O2 - UK mAlphaShort=O2 - UK} voiceSpecificInfo=null dataSpecificInfo=android.telephony.DataSpecificRegistrationInfo :{ maxDataCalls = 4 isDcNrRestricted = false isNrAvailable = true isEnDcAvailable = true LteVopsSupportInfo :  mVopsSupport = 2 mEmcBearerSupport = 3 mIsUsingCarrierAggregation = false } nrState=CONNECTED}], mNrFrequencyRange=-1, mIsIwlanPreferred=false}"

        //5G available and connected
        private const val HUAWEI_MATE20X_5G_RESULT =
            "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=-1, duplexMode()=0, mCellBandwidths=[], mVoiceRoamingType=home, mDataRoamingType=home, mVoiceOperatorAlphaLong=vodafone IT, mVoiceOperatorAlphaShort=vodafone IT, mDataOperatorAlphaLong=vodafone IT, mDataOperatorAlphaShort=vodafone IT, isManualNetworkSelection=false(automatic), mRilVoiceRadioTechnology=20(NR), mRilDataRadioTechnology=20(NR), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, mIsDataRoamingFromRegistration=false, mIsUsingCarrierAggregation=false, mLteEarfcnRsrpBoost=0, mNetworkRegistrationStates=[NetworkRegistrationState{transportType=1 domain=PS regState=HOME accessNetworkTechnology=LTE-CA reasonForDenial=-1 emergencyEnabled=false supportedServices=[I@2c074ca cellIdentity=CellIdentityLte:{ mPci=218 mEarfcn=-1 mBandwidth=2147483647 mMcc=222 mMnc=10 mAlphaLong=null mAlphaShort=null} voiceSpecificStates=null dataSpecificStates=DataSpecificRegistrationStates { mMaxDataCalls=1} nsaState=5}, NetworkRegistrationState{transportType=1 domain=CS regState=HOME accessNetworkTechnology=LTE reasonForDenial=0 emergencyEnabled=false supportedServices=[I@dc3763b cellIdentity=CellIdentityLte:{ mPci=-1 mEarfcn=-1 mBandwidth=2147483647 mMcc=222 mMnc=10 mAlphaLong=null mAlphaShort=null} voiceSpecificStates=VoiceSpecificRegistrationStates { mCssSupported=false mRoamingIndicator=0 mSystemIsInPrl=0 mDefaultRoamingIndicator=0} dataSpecificStates=null nsaState=5}]}"

        //5G available but not connected or UpperLayerIndication
        private const val HUAWEI_MATE20X_5G_RESULT_2 =
            "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=-1, duplexMode()=0, mCellBandwidths=[], mVoiceRoamingType=home, mDataRoamingType=home, mVoiceOperatorAlphaLong=vodafone IT, mVoiceOperatorAlphaShort=vodafone IT, mDataOperatorAlphaLong=vodafone IT, mDataOperatorAlphaShort=vodafone IT, isManualNetworkSelection=false(automatic), mRilVoiceRadioTechnology=20(NR), mRilDataRadioTechnology=20(NR), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, mIsDataRoamingFromRegistration=false, mIsUsingCarrierAggregation=false, mLteEarfcnRsrpBoost=0, mNetworkRegistrationStates=[NetworkRegistrationState{transportType=1 domain=PS regState=HOME accessNetworkTechnology=LTE-CA reasonForDenial=-1 emergencyEnabled=false supportedServices=[I@2c074ca cellIdentity=CellIdentityLte:{ mPci=494 mEarfcn=-1 mBandwidth=2147483647 mMcc=222 mMnc=10 mAlphaLong=null mAlphaShort=null} voiceSpecificStates=null dataSpecificStates=DataSpecificRegistrationStates { mMaxDataCalls=1} nsaState=2}, NetworkRegistrationState{transportType=1 domain=CS regState=HOME accessNetworkTechnology=LTE reasonForDenial=0 emergencyEnabled=false supportedServices=[I@dc3763b cellIdentity=CellIdentityLte:{ mPci=-1 mEarfcn=-1 mBandwidth=2147483647 mMcc=222 mMnc=10 mAlphaLong=null mAlphaShort=null} voiceSpecificStates=VoiceSpecificRegistrationStates { mCssSupported=false mRoamingIndicator=0 mSystemIsInPrl=0 mDefaultRoamingIndicator=0} dataSpecificStates=null nsaState=2}]}"

        //5G NOT_RESTRICTED, Pixel 5, Android S B1
        private const val PIXEL_5_DISCONNECTED =
            "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=6200, duplexMode()=1, mCellBandwidths=[10000], mOperatorAlphaLong=T-Mobile CZ, mOperatorAlphaShort=T-Mobile CZ, isManualNetworkSelection=false(automatic), getRilVoiceRadioTechnology=14(LTE), getRilDataRadioTechnology=14(LTE), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, isUsingCarrierAggregation=false, mLteEarfcnRsrpBoost=0, mNetworkRegistrationInfos=[NetworkRegistrationInfo{ domain=PS transportType=WLAN registrationState=NOT_REG_OR_SEARCHING roamingType=NOT_ROAMING accessNetworkTechnology=IWLAN rejectCause=0 emergencyEnabled=false availableServices=[] cellIdentity=null voiceSpecificInfo=null dataSpecificInfo=null nrState=**** rRplmn= isUsingCarrierAggregation=false}, NetworkRegistrationInfo{ domain=CS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[VOICE,SMS,VIDEO] cellIdentity=CellIdentityLte:{ mCi=28384444 mPci=96 mTac=24530 mEarfcn=6200 mBands=[20] mBandwidth=10000 mMcc=230 mMnc=01 mAlphaLong=T-Mobile CZ mAlphaShort=T-Mobile CZ mAdditionalPlmns={} mCsgInfo=null} voiceSpecificInfo=VoiceSpecificRegistrationInfo { mCssSupported=false mRoamingIndicator=0 mSystemIsInPrl=0 mDefaultRoamingIndicator=0} dataSpecificInfo=null nrState=**** rRplmn=23001 isUsingCarrierAggregation=false}, NetworkRegistrationInfo{ domain=PS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[DATA] cellIdentity=CellIdentityLte:{ mCi=28384444 mPci=96 mTac=24530 mEarfcn=6200 mBands=[20] mBandwidth=10000 mMcc=230 mMnc=01 mAlphaLong=T-Mobile CZ mAlphaShort=T-Mobile CZ mAdditionalPlmns={} mCsgInfo=null} voiceSpecificInfo=null dataSpecificInfo=android.telephony.DataSpecificRegistrationInfo :{ maxDataCalls = 16 isDcNrRestricted = false isNrAvailable = false isEnDcAvailable = false LteVopsSupportInfo :  mVopsSupport = 2 mEmcBearerSupport = 2 } nrState=**** rRplmn=23001 isUsingCarrierAggregation=false}], mNrFrequencyRange=0, mOperatorAlphaLongRaw=T-Mobile CZ, mOperatorAlphaShortRaw=T-Mobile CZ, mIsDataRoamingFromRegistration=false, mIsIwlanPreferred=false}"


        //5G NOT_RESTRICTED, Pixel 5, Android S B1
        private const val PIXEL_5_NOT_RESTRICTED =
            "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=6200, duplexMode()=1, mCellBandwidths=[10000], mOperatorAlphaLong=T-Mobile CZ, mOperatorAlphaShort=T-Mobile CZ, isManualNetworkSelection=false(automatic), getRilVoiceRadioTechnology=14(LTE), getRilDataRadioTechnology=14(LTE), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, isUsingCarrierAggregation=false, mLteEarfcnRsrpBoost=0, mNetworkRegistrationInfos=[NetworkRegistrationInfo{ domain=PS transportType=WLAN registrationState=NOT_REG_OR_SEARCHING roamingType=NOT_ROAMING accessNetworkTechnology=IWLAN rejectCause=0 emergencyEnabled=false availableServices=[] cellIdentity=null voiceSpecificInfo=null dataSpecificInfo=null nrState=**** rRplmn= isUsingCarrierAggregation=false}, NetworkRegistrationInfo{ domain=CS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[VOICE,SMS,VIDEO] cellIdentity=CellIdentityLte:{ mCi=28384444 mPci=96 mTac=24530 mEarfcn=6200 mBands=[20] mBandwidth=10000 mMcc=230 mMnc=01 mAlphaLong=T-Mobile CZ mAlphaShort=T-Mobile CZ mAdditionalPlmns={} mCsgInfo=null} voiceSpecificInfo=VoiceSpecificRegistrationInfo { mCssSupported=false mRoamingIndicator=0 mSystemIsInPrl=0 mDefaultRoamingIndicator=0} dataSpecificInfo=null nrState=**** rRplmn=23001 isUsingCarrierAggregation=false}, NetworkRegistrationInfo{ domain=PS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[DATA] cellIdentity=CellIdentityLte:{ mCi=28384444 mPci=96 mTac=24530 mEarfcn=6200 mBands=[20] mBandwidth=10000 mMcc=230 mMnc=01 mAlphaLong=T-Mobile CZ mAlphaShort=T-Mobile CZ mAdditionalPlmns={} mCsgInfo=null} voiceSpecificInfo=null dataSpecificInfo=android.telephony.DataSpecificRegistrationInfo :{ maxDataCalls = 16 isDcNrRestricted = false isNrAvailable = true isEnDcAvailable = true LteVopsSupportInfo :  mVopsSupport = 2 mEmcBearerSupport = 2 } nrState=**** rRplmn=23001 isUsingCarrierAggregation=false}], mNrFrequencyRange=0, mOperatorAlphaLongRaw=T-Mobile CZ, mOperatorAlphaShortRaw=T-Mobile CZ, mIsDataRoamingFromRegistration=false, mIsIwlanPreferred=false}"

        //5G RESTRICTED, Roaming, Pixel 5, Android S B1
        private const val PIXEL_5_RESTRICTED =
            "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=6250, duplexMode()=1, mCellBandwidths=[20000], mOperatorAlphaLong=A1, mOperatorAlphaShort=A1, isManualNetworkSelection=false(automatic), getRilVoiceRadioTechnology=14(LTE), getRilDataRadioTechnology=14(LTE), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, isUsingCarrierAggregation=false, mLteEarfcnRsrpBoost=0, mNetworkRegistrationInfos=[NetworkRegistrationInfo{ domain=PS transportType=WLAN registrationState=NOT_REG_OR_SEARCHING roamingType=NOT_ROAMING accessNetworkTechnology=IWLAN rejectCause=0 emergencyEnabled=false availableServices=[] cellIdentity=null voiceSpecificInfo=null dataSpecificInfo=null nrState=**** rRplmn= isUsingCarrierAggregation=false}, NetworkRegistrationInfo{ domain=CS transportType=WWAN registrationState=ROAMING roamingType=INTERNATIONAL accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[VOICE,SMS,VIDEO] cellIdentity=CellIdentityLte:{ mCi=1832711 mPci=49 mTac=20263 mEarfcn=6250 mBands=[20] mBandwidth=20000 mMcc=232 mMnc=01 mAlphaLong=A1 mAlphaShort=A1 mAdditionalPlmns={} mCsgInfo=null} voiceSpecificInfo=VoiceSpecificRegistrationInfo { mCssSupported=false mRoamingIndicator=0 mSystemIsInPrl=0 mDefaultRoamingIndicator=0} dataSpecificInfo=null nrState=**** rRplmn=23201 isUsingCarrierAggregation=false}, NetworkRegistrationInfo{ domain=PS transportType=WWAN registrationState=ROAMING roamingType=INTERNATIONAL accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[DATA] cellIdentity=CellIdentityLte:{ mCi=1832711 mPci=49 mTac=20263 mEarfcn=6250 mBands=[20] mBandwidth=20000 mMcc=232 mMnc=01 mAlphaLong=A1 mAlphaShort=A1 mAdditionalPlmns={} mCsgInfo=null} voiceSpecificInfo=null dataSpecificInfo=android.telephony.DataSpecificRegistrationInfo :{ maxDataCalls = 16 isDcNrRestricted = true isNrAvailable = true isEnDcAvailable = false LteVopsSupportInfo :  mVopsSupport = 3 mEmcBearerSupport = 3 } nrState=**** rRplmn=23201 isUsingCarrierAggregation=false}], mNrFrequencyRange=0, mOperatorAlphaLongRaw=A1, mOperatorAlphaShortRaw=A1, mIsDataRoamingFromRegistration=true, mIsIwlanPreferred=false}"
    }

    init {

        "Pixel 2 XL result" {
            parser.parse(PIXEL_2_XL_RESULT, emptyList()) shouldBe NrNsaState(
                enDcAvailable = false,
                nrAvailable = false,
                connection = NrNsaState.Connection.Disconnected
            )

        }

        "Samsung S10 result" {
            parser.parse(SAMSUNG_S10_5G_RESULT, emptyList()) shouldBe NrNsaState(
                enDcAvailable = true,
                nrAvailable = true,
                connection = NrNsaState.Connection.Connected
            )
        }

        "Mate 20X 5G result" {
            parser.parse(HUAWEI_MATE20X_5G_RESULT, emptyList()) shouldBe NrNsaState(
                enDcAvailable = true,
                nrAvailable = true,
                connection = NrNsaState.Connection.Connected
            )
        }

        "Mate 20X 5G result 2" {
            parser.parse(HUAWEI_MATE20X_5G_RESULT_2, emptyList()) shouldBe NrNsaState(
                enDcAvailable = false,
                nrAvailable = true,
                connection = NrNsaState.Connection.Disconnected
            )
        }

        "Pixel 5, Android S, RESTRICTED" {
            parser.parse(PIXEL_5_RESTRICTED, emptyList()) shouldBe NrNsaState(
                enDcAvailable = false,
                nrAvailable = true,
                connection = NrNsaState.Connection.Rejected(NrNsaState.RejectionReason.RESTRICTED)
            )
        }

        "Pixel 5, Android S, DISCONNECTED" {
            parser.parse(PIXEL_5_DISCONNECTED, emptyList()) shouldBe NrNsaState(
                enDcAvailable = false,
                nrAvailable = false,
                connection = NrNsaState.Connection.Disconnected
            )
        }


        "Pixel 5, Android S, NOT_RESTRICTED" {
            parser.parse(PIXEL_5_NOT_RESTRICTED, emptyList()) shouldBe NrNsaState(
                enDcAvailable = true,
                nrAvailable = true,
                connection = NrNsaState.Connection.Rejected(NrNsaState.RejectionReason.NOT_RESTRICTED)
            )
        }

        "Pixel 5, Android S, DISCONNECTED with LTE and NR cell" {
            parser.parse(
                serviceState = PIXEL_5_DISCONNECTED,
                cells = listOf(
                    CellLte(
                        network = Network.map("23001"),
                        eci = 28384444,
                        pci = 96,
                        tac = 24530,
                        band = BandTableLte.map(6200),
                        bandwidth = 10_000,
                        signal = SignalLte(null, null, null, null, null, null),
                        connectionStatus = PrimaryConnection(),
                        subscriptionId = 1,
                        timestamp = null,
                    ),
                    CellNr(
                        network = null,
                        nci = null,
                        tac = null,
                        pci = null,
                        band = null,
                        signal = SignalNr(),
                        connectionStatus = SecondaryConnection(false),
                        subscriptionId = 1,
                        timestamp = null,
                    )
                )
            ) shouldBe NrNsaState(
                enDcAvailable = false,
                nrAvailable = false,
                connection = NrNsaState.Connection.Disconnected
            )
        }

        "Pixel 5, Android S, with LTE and NR cell" {
            // If we have secondarily serving NR cell then NSA NR is definitelly connected
            parser.parse(
                serviceState = PIXEL_5_NOT_RESTRICTED,
                cells = listOf(
                    CellLte(
                        network = Network.map("23001"),
                        eci = 28384444,
                        pci = 96,
                        tac = 24530,
                        band = BandTableLte.map(6200),
                        bandwidth = 10_000,
                        signal = SignalLte(null, null, null, null, null, null),
                        connectionStatus = PrimaryConnection(),
                        subscriptionId = 1,
                        timestamp = null,
                    ),
                    CellNr(
                        network = null,
                        nci = null,
                        tac = null,
                        pci = null,
                        band = null,
                        signal = SignalNr(),
                        connectionStatus = SecondaryConnection(false),
                        subscriptionId = 1,
                        timestamp = null,
                    )
                )
            ) shouldBe NrNsaState(
                enDcAvailable = true,
                nrAvailable = true,
                connection = NrNsaState.Connection.Connected
            )
        }
    }

}