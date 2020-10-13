/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EnabledControlsAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.LoopMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.ShuffleMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.QueuedItemAttr;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class MediaPlayerState extends ResourceState {

    private final MediaPlayerAttr mediaPlayerAttr = new MediaPlayerAttr();

    public MediaPlayerState() {
    }

    public synchronized void update(MediaPlayerAttr attr) {
      GenericStateApi.setState(mediaPlayerAttr, attr);
    }

    public synchronized boolean update(OcRepresentation rep) throws OcException {
        return GenericStateApi.updateState(mediaPlayerAttr, rep);
    }

    public synchronized MediaPlayerAttr getAttribute() {
        return GenericStateApi.getState(mediaPlayerAttr);
    }

    public synchronized List<String> getCapabilities() {
        return GenericStateApi.getPrimitiveTypeList(mediaPlayerAttr.mCapabilities);
    }

    public synchronized String getDisplayName() {
       return GenericStateApi.getState(mediaPlayerAttr.mDisplayName);
    }

    public synchronized EnabledControlsAttr getEnabledControls() {
       return GenericStateApi.getState(mediaPlayerAttr.mEnabledControls);
    }

    public synchronized LoopMode getLoopMode() {
       return GenericStateApi.getState(mediaPlayerAttr.mLoopMode);
    }

    public synchronized PlayStateAttr getPlayState() {
        return GenericStateApi.getState(mediaPlayerAttr.mPlayState);
    }

    public synchronized ShuffleMode getShuffleMode() {
        return GenericStateApi.getState(mediaPlayerAttr.mShuffleMode);
    }

    public synchronized int getVersion() {
        return GenericStateApi.getState(mediaPlayerAttr.mVersion);

    }

    public synchronized void setCapabilities(List<String> values) {
      mediaPlayerAttr.mCapabilities.clear();
      for(String val: values) {
        mediaPlayerAttr.mCapabilities.add(val);
      }
    }

    public synchronized void setDisplayName(String name) {
      mediaPlayerAttr.mDisplayName = name;
    }

    public synchronized void setEnabledControls(EnabledControlsAttr attr) {
      mediaPlayerAttr.mEnabledControls.setData(attr);
    }

    public synchronized void setLoopMode(LoopMode mode) {
      mediaPlayerAttr.mLoopMode = mode;
    }

    public synchronized void setPlayState(PlayStateAttr attr) {
      mediaPlayerAttr.mPlayState.setData(attr);
    }

    public synchronized void setShuffleMode(ShuffleMode mode) {
      mediaPlayerAttr.mShuffleMode = mode;
    }

    public synchronized void setVersion(int version) {
      mediaPlayerAttr.mVersion = version;
    }

    public synchronized List<QueuedItemAttr> getQueueItems() {
      return GenericStateApi.getState(mediaPlayerAttr.mPlayState.mQueuedItems);
    }

    public synchronized void setQueueItems(List<QueuedItemAttr> attrs) {
      GenericStateApi.setStateList(mediaPlayerAttr.mPlayState.mQueuedItems,attrs);
    }

}
