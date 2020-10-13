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

public class JoinGroupOutAttr implements IResourceAttributes {


    public int mMaxVolume;
    public int mVolume;

     public JoinGroupOutAttr() {
        mMaxVolume = 0;
        mVolume = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/join_group_out_rep_uri");

         rep.setValue(ResourceAttributes.Prop_maxVolume,mMaxVolume);
         rep.setValue(ResourceAttributes.Prop_volume,mVolume);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_maxVolume)) {
            didUnpack = true;
            mMaxVolume = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_maxVolume);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volume)) {
            didUnpack = true;
            mVolume = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_volume);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    JoinGroupOutAttr clonedObj = new JoinGroupOutAttr();
    clonedObj.mMaxVolume = this.mMaxVolume;
    clonedObj.mVolume = this.mVolume;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof JoinGroupOutAttr) {
        JoinGroupOutAttr obj = (JoinGroupOutAttr) state;
        this.mMaxVolume = obj.mMaxVolume;
        this.mVolume = obj.mVolume;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_maxVolume)) {
            didChanged = (mMaxVolume != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_maxVolume));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volume)) {
            didChanged = (mVolume != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_volume));
            if(didChanged) return true;
        }
        return false;
   }
}