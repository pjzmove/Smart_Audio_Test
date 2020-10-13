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

public class AuthenticateAVSAttr implements IResourceAttributes {


    public String mCode;
    public String mUrl;

     public AuthenticateAVSAttr() {
        mCode = "";
        mUrl = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/authenticate_avs_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_code)) {
            didUnpack = true;
            mCode = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_code);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_url)) {
            didUnpack = true;
            mUrl = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_url);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    AuthenticateAVSAttr clonedObj = new AuthenticateAVSAttr();
    clonedObj.mCode = this.mCode;
    clonedObj.mUrl = this.mUrl;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof AuthenticateAVSAttr) {
        AuthenticateAVSAttr obj = (AuthenticateAVSAttr) state;
        this.mCode = obj.mCode;
        this.mUrl = obj.mUrl;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_code)) {
            didChanged = (!mCode.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_code)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_url)) {
            didChanged = (!mUrl.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_url)));
            if(didChanged) return true;
        }
        return false;
   }
}