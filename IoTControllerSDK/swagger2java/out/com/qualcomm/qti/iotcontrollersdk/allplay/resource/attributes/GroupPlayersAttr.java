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

public class GroupPlayersAttr implements IResourceAttributes {


    public List<Integer>  mChannelMap;
    public boolean mConnected;
    public String mDeviceId;
    public String mDisplayName;
    public double mVolumeRatio;

     public GroupPlayersAttr() {
        mChannelMap = new ArrayList<>();
        mConnected = false;
        mDeviceId = "";
        mDisplayName = "";
        mVolumeRatio = 0f;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/group_players_rep_uri");

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
        if (rep.hasAttribute(ResourceAttributes.Prop_connected)) {
            didUnpack = true;
            mConnected = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_connected);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceId)) {
            didUnpack = true;
            mDeviceId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_displayName)) {
            didUnpack = true;
            mDisplayName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_displayName);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volumeRatio)) {
            didUnpack = true;
            mVolumeRatio = ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_volumeRatio);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    GroupPlayersAttr clonedObj = new GroupPlayersAttr();
    clonedObj.mChannelMap = this.mChannelMap;
    clonedObj.mConnected = this.mConnected;
    clonedObj.mDeviceId = this.mDeviceId;
    clonedObj.mDisplayName = this.mDisplayName;
    clonedObj.mVolumeRatio = this.mVolumeRatio;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof GroupPlayersAttr) {
        GroupPlayersAttr obj = (GroupPlayersAttr) state;
        this.mChannelMap = obj.mChannelMap;
        this.mConnected = obj.mConnected;
        this.mDeviceId = obj.mDeviceId;
        this.mDisplayName = obj.mDisplayName;
        this.mVolumeRatio = obj.mVolumeRatio;
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
        if (rep.hasAttribute(ResourceAttributes.Prop_connected)) {
            didChanged = (mConnected != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_connected));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceId)) {
            didChanged = (!mDeviceId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_displayName)) {
            didChanged = (!mDisplayName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_displayName)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volumeRatio)) {
            didChanged = (mVolumeRatio != ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_volumeRatio));
            if(didChanged) return true;
        }
        return false;
   }
}