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

public class SelectClientInAttr implements IResourceAttributes {


    public String mClient;
    public boolean mWakewordStatus;

     public SelectClientInAttr() {
        mClient = "";
        mWakewordStatus = false;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/select_client_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_client,mClient);
         rep.setValue(ResourceAttributes.Prop_wakewordStatus,mWakewordStatus);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_client)) {
            didUnpack = true;
            mClient = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_client);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_wakewordStatus)) {
            didUnpack = true;
            mWakewordStatus = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_wakewordStatus);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    SelectClientInAttr clonedObj = new SelectClientInAttr();
    clonedObj.mClient = this.mClient;
    clonedObj.mWakewordStatus = this.mWakewordStatus;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof SelectClientInAttr) {
        SelectClientInAttr obj = (SelectClientInAttr) state;
        this.mClient = obj.mClient;
        this.mWakewordStatus = obj.mWakewordStatus;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_client)) {
            didChanged = (!mClient.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_client)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_wakewordStatus)) {
            didChanged = (mWakewordStatus != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_wakewordStatus));
            if(didChanged) return true;
        }
        return false;
   }
}