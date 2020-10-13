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

public class SetCustomPropertyInAttr implements IResourceAttributes {


    public List<Integer>  mBandLevels;

     public SetCustomPropertyInAttr() {
        mBandLevels = new ArrayList<>();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/set_custom_property_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_bandLevels,ResourceAttrUtils.streamFromIntArray(mBandLevels));
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_bandLevels)) {
            didUnpack = true;
            mBandLevels = ResourceAttrUtils.intArrayFromStream(rep, ResourceAttributes.Prop_bandLevels);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    SetCustomPropertyInAttr clonedObj = new SetCustomPropertyInAttr();
    clonedObj.mBandLevels = this.mBandLevels;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof SetCustomPropertyInAttr) {
        SetCustomPropertyInAttr obj = (SetCustomPropertyInAttr) state;
        this.mBandLevels = obj.mBandLevels;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_bandLevels)) {
            didChanged = (!mBandLevels.equals(ResourceAttrUtils.intArrayFromStream(rep, ResourceAttributes.Prop_bandLevels)));
            if(didChanged) return true;
        }
        return false;
   }
}