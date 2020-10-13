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

public class BtSignalAttr implements IResourceAttributes {

    public enum Name {
        kScanStopped,
        kPairedListCleared,
        kUnknown,
    }


    public Name mName;
    public String mStatus;

     public BtSignalAttr() {
        mName = Name.kScanStopped;
        mStatus = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/bt_signal_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_name)) {
            didUnpack = true;
            mName = nameFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_name));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_status)) {
            didUnpack = true;
            mStatus = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_status);
        }
        return didUnpack;
    }

    public Name nameFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("scanStopped")) {
                return Name.kScanStopped;
            }
            if (value.equalsIgnoreCase("pairedListCleared")) {
                return Name.kPairedListCleared;
            }
            if (value.equalsIgnoreCase("unknown")) {
                return Name.kUnknown;
            }
        }
        return Name.kScanStopped;
    }

    public String nameToString(Name value) {
        switch(value) {
            case kScanStopped:
                return "scanStopped";
            case kPairedListCleared:
                return "pairedListCleared";
            case kUnknown:
                return "unknown";
        }

        return "scanStopped";
    }


   @Override
   public Object getData() {
    BtSignalAttr clonedObj = new BtSignalAttr();
    clonedObj.mName = this.mName;
    clonedObj.mStatus = this.mStatus;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof BtSignalAttr) {
        BtSignalAttr obj = (BtSignalAttr) state;
        this.mName = obj.mName;
        this.mStatus = obj.mStatus;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_name)) {
            didChanged = (mName != nameFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_name)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_status)) {
            didChanged = (!mStatus.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_status)));
            if(didChanged) return true;
        }
        return false;
   }
}