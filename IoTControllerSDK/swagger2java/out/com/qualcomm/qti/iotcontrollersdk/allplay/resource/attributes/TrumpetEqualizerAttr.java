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

public class TrumpetEqualizerAttr implements IResourceAttributes {


    public double mFrequency;
    public double mGain;

     public TrumpetEqualizerAttr() {
        mFrequency = 0f;
        mGain = 0f;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/trumpet_equalizer_rep_uri");

         rep.setValue(ResourceAttributes.Prop_frequency,mFrequency);
         rep.setValue(ResourceAttributes.Prop_gain,mGain);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_frequency)) {
            didUnpack = true;
            mFrequency = ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_frequency);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_gain)) {
            didUnpack = true;
            mGain = ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_gain);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    TrumpetEqualizerAttr clonedObj = new TrumpetEqualizerAttr();
    clonedObj.mFrequency = this.mFrequency;
    clonedObj.mGain = this.mGain;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof TrumpetEqualizerAttr) {
        TrumpetEqualizerAttr obj = (TrumpetEqualizerAttr) state;
        this.mFrequency = obj.mFrequency;
        this.mGain = obj.mGain;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_frequency)) {
            didChanged = (mFrequency != ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_frequency));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_gain)) {
            didChanged = (mGain != ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_gain));
            if(didChanged) return true;
        }
        return false;
   }
}