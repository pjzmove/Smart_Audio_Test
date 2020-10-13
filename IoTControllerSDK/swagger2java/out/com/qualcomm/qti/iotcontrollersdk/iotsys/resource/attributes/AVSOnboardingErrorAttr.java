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

public class AVSOnboardingErrorAttr implements IResourceAttributes {

    public enum Error {
        kTimedout,
        kUnknown,
    }


    public String mClient;
    public Error mError;
    public int mReattempt;

     public AVSOnboardingErrorAttr() {
        mClient = "";
        mError = Error.kTimedout;
        mReattempt = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/avs_onboarding_error_rep_uri");

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
        if (rep.hasAttribute(ResourceAttributes.Prop_error)) {
            didUnpack = true;
            mError = errorFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_error));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_reattempt)) {
            didUnpack = true;
            mReattempt = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_reattempt);
        }
        return didUnpack;
    }

    public Error errorFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("timedout")) {
                return Error.kTimedout;
            }
            if (value.equalsIgnoreCase("unknown")) {
                return Error.kUnknown;
            }
        }
        return Error.kTimedout;
    }

    public String errorToString(Error value) {
        switch(value) {
            case kTimedout:
                return "timedout";
            case kUnknown:
                return "unknown";
        }

        return "timedout";
    }


   @Override
   public Object getData() {
    AVSOnboardingErrorAttr clonedObj = new AVSOnboardingErrorAttr();
    clonedObj.mClient = this.mClient;
    clonedObj.mError = this.mError;
    clonedObj.mReattempt = this.mReattempt;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof AVSOnboardingErrorAttr) {
        AVSOnboardingErrorAttr obj = (AVSOnboardingErrorAttr) state;
        this.mClient = obj.mClient;
        this.mError = obj.mError;
        this.mReattempt = obj.mReattempt;
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
        if (rep.hasAttribute(ResourceAttributes.Prop_error)) {
            didChanged = (mError != errorFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_error)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_reattempt)) {
            didChanged = (mReattempt != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_reattempt));
            if(didChanged) return true;
        }
        return false;
   }
}