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

public class JoinGroupInAttr implements IResourceAttributes {


    public List<Integer>  mChannelMap;
    public String mLeadDeviceId;
    public String mMlanPassword;
    public String mMlanSsid;

     public JoinGroupInAttr() {
        mChannelMap = new ArrayList<>();
        mLeadDeviceId = "";
        mMlanPassword = "";
        mMlanSsid = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/join_group_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_channelMap,ResourceAttrUtils.streamFromIntArray(mChannelMap));
         rep.setValue(ResourceAttributes.Prop_leadDeviceId,mLeadDeviceId);
         rep.setValue(ResourceAttributes.Prop_mlanPassword,mMlanPassword);
         rep.setValue(ResourceAttributes.Prop_mlanSsid,mMlanSsid);
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
        if (rep.hasAttribute(ResourceAttributes.Prop_leadDeviceId)) {
            didUnpack = true;
            mLeadDeviceId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_leadDeviceId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_mlanPassword)) {
            didUnpack = true;
            mMlanPassword = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_mlanPassword);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_mlanSsid)) {
            didUnpack = true;
            mMlanSsid = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_mlanSsid);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    JoinGroupInAttr clonedObj = new JoinGroupInAttr();
    clonedObj.mChannelMap = this.mChannelMap;
    clonedObj.mLeadDeviceId = this.mLeadDeviceId;
    clonedObj.mMlanPassword = this.mMlanPassword;
    clonedObj.mMlanSsid = this.mMlanSsid;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof JoinGroupInAttr) {
        JoinGroupInAttr obj = (JoinGroupInAttr) state;
        this.mChannelMap = obj.mChannelMap;
        this.mLeadDeviceId = obj.mLeadDeviceId;
        this.mMlanPassword = obj.mMlanPassword;
        this.mMlanSsid = obj.mMlanSsid;
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
        if (rep.hasAttribute(ResourceAttributes.Prop_leadDeviceId)) {
            didChanged = (!mLeadDeviceId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_leadDeviceId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_mlanPassword)) {
            didChanged = (!mMlanPassword.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_mlanPassword)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_mlanSsid)) {
            didChanged = (!mMlanSsid.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_mlanSsid)));
            if(didChanged) return true;
        }
        return false;
   }
}