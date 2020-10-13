/**************************************************************************************************
 *  * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                               *
 * ************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.controller;

import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.DiscoveryInterface;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import java.util.ArrayDeque;
import org.iotivity.base.OcException;
import org.iotivity.base.QualityOfService;

/**
 * The abstract class defines generic APIs for {@link IoTAllPlayClient} and {@link IoTSysClient}
 */

public abstract class ControllerInterface {

   static final QualityOfService DEFAULT_QoS = QualityOfService.HIGH;
   final ArrayDeque<IoTBaseResourceClient> mObserverList = new ArrayDeque<>();
   DiscoveryInterface mDeviceDiscovery;

   String mUriPrefix;
   String mHost;
   String mId;
   long mDeviceDiscoveryTime;
   IoTCompletionCallback mCallback;

  abstract void discoverWithCompletion( IoTCompletionCallback listener) throws OcException;

  abstract void createResourceClientsForHost(String host) throws OcException;

  abstract public void stopObserving();

  public boolean cancelNextObserver() {
      TaskExecutors.getExecutor().executeOnResourceExecutor(() -> {
        IoTBaseResourceClient client = mObserverList.poll();
          if (client != null) {
            try {
              if(!client.stopObserving())
                cancelNextObserver();
            } catch (OcException e) {
              cancelNextObserver();
              e.printStackTrace();
            }
          } else {
            if(mCallback != null)
              mCallback.onCompletion(true);
          }
      });
      return true;
    }

  public void stopObserving(IoTCompletionCallback callback) {
    mCallback = callback;
    cancelNextObserver();
  }



  public long getDeviceFoundTime() {
    return mDeviceDiscoveryTime;
  }

}
