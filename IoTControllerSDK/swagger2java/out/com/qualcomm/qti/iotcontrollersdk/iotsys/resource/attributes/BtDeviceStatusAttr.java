/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes;


import java.util.stream.Stream;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcException;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;

public class BtDeviceStatusAttr implements IResourceAttributes {


    public String mAddress;
    public String mName;
    public boolean mStatus;

     public BtDeviceStatusAttr() {
        mAddress = "";
        mName = "";
        mStatus = false;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/bt_device_status_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_address)) {
            didUnpack = true;
            mAddress = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_address);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_name)) {
            didUnpack = true;
            mName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_name);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_status)) {
            didUnpack = true;
            mStatus = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_status);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    BtDeviceStatusAttr clonedObj = new BtDeviceStatusAttr();
    clonedObj.mAddress = this.mAddress;
    clonedObj.mName = this.mName;
    clonedObj.mStatus = this.mStatus;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof BtDeviceStatusAttr) {
        BtDeviceStatusAttr obj = (BtDeviceStatusAttr) state;
        this.mAddress = obj.mAddress;
        this.mName = obj.mName;
        this.mStatus = obj.mStatus;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_address)) {
            didChanged = (!mAddress.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_address)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_name)) {
            didChanged = (!mName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_name)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_status)) {
            didChanged = (mStatus != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_status));
            if(didChanged) return true;
        }
        return false;
   }
}