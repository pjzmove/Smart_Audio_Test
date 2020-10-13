/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes;



import java.util.stream.Stream;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcException;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;

public class VirtualXSoundXAttr implements IResourceAttributes {


    public boolean mDialogClarityEnabled;
    public boolean mEnabled;
    public int mOutMode;
    public EffectRangeAttr mOutModeRange;

     public VirtualXSoundXAttr() {
        mDialogClarityEnabled = false;
        mEnabled = false;
        mOutMode = 0;
        mOutModeRange = new EffectRangeAttr();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/virtual_x_sound_x_rep_uri");

         rep.setValue(ResourceAttributes.Prop_dialogClarityEnabled,mDialogClarityEnabled);
         rep.setValue(ResourceAttributes.Prop_enabled,mEnabled);
         rep.setValue(ResourceAttributes.Prop_outMode,mOutMode);
         rep.setValue(ResourceAttributes.Prop_outModeRange,mOutModeRange.pack());
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_dialogClarityEnabled)) {
            didUnpack = true;
            mDialogClarityEnabled = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_dialogClarityEnabled);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_enabled)) {
            didUnpack = true;
            mEnabled = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enabled);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_outMode)) {
            didUnpack = true;
            mOutMode = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_outMode);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_outModeRange)) {
            didUnpack = true;
            EffectRangeAttr obj = new EffectRangeAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_outModeRange)))
                mOutModeRange = obj;
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    VirtualXSoundXAttr clonedObj = new VirtualXSoundXAttr();
    clonedObj.mDialogClarityEnabled = this.mDialogClarityEnabled;
    clonedObj.mEnabled = this.mEnabled;
    clonedObj.mOutMode = this.mOutMode;
    clonedObj.mOutModeRange = (EffectRangeAttr)mOutModeRange.getData();
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof VirtualXSoundXAttr) {
        VirtualXSoundXAttr obj = (VirtualXSoundXAttr) state;
        this.mDialogClarityEnabled = obj.mDialogClarityEnabled;
        this.mEnabled = obj.mEnabled;
        this.mOutMode = obj.mOutMode;
        this.mOutModeRange.setData(obj.mOutModeRange);
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_dialogClarityEnabled)) {
            didChanged = (mDialogClarityEnabled != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_dialogClarityEnabled));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_enabled)) {
            didChanged = (mEnabled != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enabled));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_outMode)) {
            didChanged = (mOutMode != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_outMode));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_outModeRange)) {
            didChanged = (mOutModeRange.checkDifference(rep.getValue(ResourceAttributes.Prop_outModeRange)));
            if(didChanged) return true;
        }
        return false;
   }
}