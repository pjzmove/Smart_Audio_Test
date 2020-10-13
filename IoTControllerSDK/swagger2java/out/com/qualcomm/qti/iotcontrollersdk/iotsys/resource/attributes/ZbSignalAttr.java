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

public class ZbSignalAttr implements IResourceAttributes {

    public enum SignalName {
        kNetworkFormed,
        kNetworkJoined,
        kUnknown,
    }


    public SignalName mSignalName;
    public boolean mStatus;

     public ZbSignalAttr() {
        mSignalName = SignalName.kNetworkFormed;
        mStatus = false;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/zb_signal_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_signalName)) {
            didUnpack = true;
            mSignalName = signalNameFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_signalName));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_status)) {
            didUnpack = true;
            mStatus = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_status);
        }
        return didUnpack;
    }

    public SignalName signalNameFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("networkFormed")) {
                return SignalName.kNetworkFormed;
            }
            if (value.equalsIgnoreCase("networkJoined")) {
                return SignalName.kNetworkJoined;
            }
            if (value.equalsIgnoreCase("unknown")) {
                return SignalName.kUnknown;
            }
        }
        return SignalName.kNetworkFormed;
    }

    public String signalNameToString(SignalName value) {
        switch(value) {
            case kNetworkFormed:
                return "networkFormed";
            case kNetworkJoined:
                return "networkJoined";
            case kUnknown:
                return "unknown";
        }

        return "networkFormed";
    }


   @Override
   public Object getData() {
    ZbSignalAttr clonedObj = new ZbSignalAttr();
    clonedObj.mSignalName = this.mSignalName;
    clonedObj.mStatus = this.mStatus;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof ZbSignalAttr) {
        ZbSignalAttr obj = (ZbSignalAttr) state;
        this.mSignalName = obj.mSignalName;
        this.mStatus = obj.mStatus;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_signalName)) {
            didChanged = (mSignalName != signalNameFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_signalName)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_status)) {
            didChanged = (mStatus != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_status));
            if(didChanged) return true;
        }
        return false;
   }
}