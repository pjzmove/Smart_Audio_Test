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

public class ZbDeviceAttr implements IResourceAttributes {


    public int mDeviceIdentifier;
    public String mDeviceType;
    public String mFriendlyName;

     public ZbDeviceAttr() {
        mDeviceIdentifier = 0;
        mDeviceType = "";
        mFriendlyName = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/zb_device_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceIdentifier)) {
            didUnpack = true;
            mDeviceIdentifier = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_deviceIdentifier);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceType)) {
            didUnpack = true;
            mDeviceType = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceType);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_friendlyName)) {
            didUnpack = true;
            mFriendlyName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_friendlyName);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    ZbDeviceAttr clonedObj = new ZbDeviceAttr();
    clonedObj.mDeviceIdentifier = this.mDeviceIdentifier;
    clonedObj.mDeviceType = this.mDeviceType;
    clonedObj.mFriendlyName = this.mFriendlyName;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof ZbDeviceAttr) {
        ZbDeviceAttr obj = (ZbDeviceAttr) state;
        this.mDeviceIdentifier = obj.mDeviceIdentifier;
        this.mDeviceType = obj.mDeviceType;
        this.mFriendlyName = obj.mFriendlyName;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceIdentifier)) {
            didChanged = (mDeviceIdentifier != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_deviceIdentifier));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceType)) {
            didChanged = (!mDeviceType.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceType)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_friendlyName)) {
            didChanged = (!mFriendlyName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_friendlyName)));
            if(didChanged) return true;
        }
        return false;
   }
}