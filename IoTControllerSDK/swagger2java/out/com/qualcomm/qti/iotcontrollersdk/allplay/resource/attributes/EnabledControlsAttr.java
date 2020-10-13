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

public class EnabledControlsAttr implements IResourceAttributes {


    public boolean mLoopMode;
    public boolean mNext;
    public boolean mPause;
    public boolean mPrevious;
    public boolean mSeek;
    public boolean mShuffleMode;

     public EnabledControlsAttr() {
        mLoopMode = false;
        mNext = false;
        mPause = false;
        mPrevious = false;
        mSeek = false;
        mShuffleMode = false;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/enabled_controls_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_loopMode)) {
            didUnpack = true;
            mLoopMode = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_loopMode);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_next)) {
            didUnpack = true;
            mNext = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_next);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_pause)) {
            didUnpack = true;
            mPause = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_pause);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_previous)) {
            didUnpack = true;
            mPrevious = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_previous);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_seek)) {
            didUnpack = true;
            mSeek = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_seek);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_shuffleMode)) {
            didUnpack = true;
            mShuffleMode = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_shuffleMode);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    EnabledControlsAttr clonedObj = new EnabledControlsAttr();
    clonedObj.mLoopMode = this.mLoopMode;
    clonedObj.mNext = this.mNext;
    clonedObj.mPause = this.mPause;
    clonedObj.mPrevious = this.mPrevious;
    clonedObj.mSeek = this.mSeek;
    clonedObj.mShuffleMode = this.mShuffleMode;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof EnabledControlsAttr) {
        EnabledControlsAttr obj = (EnabledControlsAttr) state;
        this.mLoopMode = obj.mLoopMode;
        this.mNext = obj.mNext;
        this.mPause = obj.mPause;
        this.mPrevious = obj.mPrevious;
        this.mSeek = obj.mSeek;
        this.mShuffleMode = obj.mShuffleMode;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_loopMode)) {
            didChanged = (mLoopMode != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_loopMode));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_next)) {
            didChanged = (mNext != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_next));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_pause)) {
            didChanged = (mPause != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_pause));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_previous)) {
            didChanged = (mPrevious != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_previous));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_seek)) {
            didChanged = (mSeek != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_seek));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_shuffleMode)) {
            didChanged = (mShuffleMode != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_shuffleMode));
            if(didChanged) return true;
        }
        return false;
   }
}