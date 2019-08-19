package cz.mroczis.netmonster.core.feature.detect

import android.os.Build
import cz.mroczis.netmonster.core.SdkTest
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import io.kotlintest.shouldBe

class DetectorLteAdvancedServiceStateTest : SdkTest(Build.VERSION_CODES.N) {

    private val detector = DetectorLteAdvancedServiceState()

    companion object {
        private const val PIXEL_2_XL_RESULT =
            "{mVoiceRegState=0(IN_SERVICE), mDataRegState=0(IN_SERVICE), mChannelNumber=6400, duplexMode()=1, mCellBandwidths=[], mVoiceOperatorAlphaLong=Vodafone CZ, mVoiceOperatorAlphaShort=Vodafone CZ, mDataOperatorAlphaLong=Vodafone CZ, mDataOperatorAlphaShort=Vodafone CZ, isManualNetworkSelection=false(automatic), getRilVoiceRadioTechnology=14(LTE), getRilDataRadioTechnology=14(LTE), mCssIndicator=unsupported, mNetworkId=-1, mSystemId=-1, mCdmaRoamingIndicator=-1, mCdmaDefaultRoamingIndicator=-1, mIsEmergencyOnly=false, isUsingCarrierAggregation=true, mLteEarfcnRsrpBoost=0, mNetworkRegistrationInfos=[NetworkRegistrationInfo{ domain=CS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[VOICE,SMS,VIDEO] cellIdentity=CellIdentityLte:{ mCi=263291 mPci=96 mTac=44600 mEarfcn=6400 mBandwidth=2147483647 mMcc=230 mMnc=03 mAlphaLong= mAlphaShort=} voiceSpecificInfo=VoiceSpecificRegistrationInfo { mCssSupported=false mRoamingIndicator=1 mSystemIsInPrl=-1 mDefaultRoamingIndicator=-1} dataSpecificInfo=null nrState=NONE}, NetworkRegistrationInfo{ domain=PS transportType=WWAN registrationState=HOME roamingType=NOT_ROAMING accessNetworkTechnology=LTE rejectCause=0 emergencyEnabled=false availableServices=[DATA] cellIdentity=CellIdentityLte:{ mCi=263291 mPci=96 mTac=44600 mEarfcn=6400 mBandwidth=2147483647 mMcc=230 mMnc=03 mAlphaLong= mAlphaShort=} voiceSpecificInfo=null dataSpecificInfo=android.telephony.DataSpecificRegistrationInfo :{ maxDataCalls = 20 isDcNrRestricted = false isNrAvailable = false isEnDcAvailable = false LteVopsSupportInfo :  mVopsSupport = 1 mEmcBearerSupport = 1 mIsUsingCarrierAggregation = true } nrState=NONE}], mNrFrequencyRange=-1, mOperatorAlphaLongRaw=Vodafone CZ, mOperatorAlphaShortRaw=Vodafone CZ, mIsIwlanPreferred=false}"
    }

    init {

        "Pixel 2 XL result" {
            detector.detect(PIXEL_2_XL_RESULT) shouldBe NetworkTypeTable.get(NetworkType.LTE_CA)
        }

    }

}