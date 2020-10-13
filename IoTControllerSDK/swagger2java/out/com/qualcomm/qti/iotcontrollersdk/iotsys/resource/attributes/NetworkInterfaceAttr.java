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

public class NetworkInterfaceAttr implements IResourceAttributes {


    public String mIPAddress;
    public boolean mConnectedState;
    public String mMacAddress;

     public NetworkInterfaceAttr() {
        mIPAddress = "";
        mConnectedState = false;
        mMacAddress = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/network_interface_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_IPAddress)) {
            didUnpack = true;
            mIPAddress = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_IPAddress);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_connectedState)) {
            didUnpack = true;
            mConnectedState = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_connectedState);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_macAddress)) {
            didUnpack = true;
            mMacAddress = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_macAddress);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    NetworkInterfaceAttr clonedObj = new NetworkInterfaceAttr();
    clonedObj.mIPAddress = this.mIPAddress;
    clonedObj.mConnectedState = this.mConnectedState;
    clonedObj.mMacAddress = this.mMacAddress;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof NetworkInterfaceAttr) {
        NetworkInterfaceAttr obj = (NetworkInterfaceAttr) state;
        this.mIPAddress = obj.mIPAddress;
        this.mConnectedState = obj.mConnectedState;
        this.mMacAddress = obj.mMacAddress;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_IPAddress)) {
            didChanged = (!mIPAddress.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_IPAddress)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_connectedState)) {
            didChanged = (mConnectedState != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_connectedState));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_macAddress)) {
            didChanged = (!mMacAddress.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_macAddress)));
            if(didChanged) return true;
        }
        return false;
   }
}