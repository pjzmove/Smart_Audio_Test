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

public class AddUnconfiguredDeviceInAttr implements IResourceAttributes {


    public List<Integer>  mChannelMap;
    public String mDeviceId;
    public String mSsid;

     public AddUnconfiguredDeviceInAttr() {
        mChannelMap = new ArrayList<>();
        mDeviceId = "";
        mSsid = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/add_unconfigured_device_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_channelMap,ResourceAttrUtils.streamFromIntArray(mChannelMap));
         rep.setValue(ResourceAttributes.Prop_deviceId,mDeviceId);
         rep.setValue(ResourceAttributes.Prop_ssid,mSsid);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_channelMap)) {
            didUnpack = true;
            mChannelMap = ResourceAttrUtils.intArrayFromStream(rep, ResourceAttributes.Prop_channelMap);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceId)) {
            didUnpack = true;
            mDeviceId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_ssid)) {
            didUnpack = true;
            mSsid = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_ssid);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    AddUnconfiguredDeviceInAttr clonedObj = new AddUnconfiguredDeviceInAttr();
    clonedObj.mChannelMap = this.mChannelMap;
    clonedObj.mDeviceId = this.mDeviceId;
    clonedObj.mSsid = this.mSsid;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof AddUnconfiguredDeviceInAttr) {
        AddUnconfiguredDeviceInAttr obj = (AddUnconfiguredDeviceInAttr) state;
        this.mChannelMap = obj.mChannelMap;
        this.mDeviceId = obj.mDeviceId;
        this.mSsid = obj.mSsid;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_channelMap)) {
            didChanged = (!mChannelMap.equals(ResourceAttrUtils.intArrayFromStream(rep, ResourceAttributes.Prop_channelMap)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceId)) {
            didChanged = (!mDeviceId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_ssid)) {
            didChanged = (!mSsid.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_ssid)));
            if(didChanged) return true;
        }
        return false;
   }
}