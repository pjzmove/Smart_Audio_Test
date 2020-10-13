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

public class AccessPointAttr implements IResourceAttributes {


    public String mRSSI;
    public String mSSID;

     public AccessPointAttr() {
        mRSSI = "";
        mSSID = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/access_point_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_RSSI)) {
            didUnpack = true;
            mRSSI = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_RSSI);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_SSID)) {
            didUnpack = true;
            mSSID = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_SSID);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    AccessPointAttr clonedObj = new AccessPointAttr();
    clonedObj.mRSSI = this.mRSSI;
    clonedObj.mSSID = this.mSSID;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof AccessPointAttr) {
        AccessPointAttr obj = (AccessPointAttr) state;
        this.mRSSI = obj.mRSSI;
        this.mSSID = obj.mSSID;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_RSSI)) {
            didChanged = (!mRSSI.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_RSSI)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_SSID)) {
            didChanged = (!mSSID.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_SSID)));
            if(didChanged) return true;
        }
        return false;
   }
}