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

public class PlayItemInAttr implements IResourceAttributes {


    public PlayItemAttr mItem;
    public int mStartPositionMsecs;

     public PlayItemInAttr() {
        mItem = new PlayItemAttr();
        mStartPositionMsecs = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/play_item_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_item,mItem.pack());
         rep.setValue(ResourceAttributes.Prop_startPositionMsecs,mStartPositionMsecs);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_item)) {
            didUnpack = true;
            PlayItemAttr obj = new PlayItemAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_item)))
                mItem = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_startPositionMsecs)) {
            didUnpack = true;
            mStartPositionMsecs = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_startPositionMsecs);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlayItemInAttr clonedObj = new PlayItemInAttr();
    clonedObj.mItem = (PlayItemAttr)mItem.getData();
    clonedObj.mStartPositionMsecs = this.mStartPositionMsecs;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlayItemInAttr) {
        PlayItemInAttr obj = (PlayItemInAttr) state;
        this.mItem.setData(obj.mItem);
        this.mStartPositionMsecs = obj.mStartPositionMsecs;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_item)) {
            didChanged = (mItem.checkDifference(rep.getValue(ResourceAttributes.Prop_item)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_startPositionMsecs)) {
            didChanged = (mStartPositionMsecs != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_startPositionMsecs));
            if(didChanged) return true;
        }
        return false;
   }
}