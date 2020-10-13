/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;

import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.IoTChannelMap;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MultichannelGroupMainAttr;
import java.util.HashMap;

public class HomeTheaterChannelMap {

    private static final String TAG = "ChannelMap";
    private HashMap<HomeTheaterChannel, MultiChannelInfo> mChannelMap = new HashMap<>();
    private int mVersion;

    public class MultiChannelInfo {

      private boolean mConnected;
      private String mDeviecId;
      private String mName;
      private double mVolumeRatio;


      public MultiChannelInfo(String id, String name, double volume, boolean connected) {
        this.mDeviecId = id;
        this.mName = name;
        this.mVolumeRatio = volume;
        this.mConnected = connected;
      }

      public MultiChannelInfo(MultiChannelInfo info) {
        this.mDeviecId = info.mDeviecId;
        this.mName = info.mName;
        this.mVolumeRatio = info.mVolumeRatio;
        this.mConnected = info.mConnected;
      }

      public boolean isAvailable() {
        return mConnected;
      }

      public double getVolumeRatio() {
        return mVolumeRatio;
      }
    }

    public void update(MultichannelGroupMainAttr mainAttr) {
      if(mainAttr == null) return;

      mChannelMap.clear();
      this.mVersion = mainAttr.mVersion;
      mainAttr.mGroup.forEach(attr->{
        MultiChannelInfo info = new MultiChannelInfo(attr.mDeviceId,attr.mDisplayName,attr.mVolumeRatio,attr.mConnected);
        attr.mChannelMap.forEach(channelId -> {
          IoTChannelMap ch = MultiChannelMapping.getIoTChannelMap(channelId);
          HomeTheaterChannel homeChannel = MultiChannelMapping.getHomeTheaterChannel(ch);
          if (homeChannel != HomeTheaterChannel.NONE)
            mChannelMap.put(homeChannel, info);
        });
      });
      logChannelMap();
    }

    public MultiChannelInfo getChannelInfo(HomeTheaterChannel channel) {
      return mChannelMap.get(channel);
    }

    public HomeTheaterChannelMap cloneChannelInfo() {
      HomeTheaterChannelMap retObj = new HomeTheaterChannelMap();
      retObj.mChannelMap = new HashMap<>();
      mChannelMap.forEach(((channel, info) ->
        retObj.mChannelMap.put(channel,new MultiChannelInfo(info))
      ));
      retObj.mVersion = mVersion;
      return retObj;
    }

    public void logChannelMap() {
      mChannelMap.forEach((homeTheaterChannel, multiChannelInfo)->
         Log.d(TAG,String.format("Home Theater:%d, Multi Channel:%s,connected:%b",homeTheaterChannel.ordinal(),multiChannelInfo.mName,multiChannelInfo.mConnected))
      );
    }

}
