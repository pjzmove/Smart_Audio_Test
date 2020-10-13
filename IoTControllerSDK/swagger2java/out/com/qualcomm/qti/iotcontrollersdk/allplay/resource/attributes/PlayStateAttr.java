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

public class PlayStateAttr implements IResourceAttributes {

    public enum PlayState {
        kStopped,
        kBuffering,
        kPlaying,
        kPaused,
    }


    public int mAudioChannels;
    public int mBitsPerSample;
    public String mGroupLeadId;
    public PlayState mPlayState;
    public int mPositionMsecs;
    public List<QueuedItemAttr>  mQueuedItems;
    public int mSampleRate;

     public PlayStateAttr() {
        mAudioChannels = 0;
        mBitsPerSample = 0;
        mGroupLeadId = "";
        mPlayState = PlayState.kStopped;
        mPositionMsecs = 0;
        mQueuedItems = new ArrayList<>();
        mSampleRate = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/play_state_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_audioChannels)) {
            didUnpack = true;
            mAudioChannels = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_audioChannels);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_bitsPerSample)) {
            didUnpack = true;
            mBitsPerSample = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_bitsPerSample);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_groupLeadId)) {
            didUnpack = true;
            mGroupLeadId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupLeadId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_playState)) {
            didUnpack = true;
            mPlayState = playStateFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_playState));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_positionMsecs)) {
            didUnpack = true;
            mPositionMsecs = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_positionMsecs);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_queuedItems)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_queuedItems);
            if(reps != null) {
                mQueuedItems.clear();
                for (OcRepresentation elem_rep: reps) {
                    QueuedItemAttr obj = new QueuedItemAttr();
                    if(obj.unpack(elem_rep))
                        mQueuedItems.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_sampleRate)) {
            didUnpack = true;
            mSampleRate = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_sampleRate);
        }
        return didUnpack;
    }

    public PlayState playStateFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("stopped")) {
                return PlayState.kStopped;
            }
            if (value.equalsIgnoreCase("buffering")) {
                return PlayState.kBuffering;
            }
            if (value.equalsIgnoreCase("playing")) {
                return PlayState.kPlaying;
            }
            if (value.equalsIgnoreCase("paused")) {
                return PlayState.kPaused;
            }
        }
        return PlayState.kStopped;
    }

    public String playStateToString(PlayState value) {
        switch(value) {
            case kStopped:
                return "stopped";
            case kBuffering:
                return "buffering";
            case kPlaying:
                return "playing";
            case kPaused:
                return "paused";
        }

        return "stopped";
    }


   @Override
   public Object getData() {
    PlayStateAttr clonedObj = new PlayStateAttr();
    clonedObj.mAudioChannels = this.mAudioChannels;
    clonedObj.mBitsPerSample = this.mBitsPerSample;
    clonedObj.mGroupLeadId = this.mGroupLeadId;
    clonedObj.mPlayState = this.mPlayState;
    clonedObj.mPositionMsecs = this.mPositionMsecs;
    for(QueuedItemAttr item:mQueuedItems) {
        clonedObj.mQueuedItems.add((QueuedItemAttr)item.getData());
    }
    clonedObj.mSampleRate = this.mSampleRate;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlayStateAttr) {
        PlayStateAttr obj = (PlayStateAttr) state;
        this.mAudioChannels = obj.mAudioChannels;
        this.mBitsPerSample = obj.mBitsPerSample;
        this.mGroupLeadId = obj.mGroupLeadId;
        this.mPlayState = obj.mPlayState;
        this.mPositionMsecs = obj.mPositionMsecs;
        this.mQueuedItems.clear();
        for(QueuedItemAttr item:obj.mQueuedItems) {
            this.mQueuedItems.add((QueuedItemAttr)item.getData());
        }
        this.mSampleRate = obj.mSampleRate;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_audioChannels)) {
            didChanged = (mAudioChannels != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_audioChannels));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_bitsPerSample)) {
            didChanged = (mBitsPerSample != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_bitsPerSample));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_groupLeadId)) {
            didChanged = (!mGroupLeadId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupLeadId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_playState)) {
            didChanged = (mPlayState != playStateFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_playState)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_positionMsecs)) {
            didChanged = (mPositionMsecs != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_positionMsecs));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_queuedItems)) {
            reps = rep.getValue(ResourceAttributes.Prop_queuedItems);
            if(reps != null) {
                if(mQueuedItems.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mQueuedItems.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_sampleRate)) {
            didChanged = (mSampleRate != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_sampleRate));
            if(didChanged) return true;
        }
        return false;
   }
}