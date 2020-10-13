/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.iotsys.state;

import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.AVSOnboardingErrorAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.AuthenticateAVSAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.VoiceUIAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.VoiceUIClientAttr;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.ResourceState;
import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class VoiceUiState extends ResourceState {

  private final VoiceUIAttr mVoiceUIAttr = new VoiceUIAttr();

  public static final String AVSClientName = "AVS";
  public static final String CortanaClientName  = "CORTANA";
  public static final String GVAClientName  = "GVA";
  public static final String ModularClientName  = "MODULAR";

  public VoiceUiState() {
      isAvailable = false;
  }

  public synchronized void update(VoiceUIAttr attr) {
      GenericStateApi.setState(mVoiceUIAttr, attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
     return GenericStateApi.updateState(mVoiceUIAttr, rep);
  }

  public synchronized VoiceUIAttr getAttribute() {
     return GenericStateApi.getState(mVoiceUIAttr);
  }

  public synchronized List<String> getASRProviders() {
     return GenericStateApi.getPrimitiveTypeList(mVoiceUIAttr.mASRProviders);
  }

  public synchronized void setASRProviders(List<String> providers) {
    GenericStateApi.setPrimitiveTypeList(mVoiceUIAttr.mASRProviders, providers);
  }

  public synchronized String getAVSCredentialFile() {
     return GenericStateApi.getState(mVoiceUIAttr.mAVSCredentialFile);
  }

  public synchronized void setAVSCredentialFile(String AVSCredentialFile) {
    mVoiceUIAttr.mAVSCredentialFile = AVSCredentialFile;
  }

  public synchronized String getAVSLocale() {
    return mVoiceUIAttr.mAVSLocale;
  }

  public synchronized void setAVSLocale(String AVSLocale) {
    mVoiceUIAttr.mAVSLocale = AVSLocale;
  }

  public synchronized List<String> getAVSLocaleList() {
    return GenericStateApi.getPrimitiveTypeList(mVoiceUIAttr.mAVSLocaleList);
  }

  public synchronized void setAVSLocaleList(List<String> AVSLocaleList) {
    GenericStateApi.setPrimitiveTypeList(mVoiceUIAttr.mAVSLocaleList,AVSLocaleList);
  }

  public synchronized boolean isAVSOnboarded() {
    return mVoiceUIAttr.mAVSOnboarded;
  }

  public synchronized void setAVSOnboarded(boolean AVSOnboarded) {
    mVoiceUIAttr.mAVSOnboarded = AVSOnboarded;
  }

  public synchronized AVSOnboardingErrorAttr getAVSOnboardingError() {
      return GenericStateApi.getState(mVoiceUIAttr.mAVSOnboardingError);
  }

  public synchronized void setAVSOnboardingError(AVSOnboardingErrorAttr AVSOnboardingError) {
    GenericStateApi.setState(mVoiceUIAttr.mAVSOnboardingError,AVSOnboardingError);
  }

  public synchronized List<String> getNLUProviders() {
      return GenericStateApi.getPrimitiveTypeList(mVoiceUIAttr.mNLUProviders);
  }

  public synchronized void setNLUProviders(List<String> NLUProviders) {
    mVoiceUIAttr.mNLUProviders = NLUProviders;
  }

  public synchronized List<String> getTTSProviders() {
      return GenericStateApi.getPrimitiveTypeList(mVoiceUIAttr.mTTSProviders);
  }

  public synchronized void setTTSProviders(List<String> TTSProviders) {
    mVoiceUIAttr.mTTSProviders = TTSProviders;
  }

  public synchronized AuthenticateAVSAttr getAuthenticateAVS() {
      return GenericStateApi.getState(mVoiceUIAttr.mAuthenticateAVS);
  }

  public synchronized void setAuthenticateAVS(AuthenticateAVSAttr authenticateAVS) {
    GenericStateApi.setState(mVoiceUIAttr.mAuthenticateAVS,authenticateAVS);
  }

  public synchronized String getDefaultVoiceUIClient() {
      return mVoiceUIAttr.mDefaultVoiceUIClient;
  }

  public synchronized void setDefaultVoiceUIClient(String defaultVoiceUIClient) {
    mVoiceUIAttr.mDefaultVoiceUIClient = defaultVoiceUIClient;
  }

  public synchronized boolean isEnableVoiceUI() {
      return mVoiceUIAttr.mEnableVoiceUI;
  }

  public synchronized void setEnableVoiceUI(boolean enableVoiceUI) {
    mVoiceUIAttr.mEnableVoiceUI = enableVoiceUI;
  }

  public synchronized List<String> getModularClientLanguages() {
      return mVoiceUIAttr.mModularClientLanguages;
  }

  public synchronized void setModularClientLanguages(List<String> modularClientLanguages) {
    mVoiceUIAttr.mModularClientLanguages = modularClientLanguages;
  }

  public synchronized String getSelectedASRProvider() {
      return mVoiceUIAttr.mSelectedASRProvider;
  }

  public synchronized void setSelectedASRProvider(String selectedASRProvider) {
    mVoiceUIAttr.mSelectedASRProvider = selectedASRProvider;
  }

  public synchronized String getSelectedModularClientLanguage() {
      return mVoiceUIAttr.mSelectedModularClientLanguage;
  }

  public synchronized void setSelectedModularClientLanguage(String selectedModularClientLanguage) {
     mVoiceUIAttr.mSelectedModularClientLanguage = selectedModularClientLanguage;
  }

  public synchronized String getSelectedNLUProvider() {
      return mVoiceUIAttr.mSelectedNLUProvider;
  }

  public synchronized void setSelectedNLUProvider(String selectedNLUProvider) {
    mVoiceUIAttr.mSelectedNLUProvider = selectedNLUProvider;
  }

  public synchronized String getSelectedTTSProvider() {
      return mVoiceUIAttr.mSelectedTTSProvider;
  }

  public synchronized void setSelectedTTSProvider(String selectedTTSProvider) {
    mVoiceUIAttr.mSelectedTTSProvider = selectedTTSProvider;
  }

  public synchronized List<VoiceUIClientAttr> getVoiceUIClients() {
      return GenericStateApi.getState(mVoiceUIAttr.mVoiceUIClients);
  }

  public synchronized void setVoiceUIClients(List<VoiceUIClientAttr> voiceUIClients) {
    GenericStateApi.setStateList(mVoiceUIAttr.mVoiceUIClients, voiceUIClients);
  }

  public synchronized boolean getWakewordStatus(String clientName) {
     VoiceUIClientAttr clientAttr = null;
     if(mVoiceUIAttr.mVoiceUIClients != null) {
        clientAttr = mVoiceUIAttr.mVoiceUIClients.stream().filter(attr -> attr.mName != null && !attr.mName.isEmpty() && attr.mName.equalsIgnoreCase(clientName)).findAny().orElse(null);
     }
     boolean retStatus = false;
     if(clientAttr != null) {
       retStatus = clientAttr.mWakewordStatus;
     }
     return retStatus;
  }

}
