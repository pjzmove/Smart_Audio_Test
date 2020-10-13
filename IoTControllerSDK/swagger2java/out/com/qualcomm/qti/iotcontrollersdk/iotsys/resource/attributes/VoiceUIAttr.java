/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcException;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;

public class VoiceUIAttr implements IResourceAttributes {


    public List<String>  mASRProviders;
    public String mAVSCredentialFile;
    public String mAVSLocale;
    public List<String>  mAVSLocaleList;
    public boolean mAVSOnboarded;
    public AVSOnboardingErrorAttr mAVSOnboardingError;
    public List<String>  mNLUProviders;
    public List<String>  mTTSProviders;
    public AuthenticateAVSAttr mAuthenticateAVS;
    public String mDefaultVoiceUIClient;
    public boolean mEnableVoiceUI;
    public List<String>  mModularClientLanguages;
    public String mSelectedASRProvider;
    public String mSelectedModularClientLanguage;
    public String mSelectedNLUProvider;
    public String mSelectedTTSProvider;
    public List<VoiceUIClientAttr>  mVoiceUIClients;

     public VoiceUIAttr() {
        mASRProviders = new ArrayList<>();
        mAVSCredentialFile = "";
        mAVSLocale = "";
        mAVSLocaleList = new ArrayList<>();
        mAVSOnboarded = false;
        mAVSOnboardingError = new AVSOnboardingErrorAttr();
        mNLUProviders = new ArrayList<>();
        mTTSProviders = new ArrayList<>();
        mAuthenticateAVS = new AuthenticateAVSAttr();
        mDefaultVoiceUIClient = "";
        mEnableVoiceUI = false;
        mModularClientLanguages = new ArrayList<>();
        mSelectedASRProvider = "";
        mSelectedModularClientLanguage = "";
        mSelectedNLUProvider = "";
        mSelectedTTSProvider = "";
        mVoiceUIClients = new ArrayList<>();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/voice_ui_rep_uri");

         rep.setValue(ResourceAttributes.Prop_AVSLocale,mAVSLocale);
         rep.setValue(ResourceAttributes.Prop_defaultVoiceUIClient,mDefaultVoiceUIClient);
         rep.setValue(ResourceAttributes.Prop_enableVoiceUI,mEnableVoiceUI);
         rep.setValue(ResourceAttributes.Prop_selectedASRProvider,mSelectedASRProvider);
         rep.setValue(ResourceAttributes.Prop_selectedModularClientLanguage,mSelectedModularClientLanguage);
         rep.setValue(ResourceAttributes.Prop_selectedNLUProvider,mSelectedNLUProvider);
         rep.setValue(ResourceAttributes.Prop_selectedTTSProvider,mSelectedTTSProvider);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_ASRProviders)) {
            didUnpack = true;
            mASRProviders = ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_ASRProviders);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_AVSCredentialFile)) {
            didUnpack = true;
            mAVSCredentialFile = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_AVSCredentialFile);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_AVSLocale)) {
            didUnpack = true;
            mAVSLocale = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_AVSLocale);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_AVSLocaleList)) {
            didUnpack = true;
            mAVSLocaleList = ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_AVSLocaleList);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_AVSOnboarded)) {
            didUnpack = true;
            mAVSOnboarded = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_AVSOnboarded);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_AVSOnboardingError)) {
            didUnpack = true;
            AVSOnboardingErrorAttr obj = new AVSOnboardingErrorAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_AVSOnboardingError)))
                mAVSOnboardingError = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_NLUProviders)) {
            didUnpack = true;
            mNLUProviders = ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_NLUProviders);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_TTSProviders)) {
            didUnpack = true;
            mTTSProviders = ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_TTSProviders);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_authenticateAVS)) {
            didUnpack = true;
            AuthenticateAVSAttr obj = new AuthenticateAVSAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_authenticateAVS)))
                mAuthenticateAVS = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_defaultVoiceUIClient)) {
            didUnpack = true;
            mDefaultVoiceUIClient = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_defaultVoiceUIClient);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_enableVoiceUI)) {
            didUnpack = true;
            mEnableVoiceUI = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enableVoiceUI);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_modularClientLanguages)) {
            didUnpack = true;
            mModularClientLanguages = ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_modularClientLanguages);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_selectedASRProvider)) {
            didUnpack = true;
            mSelectedASRProvider = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_selectedASRProvider);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_selectedModularClientLanguage)) {
            didUnpack = true;
            mSelectedModularClientLanguage = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_selectedModularClientLanguage);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_selectedNLUProvider)) {
            didUnpack = true;
            mSelectedNLUProvider = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_selectedNLUProvider);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_selectedTTSProvider)) {
            didUnpack = true;
            mSelectedTTSProvider = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_selectedTTSProvider);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_voiceUIClients)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_voiceUIClients);
            if(reps != null) {
                mVoiceUIClients.clear();
                for (OcRepresentation elem_rep: reps) {
                    VoiceUIClientAttr obj = new VoiceUIClientAttr();
                    if(obj.unpack(elem_rep))
                        mVoiceUIClients.add(obj);
                }
            }
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    VoiceUIAttr clonedObj = new VoiceUIAttr();
    clonedObj.mASRProviders = this.mASRProviders;
    clonedObj.mAVSCredentialFile = this.mAVSCredentialFile;
    clonedObj.mAVSLocale = this.mAVSLocale;
    clonedObj.mAVSLocaleList = this.mAVSLocaleList;
    clonedObj.mAVSOnboarded = this.mAVSOnboarded;
    clonedObj.mAVSOnboardingError = (AVSOnboardingErrorAttr)mAVSOnboardingError.getData();
    clonedObj.mNLUProviders = this.mNLUProviders;
    clonedObj.mTTSProviders = this.mTTSProviders;
    clonedObj.mAuthenticateAVS = (AuthenticateAVSAttr)mAuthenticateAVS.getData();
    clonedObj.mDefaultVoiceUIClient = this.mDefaultVoiceUIClient;
    clonedObj.mEnableVoiceUI = this.mEnableVoiceUI;
    clonedObj.mModularClientLanguages = this.mModularClientLanguages;
    clonedObj.mSelectedASRProvider = this.mSelectedASRProvider;
    clonedObj.mSelectedModularClientLanguage = this.mSelectedModularClientLanguage;
    clonedObj.mSelectedNLUProvider = this.mSelectedNLUProvider;
    clonedObj.mSelectedTTSProvider = this.mSelectedTTSProvider;
    for(VoiceUIClientAttr item:mVoiceUIClients) {
        clonedObj.mVoiceUIClients.add((VoiceUIClientAttr)item.getData());
    }
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof VoiceUIAttr) {
        VoiceUIAttr obj = (VoiceUIAttr) state;
        this.mASRProviders = obj.mASRProviders;
        this.mAVSCredentialFile = obj.mAVSCredentialFile;
        this.mAVSLocale = obj.mAVSLocale;
        this.mAVSLocaleList = obj.mAVSLocaleList;
        this.mAVSOnboarded = obj.mAVSOnboarded;
        this.mAVSOnboardingError.setData(obj.mAVSOnboardingError);
        this.mNLUProviders = obj.mNLUProviders;
        this.mTTSProviders = obj.mTTSProviders;
        this.mAuthenticateAVS.setData(obj.mAuthenticateAVS);
        this.mDefaultVoiceUIClient = obj.mDefaultVoiceUIClient;
        this.mEnableVoiceUI = obj.mEnableVoiceUI;
        this.mModularClientLanguages = obj.mModularClientLanguages;
        this.mSelectedASRProvider = obj.mSelectedASRProvider;
        this.mSelectedModularClientLanguage = obj.mSelectedModularClientLanguage;
        this.mSelectedNLUProvider = obj.mSelectedNLUProvider;
        this.mSelectedTTSProvider = obj.mSelectedTTSProvider;
        this.mVoiceUIClients.clear();
        for(VoiceUIClientAttr item:obj.mVoiceUIClients) {
            this.mVoiceUIClients.add((VoiceUIClientAttr)item.getData());
        }
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_ASRProviders)) {
            didChanged = (!mASRProviders.equals(ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_ASRProviders)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_AVSCredentialFile)) {
            didChanged = (!mAVSCredentialFile.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_AVSCredentialFile)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_AVSLocale)) {
            didChanged = (!mAVSLocale.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_AVSLocale)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_AVSLocaleList)) {
            didChanged = (!mAVSLocaleList.equals(ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_AVSLocaleList)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_AVSOnboarded)) {
            didChanged = (mAVSOnboarded != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_AVSOnboarded));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_AVSOnboardingError)) {
            didChanged = (mAVSOnboardingError.checkDifference(rep.getValue(ResourceAttributes.Prop_AVSOnboardingError)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_NLUProviders)) {
            didChanged = (!mNLUProviders.equals(ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_NLUProviders)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_TTSProviders)) {
            didChanged = (!mTTSProviders.equals(ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_TTSProviders)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_authenticateAVS)) {
            didChanged = (mAuthenticateAVS.checkDifference(rep.getValue(ResourceAttributes.Prop_authenticateAVS)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_defaultVoiceUIClient)) {
            didChanged = (!mDefaultVoiceUIClient.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_defaultVoiceUIClient)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_enableVoiceUI)) {
            didChanged = (mEnableVoiceUI != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enableVoiceUI));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_modularClientLanguages)) {
            didChanged = (!mModularClientLanguages.equals(ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_modularClientLanguages)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_selectedASRProvider)) {
            didChanged = (!mSelectedASRProvider.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_selectedASRProvider)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_selectedModularClientLanguage)) {
            didChanged = (!mSelectedModularClientLanguage.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_selectedModularClientLanguage)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_selectedNLUProvider)) {
            didChanged = (!mSelectedNLUProvider.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_selectedNLUProvider)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_selectedTTSProvider)) {
            didChanged = (!mSelectedTTSProvider.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_selectedTTSProvider)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_voiceUIClients)) {
            reps = rep.getValue(ResourceAttributes.Prop_voiceUIClients);
            if(reps != null) {
                if(mVoiceUIClients.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mVoiceUIClients.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        return false;
   }
}