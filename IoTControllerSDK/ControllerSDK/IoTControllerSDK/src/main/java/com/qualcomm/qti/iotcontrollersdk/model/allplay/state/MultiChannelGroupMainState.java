/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupPlayersAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MultichannelGroupMainAttr;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class MultiChannelGroupMainState extends ResourceState {

  private final static String TAG = "MLAN_STATE";
  private final MultichannelGroupMainAttr multiChannelGroupMainAttr = new MultichannelGroupMainAttr();
  private HomeTheaterChannelMap channelMap;
  
  public MultiChannelGroupMainState() {
    channelMap = new HomeTheaterChannelMap();
  }

  public synchronized void update(MultichannelGroupMainAttr attr) {
    isAvailable = true;
    GenericStateApi.setState(multiChannelGroupMainAttr,attr);
    channelMap.update(attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
    isAvailable = true;
    boolean success = GenericStateApi.updateState(multiChannelGroupMainAttr, rep);
    if(success) {
      Log.d(TAG,"Update multi channel group main attribute success");
      channelMap.update(multiChannelGroupMainAttr);
    } else {
      Log.e(TAG,"Update multi channel group main attribute failed");
    }
    return success;
  }

  public synchronized HomeTheaterChannelMap getChannelInfo() {
      return channelMap.cloneChannelInfo();
  }

  public synchronized MultichannelGroupMainAttr getAttribute() {
      return GenericStateApi.getState(multiChannelGroupMainAttr);
  }

  public synchronized List<GroupPlayersAttr> getPlayerAttrs() {
      return GenericStateApi.getState(multiChannelGroupMainAttr.mGroup);
  }

}
