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

public class ClientInAttr implements IResourceAttributes {


    public String mClient;

     public ClientInAttr() {
        mClient = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/client_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_client,mClient);
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
        return didUnpack;
    }


   @Override
   public Object getData() {
    ClientInAttr clonedObj = new ClientInAttr();
    clonedObj.mClient = this.mClient;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof ClientInAttr) {
        ClientInAttr obj = (ClientInAttr) state;
        this.mClient = obj.mClient;
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
        return false;
   }
}