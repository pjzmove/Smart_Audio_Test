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

public class EffectDescriptionAttr implements IResourceAttributes {


    public String mEffectName;
    public String mEffectUuid;

     public EffectDescriptionAttr() {
        mEffectName = "";
        mEffectUuid = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/effect_description_rep_uri");

         rep.setValue(ResourceAttributes.Prop_effectName,mEffectName);
         rep.setValue(ResourceAttributes.Prop_effectUuid,mEffectUuid);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_effectName)) {
            didUnpack = true;
            mEffectName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_effectName);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_effectUuid)) {
            didUnpack = true;
            mEffectUuid = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_effectUuid);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    EffectDescriptionAttr clonedObj = new EffectDescriptionAttr();
    clonedObj.mEffectName = this.mEffectName;
    clonedObj.mEffectUuid = this.mEffectUuid;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof EffectDescriptionAttr) {
        EffectDescriptionAttr obj = (EffectDescriptionAttr) state;
        this.mEffectName = obj.mEffectName;
        this.mEffectUuid = obj.mEffectUuid;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_effectName)) {
            didChanged = (!mEffectName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_effectName)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_effectUuid)) {
            didChanged = (!mEffectUuid.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_effectUuid)));
            if(didChanged) return true;
        }
        return false;
   }
}