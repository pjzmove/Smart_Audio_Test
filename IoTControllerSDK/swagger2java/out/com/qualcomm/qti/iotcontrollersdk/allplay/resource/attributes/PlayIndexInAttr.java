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

public class PlayIndexInAttr implements IResourceAttributes {


    public int mItemIndex;
    public int mStartPositionMsecs;

     public PlayIndexInAttr() {
        mItemIndex = 0;
        mStartPositionMsecs = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/play_index_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_itemIndex,mItemIndex);
         rep.setValue(ResourceAttributes.Prop_startPositionMsecs,mStartPositionMsecs);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_itemIndex)) {
            didUnpack = true;
            mItemIndex = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_itemIndex);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_startPositionMsecs)) {
            didUnpack = true;
            mStartPositionMsecs = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_startPositionMsecs);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlayIndexInAttr clonedObj = new PlayIndexInAttr();
    clonedObj.mItemIndex = this.mItemIndex;
    clonedObj.mStartPositionMsecs = this.mStartPositionMsecs;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlayIndexInAttr) {
        PlayIndexInAttr obj = (PlayIndexInAttr) state;
        this.mItemIndex = obj.mItemIndex;
        this.mStartPositionMsecs = obj.mStartPositionMsecs;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_itemIndex)) {
            didChanged = (mItemIndex != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_itemIndex));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_startPositionMsecs)) {
            didChanged = (mStartPositionMsecs != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_startPositionMsecs));
            if(didChanged) return true;
        }
        return false;
   }
}