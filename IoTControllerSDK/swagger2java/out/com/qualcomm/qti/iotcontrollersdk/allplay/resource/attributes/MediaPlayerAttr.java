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

public class MediaPlayerAttr implements IResourceAttributes {

    public enum LoopMode {
        kNone,
        kOne,
        kAll,
    }

    public enum ShuffleMode {
        kLinear,
        kShuffle,
    }


    public List<String>  mCapabilities;
    public String mDisplayName;
    public EnabledControlsAttr mEnabledControls;
    public int mLatencySetting;
    public LoopMode mLoopMode;
    public PlayStateAttr mPlayState;
    public ShuffleMode mShuffleMode;
    public int mVersion;

     public MediaPlayerAttr() {
        mCapabilities = new ArrayList<>();
        mDisplayName = "";
        mEnabledControls = new EnabledControlsAttr();
        mLatencySetting = 0;
        mLoopMode = LoopMode.kNone;
        mPlayState = new PlayStateAttr();
        mShuffleMode = ShuffleMode.kLinear;
        mVersion = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/media_player_rep_uri");

         rep.setValue(ResourceAttributes.Prop_latencySetting,mLatencySetting);
         rep.setValue(ResourceAttributes.Prop_loopMode,loopModeToString(mLoopMode));
         rep.setValue(ResourceAttributes.Prop_shuffleMode,shuffleModeToString(mShuffleMode));
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_capabilities)) {
            didUnpack = true;
            mCapabilities = ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_capabilities);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_displayName)) {
            didUnpack = true;
            mDisplayName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_displayName);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_enabledControls)) {
            didUnpack = true;
            EnabledControlsAttr obj = new EnabledControlsAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_enabledControls)))
                mEnabledControls = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_latencySetting)) {
            didUnpack = true;
            mLatencySetting = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_latencySetting);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_loopMode)) {
            didUnpack = true;
            mLoopMode = loopModeFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_loopMode));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_playState)) {
            didUnpack = true;
            PlayStateAttr obj = new PlayStateAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_playState)))
                mPlayState = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_shuffleMode)) {
            didUnpack = true;
            mShuffleMode = shuffleModeFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_shuffleMode));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didUnpack = true;
            mVersion = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_version);
        }
        return didUnpack;
    }

    public LoopMode loopModeFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("none")) {
                return LoopMode.kNone;
            }
            if (value.equalsIgnoreCase("one")) {
                return LoopMode.kOne;
            }
            if (value.equalsIgnoreCase("all")) {
                return LoopMode.kAll;
            }
        }
        return LoopMode.kNone;
    }

    public String loopModeToString(LoopMode value) {
        switch(value) {
            case kNone:
                return "none";
            case kOne:
                return "one";
            case kAll:
                return "all";
        }

        return "none";
    }

    public ShuffleMode shuffleModeFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("linear")) {
                return ShuffleMode.kLinear;
            }
            if (value.equalsIgnoreCase("shuffle")) {
                return ShuffleMode.kShuffle;
            }
        }
        return ShuffleMode.kLinear;
    }

    public String shuffleModeToString(ShuffleMode value) {
        switch(value) {
            case kLinear:
                return "linear";
            case kShuffle:
                return "shuffle";
        }

        return "linear";
    }


   @Override
   public Object getData() {
    MediaPlayerAttr clonedObj = new MediaPlayerAttr();
    clonedObj.mCapabilities = this.mCapabilities;
    clonedObj.mDisplayName = this.mDisplayName;
    clonedObj.mEnabledControls = (EnabledControlsAttr)mEnabledControls.getData();
    clonedObj.mLatencySetting = this.mLatencySetting;
    clonedObj.mLoopMode = this.mLoopMode;
    clonedObj.mPlayState = (PlayStateAttr)mPlayState.getData();
    clonedObj.mShuffleMode = this.mShuffleMode;
    clonedObj.mVersion = this.mVersion;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof MediaPlayerAttr) {
        MediaPlayerAttr obj = (MediaPlayerAttr) state;
        this.mCapabilities = obj.mCapabilities;
        this.mDisplayName = obj.mDisplayName;
        this.mEnabledControls.setData(obj.mEnabledControls);
        this.mLatencySetting = obj.mLatencySetting;
        this.mLoopMode = obj.mLoopMode;
        this.mPlayState.setData(obj.mPlayState);
        this.mShuffleMode = obj.mShuffleMode;
        this.mVersion = obj.mVersion;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_capabilities)) {
            didChanged = (!mCapabilities.equals(ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_capabilities)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_displayName)) {
            didChanged = (!mDisplayName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_displayName)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_enabledControls)) {
            didChanged = (mEnabledControls.checkDifference(rep.getValue(ResourceAttributes.Prop_enabledControls)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_latencySetting)) {
            didChanged = (mLatencySetting != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_latencySetting));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_loopMode)) {
            didChanged = (mLoopMode != loopModeFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_loopMode)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_playState)) {
            didChanged = (mPlayState.checkDifference(rep.getValue(ResourceAttributes.Prop_playState)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_shuffleMode)) {
            didChanged = (mShuffleMode != shuffleModeFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_shuffleMode)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didChanged = (mVersion != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_version));
            if(didChanged) return true;
        }
        return false;
   }
}