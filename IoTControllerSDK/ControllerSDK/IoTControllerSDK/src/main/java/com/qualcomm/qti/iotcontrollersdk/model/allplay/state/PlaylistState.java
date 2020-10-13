/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistAttr;
import java.util.ArrayList;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class PlaylistState extends ResourceState {

  private final PlaylistAttr playlistAttr = new PlaylistAttr();
  private boolean playlistSupported;
  private int size;
  private List<MediaItem> playItems;
  private String snapShotId;
  private int mIndex;

  public  PlaylistState() {
      playItems = new ArrayList<>();
  }

  public synchronized void update(PlaylistAttr attr) {
    GenericStateApi.setState(playlistAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
    return GenericStateApi.updateState(playlistAttr, rep);
  }

  public synchronized String getOwnerInfo() {
     return GenericStateApi.getState(playlistAttr.mOwnerInfo);
  }

  public synchronized void setOwnerInfo(String ownerInfo) {
    playlistAttr.mOwnerInfo = ownerInfo;
  }

  public synchronized String getSnapshotIdFromAttr() {
     return GenericStateApi.getState(playlistAttr.mSnapshotId);
  }

  public synchronized void setSnapshotIdIntoAttr(String snapshotId) {
    playlistAttr.mSnapshotId = snapshotId;
  }

  public synchronized PlaylistAttr getAttribute() {
      PlaylistAttr ret = (PlaylistAttr)playlistAttr.getData();
      return ret;
  }

  public synchronized String getUserData() {
     return GenericStateApi.getState(playlistAttr.mUserData);
  }

  public synchronized void setUserData(String userData) {
    playlistAttr.mUserData = userData;
  }

  public synchronized int getVersion() {
      return playlistAttr.mVersion;
  }

  public synchronized void setVersion(int version) {
    playlistAttr.mVersion = version;
  }

  public synchronized void setSupported(boolean isSupported) {
    playlistSupported = isSupported;
  }

  public synchronized int getSize() {
      return size;
  }

  public synchronized void setSize(int value) {
    size = value;
  }

  public synchronized String getSnapShotId() {
      return snapShotId;
  }

  public synchronized int getIndex() {
      return mIndex;
  }

  public synchronized void setIndex(int index) {
    mIndex = index;
  }

  public synchronized void setSnapShotId(String id) {
    snapShotId = id;
  }

  public synchronized List<MediaItem> getPlayItem() {
      return GenericStateApi.getIoTMediaState(playItems);
  }

  public synchronized void clearPlayItems() {
    playItems.clear();
  }

  public synchronized void setPlayItems(List<MediaItem> items) {
    for(MediaItem item:items) {
      playItems.add(item);
    }
  }
}
