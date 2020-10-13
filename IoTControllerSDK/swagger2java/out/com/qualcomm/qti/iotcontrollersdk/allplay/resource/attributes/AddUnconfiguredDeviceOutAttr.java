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

public class AddUnconfiguredDeviceOutAttr implements IResourceAttributes {


    public String mDeviceId;
    public boolean mKnown;

     public AddUnconfiguredDeviceOutAttr() {
        mDeviceId = "";
        mKnown = false;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/add_unconfigured_device_out_rep_uri");

         rep.setValue(ResourceAttributes.Prop_deviceId,mDeviceId);
         rep.setValue(ResourceAttributes.Prop_known,mKnown);
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
        if (rep.hasAttribute(ResourceAttributes.Prop_known)) {
            didUnpack = true;
            mKnown = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_known);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    AddUnconfiguredDeviceOutAttr clonedObj = new AddUnconfiguredDeviceOutAttr();
    clonedObj.mDeviceId = this.mDeviceId;
    clonedObj.mKnown = this.mKnown;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof AddUnconfiguredDeviceOutAttr) {
        AddUnconfiguredDeviceOutAttr obj = (AddUnconfiguredDeviceOutAttr) state;
        this.mDeviceId = obj.mDeviceId;
        this.mKnown = obj.mKnown;
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
        if (rep.hasAttribute(ResourceAttributes.Prop_known)) {
            didChanged = (mKnown != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_known));
            if(didChanged) return true;
        }
        return false;
   }
}