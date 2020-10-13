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

public class EffectRangeAttr implements IResourceAttributes {


    public int mMax;
    public int mMin;

     public EffectRangeAttr() {
        mMax = 0;
        mMin = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/effect_range_rep_uri");

         rep.setValue(ResourceAttributes.Prop_max,mMax);
         rep.setValue(ResourceAttributes.Prop_min,mMin);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_max)) {
            didUnpack = true;
            mMax = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_max);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_min)) {
            didUnpack = true;
            mMin = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_min);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    EffectRangeAttr clonedObj = new EffectRangeAttr();
    clonedObj.mMax = this.mMax;
    clonedObj.mMin = this.mMin;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof EffectRangeAttr) {
        EffectRangeAttr obj = (EffectRangeAttr) state;
        this.mMax = obj.mMax;
        this.mMin = obj.mMin;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_max)) {
            didChanged = (mMax != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_max));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_min)) {
            didChanged = (mMin != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_min));
            if(didChanged) return true;
        }
        return false;
   }
}