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

    }

}