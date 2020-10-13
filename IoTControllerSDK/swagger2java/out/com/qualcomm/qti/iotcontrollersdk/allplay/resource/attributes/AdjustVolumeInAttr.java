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

public class AdjustVolumeInAttr implements IResourceAttributes {


    public double mDelta;

     public AdjustVolumeInAttr() {
        mDelta = 0f;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/adjust_volume_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_delta,mDelta);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_delta)) {
            didUnpack = true;
            mDelta = ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_delta);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    AdjustVolumeInAttr clonedObj = new AdjustVolumeInAttr();
    clonedObj.mDelta = this.mDelta;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof AdjustVolumeInAttr) {
        AdjustVolumeInAttr obj = (AdjustVolumeInAttr) state;
        this.mDelta = obj.mDelta;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_delta)) {
            didChanged = (mDelta != ResourceAttrUtils.doubleValueFromRepresentation(rep, ResourceAttributes.Prop_delta));
            if(didChanged) return true;
        }
        return false;
   }
}