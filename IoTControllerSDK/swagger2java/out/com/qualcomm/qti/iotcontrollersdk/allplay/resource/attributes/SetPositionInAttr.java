/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes;



import java.util.stream.Stream;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcException;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;

public class SetPositionInAttr implements IResourceAttributes {


    public int mPositionMsecs;

     public SetPositionInAttr() {
        mPositionMsecs = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/set_position_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_positionMsecs,mPositionMsecs);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_positionMsecs)) {
            didUnpack = true;
            mPositionMsecs = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_positionMsecs);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    SetPositionInAttr clonedObj = new SetPositionInAttr();
    clonedObj.mPositionMsecs = this.mPositionMsecs;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof SetPositionInAttr) {
        SetPositionInAttr obj = (SetPositionInAttr) state;
        this.mPositionMsecs = obj.mPositionMsecs;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_positionMsecs)) {
            didChanged = (mPositionMsecs != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_positionMsecs));
            if(didChanged) return true;
        }
        return false;
   }
}