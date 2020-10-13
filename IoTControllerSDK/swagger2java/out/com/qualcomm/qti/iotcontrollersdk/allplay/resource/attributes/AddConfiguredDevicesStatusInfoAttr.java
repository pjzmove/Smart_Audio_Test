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

public class AddConfiguredDevicesStatusInfoAttr implements IResourceAttributes {


    public String mDeviceId;
    public boolean mKnownDevice;
    public int mRetCode;

     public AddConfiguredDevicesStatusInfoAttr() {
        mDeviceId = "";
        mKnownDevice = false;
        mRetCode = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/add_configured_devices_status_info_rep_uri");

         rep.setValue(ResourceAttributes.Prop_deviceId,mDeviceId);
         rep.setValue(ResourceAttributes.Prop_knownDevice,mKnownDevice);
         rep.setValue(ResourceAttributes.Prop_retCode,mRetCode);
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
        if (rep.hasAttribute(ResourceAttributes.Prop_knownDevice)) {
            didUnpack = true;
            mKnownDevice = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_knownDevice);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_retCode)) {
            didUnpack = true;
            mRetCode = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_retCode);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    AddConfiguredDevicesStatusInfoAttr clonedObj = new AddConfiguredDevicesStatusInfoAttr();
    clonedObj.mDeviceId = this.mDeviceId;
    clonedObj.mKnownDevice = this.mKnownDevice;
    clonedObj.mRetCode = this.mRetCode;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof AddConfiguredDevicesStatusInfoAttr) {
        AddConfiguredDevicesStatusInfoAttr obj = (AddConfiguredDevicesStatusInfoAttr) state;
        this.mDeviceId = obj.mDeviceId;
        this.mKnownDevice = obj.mKnownDevice;
        this.mRetCode = obj.mRetCode;
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
        if (rep.hasAttribute(ResourceAttributes.Prop_knownDevice)) {
            didChanged = (mKnownDevice != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_knownDevice));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_retCode)) {
            didChanged = (mRetCode != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_retCode));
            if(didChanged) return true;
        }
        return false;
   }
}