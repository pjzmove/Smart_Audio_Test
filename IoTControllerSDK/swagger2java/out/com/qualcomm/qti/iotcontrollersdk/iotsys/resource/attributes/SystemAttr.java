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

public class SystemAttr implements IResourceAttributes {


    public BatteryStatusAttr mBatteryStatus;
    public boolean mBatterySupported;
    public String mDeviceFriendlyName;
    public String mFirmwareVersion;
    public String mManufacturer;
    public String mModel;

     public SystemAttr() {
        mBatteryStatus = new BatteryStatusAttr();
        mBatterySupported = false;
        mDeviceFriendlyName = "";
        mFirmwareVersion = "";
        mManufacturer = "";
        mModel = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/system_rep_uri");

         rep.setValue(ResourceAttributes.Prop_deviceFriendlyName,mDeviceFriendlyName);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_batteryStatus)) {
            didUnpack = true;
            BatteryStatusAttr obj = new BatteryStatusAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_batteryStatus)))
                mBatteryStatus = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_batterySupported)) {
            didUnpack = true;
            mBatterySupported = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_batterySupported);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceFriendlyName)) {
            didUnpack = true;
            mDeviceFriendlyName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceFriendlyName);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_firmwareVersion)) {
            didUnpack = true;
            mFirmwareVersion = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_firmwareVersion);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_manufacturer)) {
            didUnpack = true;
            mManufacturer = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_manufacturer);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_model)) {
            didUnpack = true;
            mModel = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_model);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    SystemAttr clonedObj = new SystemAttr();
    clonedObj.mBatteryStatus = (BatteryStatusAttr)mBatteryStatus.getData();
    clonedObj.mBatterySupported = this.mBatterySupported;
    clonedObj.mDeviceFriendlyName = this.mDeviceFriendlyName;
    clonedObj.mFirmwareVersion = this.mFirmwareVersion;
    clonedObj.mManufacturer = this.mManufacturer;
    clonedObj.mModel = this.mModel;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof SystemAttr) {
        SystemAttr obj = (SystemAttr) state;
        this.mBatteryStatus.setData(obj.mBatteryStatus);
        this.mBatterySupported = obj.mBatterySupported;
        this.mDeviceFriendlyName = obj.mDeviceFriendlyName;
        this.mFirmwareVersion = obj.mFirmwareVersion;
        this.mManufacturer = obj.mManufacturer;
        this.mModel = obj.mModel;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_batteryStatus)) {
            didChanged = (mBatteryStatus.checkDifference(rep.getValue(ResourceAttributes.Prop_batteryStatus)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_batterySupported)) {
            didChanged = (mBatterySupported != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_batterySupported));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceFriendlyName)) {
            didChanged = (!mDeviceFriendlyName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceFriendlyName)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_firmwareVersion)) {
            didChanged = (!mFirmwareVersion.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_firmwareVersion)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_manufacturer)) {
            didChanged = (!mManufacturer.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_manufacturer)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_model)) {
            didChanged = (!mModel.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_model)));
            if(didChanged) return true;
        }
        return false;
   }
}