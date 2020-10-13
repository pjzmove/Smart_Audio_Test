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

public class BtErrorAttr implements IResourceAttributes {


    public String mAddress;
    public String mStatus;

     public BtErrorAttr() {
        mAddress = "";
        mStatus = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/bt_error_rep_uri");

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
        if (rep.hasAttribute(ResourceAttributes.Prop_status)) {
            didUnpack = true;
            mStatus = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_status);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    BtErrorAttr clonedObj = new BtErrorAttr();
    clonedObj.mAddress = this.mAddress;
    clonedObj.mStatus = this.mStatus;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof BtErrorAttr) {
        BtErrorAttr obj = (BtErrorAttr) state;
        this.mAddress = obj.mAddress;
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
        if (rep.hasAttribute(ResourceAttributes.Prop_status)) {
            didChanged = (!mStatus.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_status)));
            if(didChanged) return true;
        }
        return false;
   }
}