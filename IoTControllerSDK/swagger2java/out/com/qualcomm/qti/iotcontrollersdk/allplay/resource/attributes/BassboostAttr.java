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

public class BassboostAttr implements IResourceAttributes {


    public boolean mEnabled;
    public int mStrength;
    public EffectRangeAttr mStrengthRange;

     public BassboostAttr() {
        mEnabled = false;
        mStrength = 0;
        mStrengthRange = new EffectRangeAttr();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/bassboost_rep_uri");

         rep.setValue(ResourceAttributes.Prop_enabled,mEnabled);
         rep.setValue(ResourceAttributes.Prop_strength,mStrength);
         rep.setValue(ResourceAttributes.Prop_strengthRange,mStrengthRange.pack());
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_enabled)) {
            didUnpack = true;
            mEnabled = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enabled);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_strength)) {
            didUnpack = true;
            mStrength = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_strength);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_strengthRange)) {
            didUnpack = true;
            EffectRangeAttr obj = new EffectRangeAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_strengthRange)))
                mStrengthRange = obj;
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    BassboostAttr clonedObj = new BassboostAttr();
    clonedObj.mEnabled = this.mEnabled;
    clonedObj.mStrength = this.mStrength;
    clonedObj.mStrengthRange = (EffectRangeAttr)mStrengthRange.getData();
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof BassboostAttr) {
        BassboostAttr obj = (BassboostAttr) state;
        this.mEnabled = obj.mEnabled;
        this.mStrength = obj.mStrength;
        this.mStrengthRange.setData(obj.mStrengthRange);
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_enabled)) {
            didChanged = (mEnabled != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enabled));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_strength)) {
            didChanged = (mStrength != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_strength));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_strengthRange)) {
            didChanged = (mStrengthRange.checkDifference(rep.getValue(ResourceAttributes.Prop_strengthRange)));
            if(didChanged) return true;
        }
        return false;
   }
}