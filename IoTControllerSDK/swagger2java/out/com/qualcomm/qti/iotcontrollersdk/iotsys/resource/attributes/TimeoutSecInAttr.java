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

public class TimeoutSecInAttr implements IResourceAttributes {


    public int mTimeoutSecs;

     public TimeoutSecInAttr() {
        mTimeoutSecs = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/timeout_sec_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_timeoutSecs,mTimeoutSecs);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_timeoutSecs)) {
            didUnpack = true;
            mTimeoutSecs = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_timeoutSecs);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    TimeoutSecInAttr clonedObj = new TimeoutSecInAttr();
    clonedObj.mTimeoutSecs = this.mTimeoutSecs;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof TimeoutSecInAttr) {
        TimeoutSecInAttr obj = (TimeoutSecInAttr) state;
        this.mTimeoutSecs = obj.mTimeoutSecs;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_timeoutSecs)) {
            didChanged = (mTimeoutSecs != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_timeoutSecs));
            if(didChanged) return true;
        }
        return false;
   }
}