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

public class EqualizerAttr implements IResourceAttributes {


    public String mCurrentPreset;
    public boolean mEnabled;
    public EqualizerPropertyAttr mEqualizerProperty;
    public List<String>  mPresets;

     public EqualizerAttr() {
        mCurrentPreset = "";
        mEnabled = false;
        mEqualizerProperty = new EqualizerPropertyAttr();
        mPresets = new ArrayList<>();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/equalizer_rep_uri");

         rep.setValue(ResourceAttributes.Prop_currentPreset,mCurrentPreset);
         rep.setValue(ResourceAttributes.Prop_enabled,mEnabled);
         rep.setValue(ResourceAttributes.Prop_equalizerProperty,mEqualizerProperty.pack());
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
        if (rep.hasAttribute(ResourceAttributes.Prop_equalizerProperty)) {
            didUnpack = true;
            EqualizerPropertyAttr obj = new EqualizerPropertyAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_equalizerProperty)))
                mEqualizerProperty = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_presets)) {
            didUnpack = true;
            mPresets = ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_presets);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    EqualizerAttr clonedObj = new EqualizerAttr();
    clonedObj.mCurrentPreset = this.mCurrentPreset;
    clonedObj.mEnabled = this.mEnabled;
    clonedObj.mEqualizerProperty = (EqualizerPropertyAttr)mEqualizerProperty.getData();
    clonedObj.mPresets = this.mPresets;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof EqualizerAttr) {
        EqualizerAttr obj = (EqualizerAttr) state;
        this.mCurrentPreset = obj.mCurrentPreset;
        this.mEnabled = obj.mEnabled;
        this.mEqualizerProperty.setData(obj.mEqualizerProperty);
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
        if (rep.hasAttribute(ResourceAttributes.Prop_equalizerProperty)) {
            didChanged = (mEqualizerProperty.checkDifference(rep.getValue(ResourceAttributes.Prop_equalizerProperty)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_presets)) {
            didChanged = (!mPresets.equals(ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_presets)));
            if(didChanged) return true;
        }
        return false;
   }
}