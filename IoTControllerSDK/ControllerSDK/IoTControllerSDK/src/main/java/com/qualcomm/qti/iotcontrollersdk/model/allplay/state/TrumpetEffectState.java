/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;


import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EffectsTrumpetAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EffectsTrumpetAttr.Preset;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.TrumpetEqualizerAttr;
import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import java.util.Arrays;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class TrumpetEffectState extends ResourceState {

  private final EffectsTrumpetAttr mTrumpetAttr = new EffectsTrumpetAttr();

  public enum TrumpetPreset {
    OFF,
    DYNAMIC,
    MOVIE,
    MUSIC,
    GAME,
    VOICE
  }

  public final static List<String> PresetList = Arrays.asList("OFF","DYNAMIC","MOVIE","MUSIC","GAME","VOICE");

  public TrumpetEffectState() {

  }

  public synchronized void update(EffectsTrumpetAttr attr) {
    GenericStateApi.setState(mTrumpetAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
      return GenericStateApi.updateState(mTrumpetAttr, rep);
  }

  public synchronized EffectsTrumpetAttr getAttribute() {
      return GenericStateApi.getState(mTrumpetAttr);
  }

  public synchronized boolean isEnabled() {
      return mTrumpetAttr.mEnabled;
  }

  public synchronized void setEnabled(boolean enabled) {
      mTrumpetAttr.mEnabled = enabled;
  }

  public synchronized boolean isVirtualization() {
    return mTrumpetAttr.mVirtualization;
  }

  public synchronized void setVirtualization(boolean enabled) {
     mTrumpetAttr.mVirtualization = enabled;
  }

  public synchronized double getGain() {
    return mTrumpetAttr.mGain;
  }

  public synchronized void setGain(double gain) {
    mTrumpetAttr.mGain = gain;
  }

  public synchronized List<TrumpetEqualizerAttr> getTrumpetEqbands() {
    return GenericStateApi.getState(mTrumpetAttr.mEqualizer);
  }

  public synchronized  TrumpetPreset getCurrentPreset() {

      Preset preset = mTrumpetAttr.mPreset;
      switch (preset) {
        case kOff:
          return TrumpetPreset.OFF;
        case kDynamic:
          return TrumpetPreset.DYNAMIC;
        case kMovie:
          return TrumpetPreset.MOVIE;
        case kMusic:
          return TrumpetPreset.MUSIC;
        case kGame:
          return TrumpetPreset.GAME;
        case kVoice:
          return TrumpetPreset.VOICE;
        default:
          return TrumpetPreset.OFF;
      }
  }

  public static Preset getPreset(String preset) {
    if(preset != null && !preset.isEmpty()) {
      if(preset.equalsIgnoreCase("OFF"))
        return Preset.kOff;
      if(preset.equalsIgnoreCase("DYNAMIC"))
        return Preset.kDynamic;
      if(preset.equalsIgnoreCase("MOVIE"))
        return Preset.kMovie;
      if(preset.equalsIgnoreCase("MUSIC"))
        return Preset.kMusic;
      if(preset.equalsIgnoreCase("GAME"))
        return Preset.kGame;
      if(preset.equalsIgnoreCase("VOICE"))
        return Preset.kVoice;
    } else {
      return Preset.kOff;
    }
    return Preset.kOff;
  }


}
