/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager;

import android.content.Context;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.AVSOnboardingErrorAttr.Error;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTBluetoothDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTSysUpdatesDelegate;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTVoiceUIClient;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BatteryStatusAttr;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class IoTSysManager implements IoTSysUpdatesDelegate {

  private static IoTSysManager mInstance = new IoTSysManager();

  private WeakReference<Context> mContextRef;
  private final List<onVoiceUiListener> mVoiceUiListeners = new ArrayList<>();
  private final List<onSystemListener> mSystemListeners = new ArrayList<>();
  private final List<onZigbeeListener> mZigbeeListeners = new ArrayList<>();
  private final List<OnBluetoothListener> mOnBluetoothListeners = new ArrayList<>();

  public interface onVoiceUiListener {

   default void voiceUIClientsDidChange(List<IoTVoiceUIClient> voiceUIClients) {
   }

   default void voiceUIEnabledStateDidChange(boolean enabled) {
   }

   default void voiceUIDefaultClientDidChange(IoTVoiceUIClient voiceUIClient) {
   }

   default void voiceUIDidProvideAVSAuthenticationCode(String code, String url) {
   }

   default void voiceUIOnboardingDidErrorWithTimeout(Error didTimeout, int reattempt) {
   }

	}

	public boolean isZbCoordinator(String deviceId) {
      return IoTService.getInstance().isZbCoordinator(deviceId);
  }

  public boolean isZigbeeCoordinatorExisted(String deviceId) {
    return IoTService.getInstance().isZbCoordinatorExisted(deviceId);
  }

	public interface onSystemListener {
   void didChangeName(String name);
   void deviceDidChangeBatteryState(BatteryStatusAttr attr);
	}

	public interface onZigbeeListener {
      void onZbAdapterStateChanged(IoTDevice device);
      void onZbCoordinatorStateDidChanged(IoTDevice device);
      void onZbJoinedDevicesDidChanged(IoTDevice device);
      void OnZbJoiningStateDidChanged(IoTDevice device, boolean allowed);
	}

  /**
    * <p>This listener gets events related to the Bluetooth feature of a device. It informs when any of
    * its state change.</p>
    */
   public interface OnBluetoothListener {
     /**
      * <p>Notifies the enable state of the Bluetooth Adapter of the provided device.</p>
      *
      * @param device
      *         The device for which the Bluetooth adapter state has been updated.
      * @param state
      *         The new state of the Bluetooth adapter.
      */
     void onBluetoothAdapterStateChanged(IoTDevice device, IoTDevice.IoTBluetoothAdapterState state);

     /**
      * <p>Notifies the new Discoverable state of a device.</p>
      *
      * @param device
      *         The device for which the Discoverable state has been updated.
      * @param state
      *         The new Discoverable state.
      */
     void onBluetoothDiscoverableStateChanged(IoTDevice device, IoTDevice.IoTBluetoothDiscoverableState state);
     /**
      * <p>Notifies the updated list of devices paired with the given device.</p>
      *
      * @param device
      *         The device for which the Discoverable state has been updated.
      * @param devices
      *         The updated list of paired devices.
      */
     void onPairedDevicesUpdated(IoTDevice device, List<IoTBluetoothDevice> devices);
     /**
      * <p>Notifies a Bluetooth error which has occurred on the device.</p>
      *
      * @param device
      *         The device for which the Discoverable state has been updated.
      * @param error
      *         The error which occurs.
      */
     void onBluetoothError(IoTDevice device, IoTDevice.IoTBluetoothError error);
     /**
      * <p>Notifies the Bluetooth connection state of the Bluetooth device with the IoT device.</p>
      *
      * @param device
      *          The IoT device the Bluetooth device is connected with/disconnected from.
      * @param bluetoothDevice
      *          The Bluetooth device which is connected/disconnected.
      */
     void onConnectedBluetoothDeviceChanged(IoTDevice device, IoTBluetoothDevice bluetoothDevice);
     /**
      * <p>Notifies the Bluetooth scan state of the IoT device.</p>
      *
      * @param device
      *          The IoT device for which the state has changed.
      * @param scanning
      *          True if the device is scanning, false otherwise.
      */
     void onBluetoothScanStateChanged(IoTDevice device, boolean scanning);
     /**
      * <p>Notifies the discovery of a Bluetooth device while scanning for devices.</p>
      *
      * @param device The IoT device for which the state has changed.
      * @param scanned The Bluetooth device which has been found.
      */
     void onBluetoothDeviceDiscovered(IoTDevice device, IoTBluetoothDevice scanned);
     /**
      * <p>Notifies when a device has been paired or unpaired with the IoTDevice.</p>
      *
      * @param device The IoT device for which the state has changed.
      * @param pairDevice the device which has been paired and unpaired.
      * @param paired the new pair status between the IoT device and the Bluetooth device.
      */
     void onBluetoothPairStateUpdated(IoTDevice device, IoTBluetoothDevice pairDevice, boolean paired);
   }

  private IoTSysManager() {

  }

  public static void init(Context context) {
    mInstance.mContextRef = new WeakReference<>(context);
    IoTService.getInstance().setIoTSysDelegate(mInstance);
  }

  public static IoTSysManager getInstance() throws Exception {
    if(mInstance.mContextRef == null || mInstance.mContextRef.get() == null)
      throw new Exception("no IoTSys manager instance created!");
    return mInstance;
  }

  public void addVoiceUiListener(onVoiceUiListener listener) {
    if(listener != null && !mVoiceUiListeners.contains(listener)) {
      mVoiceUiListeners.add(listener);
    }
  }

  public void addSystemListener(onSystemListener listener) {
    if(listener != null && !mSystemListeners.contains(listener)) {
      mSystemListeners.add(listener);
    }
  }

  public void removeVoiceUiListener(onVoiceUiListener listener) {
    if(listener != null) {
      mVoiceUiListeners.remove(listener);
    }
  }

  public void removeSystemListener(onSystemListener listener) {
    if(listener != null ) {
      mSystemListeners.remove(listener);
    }
  }

  public void setDeviceName(String host, String name, IoTCompletionCallback callback) {
    IoTService.getInstance().setDeviceName(host, name, callback);
  }

  public void setZigbeeName(String host, String name, int id, IoTCompletionCallback callback) {
    IoTService.getInstance().setZigbeeName(host, name, id, callback);
  }

  @Override
  public void didChangeName(String name) {
    synchronized (mSystemListeners) {
      for (onSystemListener listener : mSystemListeners) {
        listener.didChangeName(name);
      }
    }
  }

  @Override
  public void deviceDidChangeBatteryState(BatteryStatusAttr attr) {
    synchronized (mSystemListeners) {
      for (onSystemListener listener : mSystemListeners) {
        listener.deviceDidChangeBatteryState(attr);
      }
    }
  }

  @Override
  public void deviceDidChangeEthernetState() {

  }

  @Override
  public void deviceDidChangeWiFiState() {

  }

  @Override
  public void deviceDidChangeAccessPointState() {

  }

  /**
    * <p>This method unregisters the given listener from the list of listeners.</p>
    *
    * @param listener
    *         The listener to unregister.
    */
   public void removeOnBluetoothListener(final OnBluetoothListener listener) {
     if (listener != null) {
       synchronized (mOnBluetoothListeners) {
         mOnBluetoothListeners.remove(listener);
       }
     }
   }

 /**
    * <p>This method registers the given listener for Bluetooth State events.</p>
    *
    * @param listener
    *         The listener to register.
    */
   public void addOnBluetoothListener(final OnBluetoothListener listener) {
     if (listener != null) {
       synchronized (mOnBluetoothListeners) {
         if (!mOnBluetoothListeners.contains(listener)) {
           mOnBluetoothListeners.add(listener);
         }
       }
     }
   }

  @Override // IoTDevice.IoTSysUpdatesDelegate
   public void btScanDidDiscoverBtDevice(IoTDevice device, IoTBluetoothDevice scanned) {
     synchronized (mOnBluetoothListeners) {
       for (OnBluetoothListener listener : mOnBluetoothListeners) {
         listener.onBluetoothDeviceDiscovered(device, scanned);
       }
     }
   }

  @Override // IoTDevice.IoTSysUpdatesDelegate
   public void btScanStateDidChange(IoTDevice device, boolean scanning) {
     synchronized (mOnBluetoothListeners) {
       for (OnBluetoothListener listener : mOnBluetoothListeners) {
         listener.onBluetoothScanStateChanged(device, scanning);
       }
     }
   }

  @Override // IoTDevice.IoTSysUpdatesDelegate
   public void btAdapterStateDidChange(IoTDevice device, IoTDevice.IoTBluetoothAdapterState state) {
     synchronized (mOnBluetoothListeners) {
       for (OnBluetoothListener listener : mOnBluetoothListeners) {
         listener.onBluetoothAdapterStateChanged(device, state);
       }
     }
   }

  public void btDiscoverableStateDidChange(IoTDevice device, IoTDevice.IoTBluetoothDiscoverableState state) {
     synchronized (mOnBluetoothListeners) {
       for (OnBluetoothListener listener : mOnBluetoothListeners) {
         listener.onBluetoothDiscoverableStateChanged(device, state);
       }
     }
   }

  @Override // IoTDevice.IoTSysUpdatesDelegate
   public void btConnectedDeviceHasChanged(IoTDevice device, IoTBluetoothDevice bluetoothDevice) {
     synchronized (mOnBluetoothListeners) {
       for (OnBluetoothListener listener : mOnBluetoothListeners) {
         listener.onConnectedBluetoothDeviceChanged(device, bluetoothDevice);
       }
     }
   }

   @Override // IoTDevice.IoTSysUpdatesDelegate
   public void btPairedDevicesDidChange(IoTDevice device, List<IoTBluetoothDevice> devices) {
     synchronized (mOnBluetoothListeners) {
       for (OnBluetoothListener listener : mOnBluetoothListeners) {
         listener.onPairedDevicesUpdated(device, devices);
       }
     }
   }

   @Override // IoTDevice.IoTSysUpdatesDelegate
   public void btPairStateDidChange(IoTDevice device, IoTBluetoothDevice pairDevice, boolean paired) {
     synchronized (mOnBluetoothListeners) {
       for (OnBluetoothListener listener : mOnBluetoothListeners) {
         listener.onBluetoothPairStateUpdated(device, pairDevice, paired);
       }
     }
   }

  @Override // IoTDevice.IoTSysUpdatesDelegate
   public void btError(IoTDevice device, IoTDevice.IoTBluetoothError error) {
     synchronized (mOnBluetoothListeners) {
       for (OnBluetoothListener listener : mOnBluetoothListeners) {
         listener.onBluetoothError(device, error);
       }
     }
  }

  @Override
  public void voiceUIClientsDidChange(List<IoTVoiceUIClient> voiceUIClients) {
    synchronized (mVoiceUiListeners) {
      for (onVoiceUiListener listener : mVoiceUiListeners) {
        listener.voiceUIClientsDidChange(voiceUIClients);
      }
    }
  }

  @Override
  public void voiceUIEnabledStateDidChange(boolean enabled) {
    synchronized (mVoiceUiListeners) {
      for (onVoiceUiListener listener : mVoiceUiListeners) {
        listener.voiceUIEnabledStateDidChange(enabled);
      }
    }
  }

  @Override
  public void voiceUIDefaultClientDidChange(IoTVoiceUIClient voiceUIClient) {
    synchronized (mVoiceUiListeners) {
      for (onVoiceUiListener listener : mVoiceUiListeners) {
        listener.voiceUIDefaultClientDidChange(voiceUIClient);
      }
    }
  }

  @Override
  public void voiceUIDidProvideAVSAuthenticationCode(String code, String url) {
    synchronized (mVoiceUiListeners) {
      for (onVoiceUiListener listener : mVoiceUiListeners) {
        listener.voiceUIDidProvideAVSAuthenticationCode(code, url);
      }
    }
  }

  @Override
  public void voiceUIOnboardingDidErrorWithTimeout(Error error, int reattempt) {
    synchronized (mVoiceUiListeners) {
      for (onVoiceUiListener listener : mVoiceUiListeners) {
        listener.voiceUIOnboardingDidErrorWithTimeout(error, reattempt);
      }
    }
  }

  public void addZigbeeiListener(onZigbeeListener listener) {
    synchronized (mZigbeeListeners) {
      if (listener != null && !mZigbeeListeners.contains(listener)) {
        mZigbeeListeners.add(listener);
      }
    }
  }

  public void removeZigbeeListener(onZigbeeListener listener) {
    synchronized (mZigbeeListeners) {
      if (listener != null) {
        mZigbeeListeners.remove(listener);
      }
    }
  }

  @Override
  public void onZbAdapterStateChanged(IoTDevice device) {
    synchronized (mZigbeeListeners) {
    for (onZigbeeListener listener : mZigbeeListeners) {
      listener.onZbAdapterStateChanged(device);
     }
    }
  }

  @Override
  public void onZbCoordinatorStateDidChanged(IoTDevice device) {
    synchronized (mZigbeeListeners) {
    for (onZigbeeListener listener : mZigbeeListeners) {
      listener.onZbCoordinatorStateDidChanged(device);
     }
    }
  }

  @Override
  public void onZbJoinedDevicesDidChanged(IoTDevice device) {
    synchronized (mZigbeeListeners) {
    for (onZigbeeListener listener : mZigbeeListeners) {
      listener.onZbJoinedDevicesDidChanged(device);
     }
    }
  }

  @Override
  public void OnZbJoiningStateDidChanged(IoTDevice device, boolean allowed) {
    synchronized (mZigbeeListeners) {
    for (onZigbeeListener listener : mZigbeeListeners) {
      listener.OnZbJoiningStateDidChanged(device, allowed);
     }
    }
  }
}
