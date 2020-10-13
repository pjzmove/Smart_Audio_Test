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

public class AddressInAttr implements IResourceAttributes {


    public String mAddress;

     public AddressInAttr() {
        mAddress = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/address_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_address,mAddress);
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
        return didUnpack;
    }


   @Override
   public Object getData() {
    AddressInAttr clonedObj = new AddressInAttr();
    clonedObj.mAddress = this.mAddress;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof AddressInAttr) {
        AddressInAttr obj = (AddressInAttr) state;
        this.mAddress = obj.mAddress;
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
        return false;
   }
}