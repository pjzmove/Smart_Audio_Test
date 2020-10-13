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

public class VolumeControlAttr implements IResourceAttributes {


    public boolean mMute;
    public double mStep;
    public int mVersion;
    public double mVolume;

     public VolumeControlAttr() {
        mMute = false;
        mStep = 0f;
        mVersion = 0;
        mVolume = 0f;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/volume_control_rep_uri");

         rep.setValue(ResourceAttributes.Prop_mute,mMute);
         rep.setValue(ResourceAttributes.Prop_volume,mVolume);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_mute)) {
            didUnpack = true;
            mMute = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_mute);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_step)) {
            didUnpack = true;
            mStep = ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_step);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didUnpack = true;
            mVersion = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_version);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volume)) {
            didUnpack = true;
            mVolume = ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_volume);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    VolumeControlAttr clonedObj = new VolumeControlAttr();
    clonedObj.mMute = this.mMute;
    clonedObj.mStep = this.mStep;
    clonedObj.mVersion = this.mVersion;
    clonedObj.mVolume = this.mVolume;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof VolumeControlAttr) {
        VolumeControlAttr obj = (VolumeControlAttr) state;
        this.mMute = obj.mMute;
        this.mStep = obj.mStep;
        this.mVersion = obj.mVersion;
        this.mVolume = obj.mVolume;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_mute)) {
            didChanged = (mMute != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_mute));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_step)) {
            didChanged = (mStep != ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_step));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didChanged = (mVersion != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_version));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volume)) {
            didChanged = (mVolume != ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_volume));
            if(didChanged) return true;
        }
        return false;
   }
}