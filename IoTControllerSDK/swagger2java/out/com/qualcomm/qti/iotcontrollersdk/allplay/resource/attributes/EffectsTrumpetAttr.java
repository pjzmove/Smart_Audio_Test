/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes;



import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcException;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;

public class EffectsTrumpetAttr implements IResourceAttributes {

    public enum Preset {
        kOff,
        kDynamic,
        kMovie,
        kMusic,
        kGame,
        kVoice,
    }


    public boolean mEnabled;
    public List<TrumpetEqualizerAttr>  mEqualizer;
    public double mGain;
    public Preset mPreset;
    public boolean mVirtualization;

     public EffectsTrumpetAttr() {
        mEnabled = false;
        mEqualizer = new ArrayList<>();
        mGain = 0f;
        mPreset = Preset.kOff;
        mVirtualization = false;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/effects_trumpet_rep_uri");

         List<OcRepresentation> reps = new ArrayList<>();
         rep.setValue(ResourceAttributes.Prop_enabled,mEnabled);

         for (TrumpetEqualizerAttr elem : mEqualizer) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_equalizer,repArray);
         }
         rep.setValue(ResourceAttributes.Prop_gain,mGain);
         rep.setValue(ResourceAttributes.Prop_preset,presetToString(mPreset));
         rep.setValue(ResourceAttributes.Prop_virtualization,mVirtualization);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_enabled)) {
            didUnpack = true;
            mEnabled = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enabled);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_equalizer)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_equalizer);
            if(reps != null) {
                mEqualizer.clear();
                for (OcRepresentation elem_rep: reps) {
                    TrumpetEqualizerAttr obj = new TrumpetEqualizerAttr();
                    if(obj.unpack(elem_rep))
                        mEqualizer.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_gain)) {
            didUnpack = true;
            mGain = ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_gain);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_preset)) {
            didUnpack = true;
            mPreset = presetFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_preset));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_virtualization)) {
            didUnpack = true;
            mVirtualization = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_virtualization);
        }
        return didUnpack;
    }

    public Preset presetFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("off")) {
                return Preset.kOff;
            }
            if (value.equalsIgnoreCase("dynamic")) {
                return Preset.kDynamic;
            }
            if (value.equalsIgnoreCase("movie")) {
                return Preset.kMovie;
            }
            if (value.equalsIgnoreCase("music")) {
                return Preset.kMusic;
            }
            if (value.equalsIgnoreCase("game")) {
                return Preset.kGame;
            }
            if (value.equalsIgnoreCase("voice")) {
                return Preset.kVoice;
            }
        }
        return Preset.kOff;
    }

    public String presetToString(Preset value) {
        switch(value) {
            case kOff:
                return "off";
            case kDynamic:
                return "dynamic";
            case kMovie:
                return "movie";
            case kMusic:
                return "music";
            case kGame:
                return "game";
            case kVoice:
                return "voice";
        }

        return "off";
    }


   @Override
   public Object getData() {
    EffectsTrumpetAttr clonedObj = new EffectsTrumpetAttr();
    clonedObj.mEnabled = this.mEnabled;
    for(TrumpetEqualizerAttr item:mEqualizer) {
        clonedObj.mEqualizer.add((TrumpetEqualizerAttr)item.getData());
    }
    clonedObj.mGain = this.mGain;
    clonedObj.mPreset = this.mPreset;
    clonedObj.mVirtualization = this.mVirtualization;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof EffectsTrumpetAttr) {
        EffectsTrumpetAttr obj = (EffectsTrumpetAttr) state;
        this.mEnabled = obj.mEnabled;
        this.mEqualizer.clear();
        for(TrumpetEqualizerAttr item:obj.mEqualizer) {
            this.mEqualizer.add((TrumpetEqualizerAttr)item.getData());
        }
        this.mGain = obj.mGain;
        this.mPreset = obj.mPreset;
        this.mVirtualization = obj.mVirtualization;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_enabled)) {
            didChanged = (mEnabled != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enabled));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_equalizer)) {
            reps = rep.getValue(ResourceAttributes.Prop_equalizer);
            if(reps != null) {
                if(mEqualizer.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mEqualizer.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_gain)) {
            didChanged = (mGain != ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_gain));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_preset)) {
            didChanged = (mPreset != presetFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_preset)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_virtualization)) {
            didChanged = (mVirtualization != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_virtualization));
            if(didChanged) return true;
        }
        return false;
   }
}