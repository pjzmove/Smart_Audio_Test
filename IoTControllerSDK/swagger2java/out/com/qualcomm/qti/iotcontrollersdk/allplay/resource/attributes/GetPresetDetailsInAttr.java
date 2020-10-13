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

public class GetPresetDetailsInAttr implements IResourceAttributes {


    public String mPreset;

     public GetPresetDetailsInAttr() {
        mPreset = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/get_preset_details_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_preset,mPreset);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_preset)) {
            didUnpack = true;
            mPreset = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_preset);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    GetPresetDetailsInAttr clonedObj = new GetPresetDetailsInAttr();
    clonedObj.mPreset = this.mPreset;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof GetPresetDetailsInAttr) {
        GetPresetDetailsInAttr obj = (GetPresetDetailsInAttr) state;
        this.mPreset = obj.mPreset;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_preset)) {
            didChanged = (!mPreset.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_preset)));
            if(didChanged) return true;
        }
        return false;
   }
}