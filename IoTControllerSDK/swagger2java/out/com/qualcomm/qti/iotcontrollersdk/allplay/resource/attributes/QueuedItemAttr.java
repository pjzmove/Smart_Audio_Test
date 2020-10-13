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

public class QueuedItemAttr implements IResourceAttributes {


    public int mIndex;
    public PlayItemAttr mPlayItem;

     public QueuedItemAttr() {
        mIndex = 0;
        mPlayItem = new PlayItemAttr();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/queued_item_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_index)) {
            didUnpack = true;
            mIndex = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_index);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_playItem)) {
            didUnpack = true;
            PlayItemAttr obj = new PlayItemAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_playItem)))
                mPlayItem = obj;
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    QueuedItemAttr clonedObj = new QueuedItemAttr();
    clonedObj.mIndex = this.mIndex;
    clonedObj.mPlayItem = (PlayItemAttr)mPlayItem.getData();
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof QueuedItemAttr) {
        QueuedItemAttr obj = (QueuedItemAttr) state;
        this.mIndex = obj.mIndex;
        this.mPlayItem.setData(obj.mPlayItem);
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_index)) {
            didChanged = (mIndex != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_index));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_playItem)) {
            didChanged = (mPlayItem.checkDifference(rep.getValue(ResourceAttributes.Prop_playItem)));
            if(didChanged) return true;
        }
        return false;
   }
}