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

public class VolumeInfoHelperAttr implements IResourceAttributes {


    public String mDeviceId;
    public boolean mMute;
    public double mVolume;

     public VolumeInfoHelperAttr() {
        mDeviceId = "";
        mMute = false;
        mVolume = 0f;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/volume_info_helper_rep_uri");

         rep.setValue(ResourceAttributes.Prop_deviceId,mDeviceId);
         rep.setValue(ResourceAttributes.Prop_mute,mMute);
         rep.setValue(ResourceAttributes.Prop_volume,mVolume);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceId)) {
            didUnpack = true;
            mDeviceId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_mute)) {
            didUnpack = true;
            mMute = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_mute);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volume)) {
            didUnpack = true;
            mVolume = ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_volume);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    VolumeInfoHelperAttr clonedObj = new VolumeInfoHelperAttr();
    clonedObj.mDeviceId = this.mDeviceId;
    clonedObj.mMute = this.mMute;
    clonedObj.mVolume = this.mVolume;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof VolumeInfoHelperAttr) {
        VolumeInfoHelperAttr obj = (VolumeInfoHelperAttr) state;
        this.mDeviceId = obj.mDeviceId;
        this.mMute = obj.mMute;
        this.mVolume = obj.mVolume;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceId)) {
            didChanged = (!mDeviceId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_mute)) {
            didChanged = (mMute != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_mute));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volume)) {
            didChanged = (mVolume != ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_volume));
            if(didChanged) return true;
        }
        return false;
   }
}