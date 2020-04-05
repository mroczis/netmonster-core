package cz.mroczis.netmonster.core.feature.detect

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import io.kotlintest.shouldBe

class DetectorLteAdvancedNrServiceStateTest : SdkTest(Build.VERSION_CODES.N) {

    private val detector = DetectorLteAdvancedNrServiceState()

    companion object {
        private const val PIXEL_2_XL_RESULT =
            "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=6400, duplexMode()=1, mCellBandwidths=[], mVoiceOperatorAlphaLong=Vodafone CZ, mVoiceOperatorAlphaShort=Vodafone CZ, mDataOperatorAlphaLong=Vodafone CZ, mDataOperatorAlphaShort=Vodafone CZ, isManualNetworkSelection=false(automatic), getRilVoiceRadioTechnology=14(LTE), getRilDataRadioTechnology=14(LTE), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, isUsingCarrierAggregation=true, mLteEarfcnRsrpBoost=0, mNetworkRegistrationInfos=[NetworkRegistrationInfo{ domain=CS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[VOICE,SMS,VIDEO] cellIdentity=CellIdentityLte:{ mCi=263291 mPci=96 mTac=44600 mEarfcn=6400 mBandwidth=2147483647 mMcc=230 mMnc=03 mAlphaLong= mAlphaShort=} voiceSpecificInfo=VoiceSpecificRegistrationInfo { mCssSupported=false mRoamingIndicator=1 mSystemIsInPrl=-1 mDefaultRoamingIndicator=-1} dataSpecificInfo=null nrState=NONE}, NetworkRegistrationInfo{ domain=PS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[DATA] cellIdentity=CellIdentityLte:{ mCi=263291 mPci=96 mTac=44600 mEarfcn=6400 mBandwidth=2147483647 mMcc=230 mMnc=03 mAlphaLong= mAlphaShort=} voiceSpecificInfo=null dataSpecificInfo=android.telephony.DataSpecificRegistrationInfo :{ maxDataCalls = 20 isDcNrRestricted = false isNrAvailable = false isEnDcAvailable = false LteVopsSupportInfo :  mVopsSupport = 1 mEmcBearerSupport = 1 mIsUsingCarrierAggregation = true } nrState=NONE}], mNrFrequencyRange=-1, mOperatorAlphaLongRaw=Vodafone CZ, mOperatorAlphaShortRaw=Vodafone CZ, mIsIwlanPreferred=false}"

        private const val SAMSUNG_S10_5G_RESULT =
            "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=199, duplexMode()=1, mCellBandwidths=[], isManualNetworkSelection=false(automatic), getRilVoiceRadioTechnology=14(LTE), getRilDataRadioTechnology=14(LTE), mCssIndicator=unsupported, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, VoiceRegType=0, ImsVoiceAvail=2, Snap=0, MobileVoice=IN_SERVICE, MobileVoiceRat=Unknown, MobileData=IN_SERVICE, MobileDataRoamingType=home, MobileDataRat=LTE PsOnly=false FemtocellInd=0 SprDisplayRoam=false EndcStatus=1 RestrictDcnr=0 NrBearerStatus=1 5gStatus=1 RRCState=1, mIsEmergencyOnly=false, isUsingCarrierAggregation=false, mLteEarfcnRsrpBoost=0, mNetworkRegistrationInfos=[NetworkRegistrationInfo{ domain=CS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[VOICE,SMS,VIDEO] cellIdentity=CellIdentityLte:{ mCi=1 mPci=2 mTac=3 mEarfcn=199 mBandwidth=0 mMcc=234 mMnc=10 mAlphaLong=O2 - UK mAlphaShort=O2 - UK} voiceSpecificInfo=VoiceSpecificRegistrationInfo { mCssSupported=false mRoamingIndicator=0 mSystemIsInPrl=0 mDefaultRoamingIndicator=0} dataSpecificInfo=null nrState=NONE}, NetworkRegistrationInfo{ domain=PS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[DATA] cellIdentity=CellIdentityLte:{ mCi=1 mPci=2 mTac=3 mEarfcn=4 mBandwidth=0 mMcc=234 mMnc=10 mAlphaLong=O2 - UK mAlphaShort=O2 - UK} voiceSpecificInfo=null dataSpecificInfo=android.telephony.DataSpecificRegistrationInfo :{ maxDataCalls = 4 isDcNrRestricted = false isNrAvailable = true isEnDcAvailable = true LteVopsSupportInfo :  mVopsSupport = 2 mEmcBearerSupport = 3 mIsUsingCarrierAggregation = false } nrState=CONNECTED}], mNrFrequencyRange=-1, mIsIwlanPreferred=false}"

        //LTE-A and no 5G
        private const val LG_G7_RESULT = "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=350, duplexMode()=1, mCellBandwidths=[10000, 20000, 20000], mVoiceRoamingType=home, mDataRoamingType=home, mVoiceOperatorAlphaLong=3ITA, mVoiceOperatorAlphaShort=3ITA, mDataOperatorAlphaLong=3ITA, mDataOperatorAlphaShort=3ITA, isManualNetworkSelection=false(automatic), mRilVoiceRadioTechnology=14(LTE), mRilDataRadioTechnology=14(LTE), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, mIsDataRoamingFromRegistration=false, mIsUsingCarrierAggregation=false, mLteEarfcnRsrpBoost=0, mNetworkRegistrationStates=[NetworkRegistrationState{transportType=1 domain=PS regState=HOME accessNetworkTechnology=LTE reasonForDenial=0 emergencyEnabled=false supportedServices=[I@5caff1f cellIdentity=CellIdentityLte:{ mCi=83970067 mPci=301 mTac=33019 mEarfcn=350 mBandwidth=2147483647 mMcc=222 mMnc=99 mAlphaLong=null mAlphaShort=null} voiceSpecificStates=null dataSpecificStates=DataSpecificRegistrationStates { mMaxDataCalls=20} rawRegState=1 endcAvailable=false dcnrRestricted=false 5gAllocated=false}, NetworkRegistrationState{transportType=1 domain=CS regState=HOME accessNetworkTechnology=LTE reasonForDenial=0 emergencyEnabled=false supportedServices=[I@224426c cellIdentity=CellIdentityLte:{ mCi=83970067 mPci=301 mTac=33019 mEarfcn=350 mBandwidth=10000 mMcc=222 mMnc=99 mAlphaLong=3ITA mAlphaShort=3ITA} voiceSpecificStates=VoiceSpecificRegistrationStates { mCssSupported=false mRoamingIndicator=1 mSystemIsInPrl=-1 mDefaultRoamingIndicator=-1} dataSpecificStates=null rawRegState=1 endcAvailable=false dcnrRestricted=false 5gAllocated=false}] mIsVoiceSearching=false mIsDataSearching=false Check64QAM0 Dual carrier0 LTE AdvanceMode1 EnDc=false DcNr Restricted=false 5G Allocated=false}"

        //5G available and connected
        private const val HUAWEI_MATE20X_5G_RESULT = "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=-1, duplexMode()=0, mCellBandwidths=[], mVoiceRoamingType=home, mDataRoamingType=home, mVoiceOperatorAlphaLong=vodafone IT, mVoiceOperatorAlphaShort=vodafone IT, mDataOperatorAlphaLong=vodafone IT, mDataOperatorAlphaShort=vodafone IT, isManualNetworkSelection=false(automatic), mRilVoiceRadioTechnology=20(NR), mRilDataRadioTechnology=20(NR), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, mIsDataRoamingFromRegistration=false, mIsUsingCarrierAggregation=false, mLteEarfcnRsrpBoost=0, mNetworkRegistrationStates=[NetworkRegistrationState{transportType=1 domain=PS regState=HOME accessNetworkTechnology=LTE-CA reasonForDenial=-1 emergencyEnabled=false supportedServices=[I@2c074ca cellIdentity=CellIdentityLte:{ mPci=218 mEarfcn=-1 mBandwidth=2147483647 mMcc=222 mMnc=10 mAlphaLong=null mAlphaShort=null} voiceSpecificStates=null dataSpecificStates=DataSpecificRegistrationStates { mMaxDataCalls=1} nsaState=5}, NetworkRegistrationState{transportType=1 domain=CS regState=HOME accessNetworkTechnology=LTE reasonForDenial=0 emergencyEnabled=false supportedServices=[I@dc3763b cellIdentity=CellIdentityLte:{ mPci=-1 mEarfcn=-1 mBandwidth=2147483647 mMcc=222 mMnc=10 mAlphaLong=null mAlphaShort=null} voiceSpecificStates=VoiceSpecificRegistrationStates { mCssSupported=false mRoamingIndicator=0 mSystemIsInPrl=0 mDefaultRoamingIndicator=0} dataSpecificStates=null nsaState=5}]}"

        //5G available but not connected or UpperLayerIndication
        private const val HUAWEI_MATE20X_5G_RESULT_2 = "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=-1, duplexMode()=0, mCellBandwidths=[], mVoiceRoamingType=home, mDataRoamingType=home, mVoiceOperatorAlphaLong=vodafone IT, mVoiceOperatorAlphaShort=vodafone IT, mDataOperatorAlphaLong=vodafone IT, mDataOperatorAlphaShort=vodafone IT, isManualNetworkSelection=false(automatic), mRilVoiceRadioTechnology=20(NR), mRilDataRadioTechnology=20(NR), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, mIsDataRoamingFromRegistration=false, mIsUsingCarrierAggregation=false, mLteEarfcnRsrpBoost=0, mNetworkRegistrationStates=[NetworkRegistrationState{transportType=1 domain=PS regState=HOME accessNetworkTechnology=LTE-CA reasonForDenial=-1 emergencyEnabled=false supportedServices=[I@2c074ca cellIdentity=CellIdentityLte:{ mPci=494 mEarfcn=-1 mBandwidth=2147483647 mMcc=222 mMnc=10 mAlphaLong=null mAlphaShort=null} voiceSpecificStates=null dataSpecificStates=DataSpecificRegistrationStates { mMaxDataCalls=1} nsaState=2}, NetworkRegistrationState{transportType=1 domain=CS regState=HOME accessNetworkTechnology=LTE reasonForDenial=0 emergencyEnabled=false supportedServices=[I@dc3763b cellIdentity=CellIdentityLte:{ mPci=-1 mEarfcn=-1 mBandwidth=2147483647 mMcc=222 mMnc=10 mAlphaLong=null mAlphaShort=null} voiceSpecificStates=VoiceSpecificRegistrationStates { mCssSupported=false mRoamingIndicator=0 mSystemIsInPrl=0 mDefaultRoamingIndicator=0} dataSpecificStates=null nsaState=2}]}"

    }

    init {

        "Pixel 2 XL result" {
            detector.isUsingCarrierAggregation(PIXEL_2_XL_RESULT) shouldBe true
            detector.is5gActive(PIXEL_2_XL_RESULT) shouldBe false
        }

        "Samsung S10 result" {
            detector.isUsingCarrierAggregation(SAMSUNG_S10_5G_RESULT) shouldBe false
            detector.is5gActive(SAMSUNG_S10_5G_RESULT) shouldBe true
        }

        "LG LTE-A result" {
            detector.isUsingCarrierAggregation(LG_G7_RESULT) shouldBe true
            detector.is5gActive(LG_G7_RESULT) shouldBe false
        }

        "Mate 20X 5G result" {
            detector.isUsingCarrierAggregation(HUAWEI_MATE20X_5G_RESULT) shouldBe true
            detector.is5gActive(HUAWEI_MATE20X_5G_RESULT) shouldBe true
        }

        "Mate 20X 5G result 2" {
            detector.isUsingCarrierAggregation(HUAWEI_MATE20X_5G_RESULT_2) shouldBe true
            detector.is5gActive(HUAWEI_MATE20X_5G_RESULT_2) shouldBe false
        }
    }

}