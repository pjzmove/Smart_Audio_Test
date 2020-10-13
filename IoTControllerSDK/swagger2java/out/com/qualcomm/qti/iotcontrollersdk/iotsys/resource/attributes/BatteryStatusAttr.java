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

public class BatteryStatusAttr implements IResourceAttributes {


    public boolean mBatteryPowered;
    public int mChargeLevel;
    public int mTimeTillDischarge;
    public int mTimeTillFullCharge;

     public BatteryStatusAttr() {
        mBatteryPowered = false;
        mChargeLevel = 0;
        mTimeTillDischarge = 0;
        mTimeTillFullCharge = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/battery_status_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_batteryPowered)) {
            didUnpack = true;
            mBatteryPowered = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_batteryPowered);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_chargeLevel)) {
            didUnpack = true;
            mChargeLevel = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_chargeLevel);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_timeTillDischarge)) {
            didUnpack = true;
            mTimeTillDischarge = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_timeTillDischarge);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_timeTillFullCharge)) {
            didUnpack = true;
            mTimeTillFullCharge = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_timeTillFullCharge);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    BatteryStatusAttr clonedObj = new BatteryStatusAttr();
    clonedObj.mBatteryPowered = this.mBatteryPowered;
    clonedObj.mChargeLevel = this.mChargeLevel;
    clonedObj.mTimeTillDischarge = this.mTimeTillDischarge;
    clonedObj.mTimeTillFullCharge = this.mTimeTillFullCharge;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof BatteryStatusAttr) {
        BatteryStatusAttr obj = (BatteryStatusAttr) state;
        this.mBatteryPowered = obj.mBatteryPowered;
        this.mChargeLevel = obj.mChargeLevel;
        this.mTimeTillDischarge = obj.mTimeTillDischarge;
        this.mTimeTillFullCharge = obj.mTimeTillFullCharge;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_batteryPowered)) {
            didChanged = (mBatteryPowered != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_batteryPowered));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_chargeLevel)) {
            didChanged = (mChargeLevel != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_chargeLevel));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_timeTillDischarge)) {
            didChanged = (mTimeTillDischarge != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_timeTillDischarge));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_timeTillFullCharge)) {
            didChanged = (mTimeTillFullCharge != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_timeTillFullCharge));
            if(didChanged) return true;
        }
        return false;
   }
}