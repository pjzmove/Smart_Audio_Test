/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes;



import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcException;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;

public class AudioEffectsAttr implements IResourceAttributes {


    public List<EffectDescriptionAttr>  mListEffects;

     public AudioEffectsAttr() {
        mListEffects = new ArrayList<>();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/audio_effects_rep_uri");

         List<OcRepresentation> reps = new ArrayList<>();

         for (EffectDescriptionAttr elem : mListEffects) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_listEffects,repArray);
         }
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_listEffects)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_listEffects);
            if(reps != null) {
                mListEffects.clear();
                for (OcRepresentation elem_rep: reps) {
                    EffectDescriptionAttr obj = new EffectDescriptionAttr();
                    if(obj.unpack(elem_rep))
                        mListEffects.add(obj);
                }
            }
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    AudioEffectsAttr clonedObj = new AudioEffectsAttr();
    for(EffectDescriptionAttr item:mListEffects) {
        clonedObj.mListEffects.add((EffectDescriptionAttr)item.getData());
    }
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof AudioEffectsAttr) {
        AudioEffectsAttr obj = (AudioEffectsAttr) state;
        this.mListEffects.clear();
        for(EffectDescriptionAttr item:obj.mListEffects) {
            this.mListEffects.add((EffectDescriptionAttr)item.getData());
        }
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_listEffects)) {
            reps = rep.getValue(ResourceAttributes.Prop_listEffects);
            if(reps != null) {
                if(mListEffects.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mListEffects.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        return false;
   }
}