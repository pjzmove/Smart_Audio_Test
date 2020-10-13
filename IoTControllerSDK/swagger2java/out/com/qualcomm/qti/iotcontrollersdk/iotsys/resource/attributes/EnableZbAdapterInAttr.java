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

public class EnableZbAdapterInAttr implements IResourceAttributes {


    public boolean mAdapterState;

     public EnableZbAdapterInAttr() {
        mAdapterState = false;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/enable_zb_adapter_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_adapterState,mAdapterState);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_adapterState)) {
            didUnpack = true;
            mAdapterState = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_adapterState);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    EnableZbAdapterInAttr clonedObj = new EnableZbAdapterInAttr();
    clonedObj.mAdapterState = this.mAdapterState;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof EnableZbAdapterInAttr) {
        EnableZbAdapterInAttr obj = (EnableZbAdapterInAttr) state;
        this.mAdapterState = obj.mAdapterState;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_adapterState)) {
            didChanged = (mAdapterState != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_adapterState));
            if(didChanged) return true;
        }
        return false;
   }
}