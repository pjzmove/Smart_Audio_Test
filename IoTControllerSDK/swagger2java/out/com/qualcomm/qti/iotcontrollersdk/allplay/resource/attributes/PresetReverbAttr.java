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

public class PresetReverbAttr implements IResourceAttributes {


    public String mCurrentPreset;
    public boolean mEnabled;
    public List<String>  mPresets;

     public PresetReverbAttr() {
        mCurrentPreset = "";
        mEnabled = false;
        mPresets = new ArrayList<>();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/preset_reverb_rep_uri");

         rep.setValue(ResourceAttributes.Prop_currentPreset,mCurrentPreset);
         rep.setValue(ResourceAttributes.Prop_enabled,mEnabled);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_currentPreset)) {
            didUnpack = true;
            mCurrentPreset = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_currentPreset);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_enabled)) {
            didUnpack = true;
            mEnabled = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enabled);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_presets)) {
            didUnpack = true;
            mPresets = ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_presets);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PresetReverbAttr clonedObj = new PresetReverbAttr();
    clonedObj.mCurrentPreset = this.mCurrentPreset;
    clonedObj.mEnabled = this.mEnabled;
    clonedObj.mPresets = this.mPresets;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PresetReverbAttr) {
        PresetReverbAttr obj = (PresetReverbAttr) state;
        this.mCurrentPreset = obj.mCurrentPreset;
        this.mEnabled = obj.mEnabled;
        this.mPresets = obj.mPresets;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_currentPreset)) {
            didChanged = (!mCurrentPreset.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_currentPreset)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_enabled)) {
            didChanged = (mEnabled != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enabled));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_presets)) {
            didChanged = (!mPresets.equals(ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_presets)));
            if(didChanged) return true;
        }
        return false;
   }
}