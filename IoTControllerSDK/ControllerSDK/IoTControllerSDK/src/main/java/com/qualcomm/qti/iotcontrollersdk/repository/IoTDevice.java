/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.repository;

import static com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.AdapterState.kEnabled;
import static com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.VoiceUiState.AVSClientName;
import static com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTVoiceUIClientType.IoTVoiceUIClientTypeUnknown;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import com.qualcomm.qti.iotcontrollersdk.constants.BluetoothStatus;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import com.qualcomm.qti.iotcontrollersdk.constants.NetworkInterface;
import com.qualcomm.qti.iotcontrollersdk.constants.OnboardingError;
import com.qualcomm.qti.iotcontrollersdk.constants.UpdateStatus;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTSysClient;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTSysClient.IoTSysClientObserver;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTSysClient.IoTSysObserveOrigin;
import com.qualcomm.qti.iotcontrollersdk.controller.TaskExecutors;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import com.qualcomm.qti.iotcontrollersdk.iotsys.listeners.VoiceUIListener;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.AVSOnboardingErrorAttr.Error;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.AddressInAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BatteryStatusAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BluetoothAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BluetoothAttr.AdapterState;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BtDeviceAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BtDeviceStatusAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BtErrorAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BtSignalAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BtSignalAttr.Name;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ClientInAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.EnableBtAdapterInAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.EnableZbAdapterInAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.SelectClientInAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.SetZbFriendlyNameInAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.SystemAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.TimeoutSecInAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.VoiceUIAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.VoiceUIClientAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZbDeviceAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.CoordinatorState;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.JoiningState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.ScanInfo;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.IoTSysStates;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.BluetoothState;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.SystemState;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.VoiceUiState;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.ZigbeeState;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

/**
 * The class provides APIs for the app to access to IoTSys service
 */
public class IoTDevice extends IoTRepository {

    private final static String TAG ="IoTDevice";

    private IoTSysClient mClient;

    private final AtomicBoolean isFirstTimeFetchComplete;

    private final IoTSysStates mState = new IoTSysStates();
    private CountDownLatch mLatch;

    private IoTSysObserver mObserver;

    private TaskExecutors mExecutor;

    private IoTSysUpdatesDelegate mUpdatesDelegate;

    private String mId;
    public String mWifiSSID;
    public boolean isFirmwareUpdateProgressSupported;
    public boolean isPasswordSet;
    public boolean isPasswordSupported;

    public interface IoTSysUpdatesDelegate {
      void didChangeName(String name);
      void deviceDidChangeBatteryState(BatteryStatusAttr attr);

      //Network Attribute notification
      void deviceDidChangeEthernetState();
      void deviceDidChangeWiFiState();
      void deviceDidChangeAccessPointState();

      //Bluetooth Attribute notification
      /**
       * <p>This is called when the IoT system observes that an IoTDevice has found a Bluetooth
       * device.</p>
       *
       * @param device Gives the IoT Device for which the change has been observed.
       * @param scanResult The information of the Bluetooth device which has been found.
       */
      void btScanDidDiscoverBtDevice(IoTDevice device, IoTBluetoothDevice scanResult);

      /**
       * <p>This is called when the IoT system observes that the scanning has stopped.</p>
       *
       * @param device Gives the IoT Device for which the change has been observed.
       * @param scanning Always set to <code>false</code> when called.
       */
      void btScanStateDidChange(IoTDevice device, boolean scanning);

      /**
       * <p>This is called when the IoT system observes a change of state for the Bluetooth adapter of
       * the IoT
       * Device.</p>
       *
       * @param device Gives the IoT Device for which the change has been observed.
       * @param state Gives the new state of the Bluetooth Adapter.
       */
      void btAdapterStateDidChange(IoTDevice device, IoTBluetoothAdapterState state);

      /**
       * <p>This is called when the IoT system observes a change of state for the Discoverable mode of
       * the IoT
       * Device.</p>
       *
       * @param device Gives the IoT Device for which the change has been observed.
       * @param state Gives the new state of the discoverable mode.
       */
      void btDiscoverableStateDidChange(IoTDevice device, IoTBluetoothDiscoverableState state);

      /**
       * <p>This is called when the IoT system observes that the connection state of a Bluetooth
       * device with the
       * IoT Device has changed .</p>
       *
       * @param device Gives the IoT Device for which the change has been observed.
       * @param bluetoothDevice The Bluetooth device for which the connection state has changed. This
       * can be null if there is no connected device.
       */
      void btConnectedDeviceHasChanged(IoTDevice device, IoTBluetoothDevice bluetoothDevice);

      /**
       * <p>This is called when the IoT system observes that the list of Bluetooth devices paired with
       * the IoT
       * Device has changed.</p>
       *
       * @param device Gives the IoT Device for which the change has been observed.
       * @param devices The updated list of paired devices.
       */
      void btPairedDevicesDidChange(IoTDevice device, List<IoTBluetoothDevice> devices);

      /**
       * <p>This is called when the IoT system receives a new pair state from the IoT Device.</p>
       *
       * @param device Gives the IoT Device for which the change has been observed.
       * @param pairDevice the device for which the paired state has changed.
       * @param paired True if the devices are now paired, false if they are not anymore.
       */
      void btPairStateDidChange(IoTDevice device, IoTBluetoothDevice pairDevice, boolean paired);

      /**
       * <p>This method is called when the IoT system observes a Bluetooth error received from an IoT
       * Device.</p>
       *
       * @param device Gives the IoT Device for which the error has been observed.
       * @param error The error which occurs.
       */
      void btError(IoTDevice device, IoTBluetoothError error);


      //Voice UI attribute notification
      void voiceUIClientsDidChange(List<IoTVoiceUIClient> voiceUIClients);
      void voiceUIEnabledStateDidChange(boolean enabled);
      void voiceUIDefaultClientDidChange(IoTVoiceUIClient voiceUIClient);
      void voiceUIDidProvideAVSAuthenticationCode(String code, String url);
      void voiceUIOnboardingDidErrorWithTimeout(Error error, int reattempt);


      //Zigbee
      void onZbAdapterStateChanged(IoTDevice device);
      void onZbCoordinatorStateDidChanged(IoTDevice device);
      void onZbJoinedDevicesDidChanged(IoTDevice device);
      void OnZbJoiningStateDidChanged(IoTDevice device, boolean changed);
    }

    public enum IoTVoiceUIClientType {
      IoTVoiceUIClientTypeUnknown,
      IoTVoiceUIClientTypeAVS,
      IoTVoiceUIClientTypeGVA,
      IoTVoiceUIClientTypeCortana,
      IoTVoiceUIClientTypeModular
    }

    public class IoTVoiceUIClient {
      public String name;
      public IoTVoiceUIClientType type;
      public boolean enabled;
      public boolean onboarded;
      public boolean wakewordEnabled;

      IoTVoiceUIClient(String name, boolean enabled,boolean onBoarded, boolean wakeword) {
        this.name = name;
        this.type = getClientTypeForName(name);
        this.enabled = enabled;
        this.onboarded = onBoarded;
        this.wakewordEnabled = wakeword;
      }
    }

    /**
     * All states of the IoT device Bluetooth Adapter. These states determine if the Bluetooth feature
     * is enabled or disabled.
     */
    public enum IoTBluetoothAdapterState {
      Enabled,
      Disabled,
      Unknown
    }

    /**
     * All states of the IoT device discoverable mode. These states determine if the device can be
     * discovered by other Bluetooth devices.
     */
    public enum IoTBluetoothDiscoverableState {
      Discoverable,
      NonDiscoverable,
      Unknown
    }

  /**
     * <p>This class describes a Bluetooth error which might have occurred on the device.</p>
     */
    public static class IoTBluetoothError {

      /**
       * The error status
       */
      private String mmStatus;
      /**
       * The address of the Bluetooth device with which the error occurred - if it occurred with a
       * device.
       */
      private String mmAddress;

      /**
       * To build a new instance of a Bluetooth error.
       *
       * @param status The error which occurs
       * @param address The address of the device with which the error occurred or <code>null</code>
       * if the error is not related to a device.
       */
      public IoTBluetoothError(String address, String status) {
        this.mmStatus = status;
        this.mmAddress = address;
      }

      /**
       * <p>To get the status of the error.</p>
       *
       * @return The error status as sent by the device.
       */
      public String getStatus() {
        return mmStatus;
      }

      /**
       * <p>To get the Bluetooth address of the device with which the issue occurred.</p>
       *
       * @return The Bluetooth address of the device. If the error was not related to a Bluetooth
       * device, this could be empty or <code>null</code>.
       */
      public String getAddress() {
        return mmAddress;
      }
    }

    public IoTDevice(IoTSysClient client) {
      super(IoTType.IOT_DEVICE);
      mClient = client;
      mExecutor = TaskExecutors.getExecutor();
      mObserver = new IoTSysObserver(this);
      client.registerObserver(mObserver);
      isFirstTimeFetchComplete = new AtomicBoolean(false);
    }

    @Override
    public String getName() {
      return mState.getSystemState().getDeviceFriendlyName();
    }

    @Override
    public synchronized String getId() {
      return mId;
    }

    public synchronized void setId(String id) {
      mId = id;
    }

    @Override
    public IoTType getType() {
      return IoTType.IOT_DEVICE;
    }

    public String getHostName() {
      return mClient.getHostName();
    }

    @Override
    public boolean dispose() {
      mClient.stopObserving();
      return true;
    }

    public boolean dispose(IoTCompletionCallback callback) {
      boolean ret = false;
      if(mClient != null) {
        mClient.stopObserving(callback);
        ret = true;
      }
      return ret;
    }

    public boolean isNetworkInfoAvailable() {
      return mState.getNetworkState().isAvailable();
    }

    public boolean isVoiceUIEnabled() {
      return mState.getVoiceUiState().isEnableVoiceUI();
    }

    public boolean isAvsOnBoarded() {
      return mState.getVoiceUiState().isAVSOnboarded();
    }

    public boolean isAVSWakeword() {
      return mState.getVoiceUiState().getWakewordStatus(AVSClientName);
    }

    public String getAVSLocale() {
      return mState.getVoiceUiState().getAVSLocale();
    }

    public boolean isZigbeeAvailable() {
      return mState.getZigbeeState().isAvailable();
    }

    public boolean isZigbeeEnabled() {
      ZigbeeState state = mState.getZigbeeState();
      return state.isAvailable() && state.getAdapterState() == kEnabled;
    }

  public List<IoTZigbeeDevice> getZGJoinedDevices() {
    List<IoTZigbeeDevice> result = new ArrayList<>();
    List<ZbDeviceAttr> zbDevices = mState.getZigbeeState().getJoinedDevices();
    zbDevices.forEach(attr -> result
        .add(new IoTZigbeeDevice(attr.mDeviceIdentifier, attr.mDeviceType, attr.mFriendlyName)));
    return result;
  }

    public String getFirmwareVersion() {
       return mState.getSystemState().getFirmwareVersion();
    }

    public String getModel() {
      return mState.getSystemState().getModel();
    }

    public String getManufacturer() {
      return mState.getSystemState().getManufacturer();
    }

    public String getWifiIPAddress() {
      return mState.getNetworkState().getWifiAdapter().mIPAddress;
    }

    public String getWifiMacAddress() {
      return mState.getNetworkState().getWifiAdapter().mMacAddress;
    }

    public String getEthernetIPAddress() {
      return mState.getNetworkState().getEthrnerAdapter().mIPAddress;
    }

    public String getEthernetMacAddress() {
      return mState.getNetworkState().getEthrnerAdapter().mMacAddress;
    }

    @Override
    public boolean equals(Object other) {
      if ((other == null) || !(other instanceof IoTDevice)) {
        return false;
      }
      return getId().equalsIgnoreCase(((IoTDevice) other).getId());
    }

    public void setDelegate(IoTSysUpdatesDelegate delegate) {
      mUpdatesDelegate = delegate;
    }

    private class IoTSysObserver implements IoTSysClientObserver {

      private WeakReference<IoTDevice> mParent;

      public IoTSysObserver(IoTDevice device) {
        mParent = new WeakReference<>(device);
      }

      @Override
      public void onUpdate(IoTSysObserveOrigin type, OcRepresentation rep) {
         Log.d(TAG,"***** IoTSysObserveOrigin update for "+type + "***** begin" + mId + "," + mClient.getHostName());
         boolean canUpdate = isFirstTimeFetchComplete.get();
         IoTDevice device = mParent.get();
         switch(type) {
           case IoTSysObserveOriginBluetooth: {
            try {
              device.onObserveBluetoothUpdate(rep, canUpdate);
            } catch (OcException e) {
              e.printStackTrace();
            }
           }
           break;
           case IoTSysObserveOriginNetwork: {
           }
           break;
           case IoTSysObserveOriginSystem: {
             try {
               SystemState state = mState.getSystemState();
               if(state.update(rep)) {
                 if (canUpdate && mUpdatesDelegate != null) {
                     mUpdatesDelegate.didChangeName(state.getDeviceFriendlyName());
                 }

                 if (rep.hasAttribute(ResourceAttributes.Prop_batteryStatus)) {
                   if (canUpdate && mUpdatesDelegate != null) {
                     mUpdatesDelegate.deviceDidChangeBatteryState(state.getBatteryStatus());
                   }
                 }
               }
             } catch (OcException e) {
               e.printStackTrace();
             }
           }
           break;
           case IoTSysObserveOriginVoiceUI: {
             try {
               if(mState.getVoiceUiState().update(rep)) {

                  if( rep.hasAttribute(ResourceAttributes.Prop_voiceUIClients) ||
                      rep.hasAttribute(ResourceAttributes.Prop_AVSOnboarded) ||
                      rep.hasAttribute(ResourceAttributes.Prop_AVSLocale) ||
                      rep.hasAttribute(ResourceAttributes.Prop_selectedModularClientLanguage)) {

                       if(canUpdate && mUpdatesDelegate != null) {
                        mUpdatesDelegate.voiceUIClientsDidChange(getVoiceUiClients());
                       }
                 }

                 if(rep.hasAttribute(ResourceAttributes.Prop_enableVoiceUI))  {

                    if(canUpdate && mUpdatesDelegate != null) {
                        mUpdatesDelegate.voiceUIEnabledStateDidChange(mState.getVoiceUiState().isEnableVoiceUI());
                    }
                 }

                 if(rep.hasAttribute(ResourceAttributes.Prop_defaultVoiceUIClient)) {
                    if(canUpdate && mUpdatesDelegate != null) {
                        mUpdatesDelegate.voiceUIDefaultClientDidChange(getDefaultVoiceUIClient());
                     }
                 }

                 if(rep.hasAttribute(ResourceAttributes.Prop_authenticateAVS)) {
                   if(canUpdate && mUpdatesDelegate != null) {
                        mUpdatesDelegate.voiceUIDidProvideAVSAuthenticationCode(mState.getVoiceUiState().getAuthenticateAVS().mCode,mState.getVoiceUiState().getAuthenticateAVS().mUrl);
                   }
                 }

                 if(rep.hasAttribute(ResourceAttributes.Prop_AVSOnboardingError)) {
                    if(canUpdate && mUpdatesDelegate != null) {
                        int reattempt = mState.getVoiceUiState().getAVSOnboardingError().mReattempt;
                        mUpdatesDelegate.voiceUIOnboardingDidErrorWithTimeout(mState.getVoiceUiState().getAVSOnboardingError().mError, reattempt);
                    }
                 }

               }
             } catch (OcException e) {
               e.printStackTrace();
             }
           }
           break;
           case IoTSysObserveOriginZigbee:
            try {
              ZigbeeState state = mState.getZigbeeState();
              if (mState.getZigbeeState().update(rep)) {
                if(canUpdate){
                  if(rep.hasAttribute(ResourceAttributes.Prop_adapterState)) {
                    mUpdatesDelegate.onZbAdapterStateChanged(mParent.get());
                  }

                  if(rep.hasAttribute(ResourceAttributes.Prop_coordinatorState)) {
                    mUpdatesDelegate.onZbCoordinatorStateDidChanged(mParent.get());
                  }

                  if(rep.hasAttribute(ResourceAttributes.Prop_joinedDevices)) {
                    mUpdatesDelegate.onZbJoinedDevicesDidChanged(mParent.get());
                  }

                  if(rep.hasAttribute(ResourceAttributes.Prop_joiningState)) {
                    if(state.getJoiningState() == JoiningState.kAllowed)
                      mUpdatesDelegate.OnZbJoiningStateDidChanged(mParent.get(), true);
                    else
                      mUpdatesDelegate.OnZbJoiningStateDidChanged(mParent.get(), false);
                  }

                  if(state.isCoordinatorStateChanged()) {
                    mUpdatesDelegate.onZbCoordinatorStateDidChanged(mParent.get());
                  }
                }
              }
            } catch (OcException e) {
              e.printStackTrace();
            }
           break;
         }
         Log.d(TAG,"***** IoTSysObserveOrigin update for "+type + "***** end");
    }

      @Override
      public void onFailed(IoTSysObserveOrigin type, String exception) {
        Log.w(TAG, "[IoTDevice->IoTSysObserver->onFailed] failed for " + type + " with exception \'"
            + exception + "\'");
        switch (type) {
          case IoTSysObserveOriginBluetooth:
            onObserveBluetoothFail(exception);
            break;
        }
      }

    @Override
    public void onRegistration(IoTSysObserveOrigin type) {
      Log.d(TAG,"***** Observer:"+type + " registered *****");
    }

    @Override
    public void deRegistration(IoTSysObserveOrigin type) {
      Log.d(TAG,"***** Observer:"+type + " deregistered *****");
      if(mParent.get().mClient.cancelNextObserver()) {

      }
    }
  }

  /**
     * <p>This method is called when {@link IoTSysObserver IoTSysObserver} receives an update of type
     * {@link IoTSysObserveOrigin#IoTSysObserveOriginBluetooth IoTSysObserveOriginBluetooth}.</p>
     * <p>This method updates the current Bluetooth states, dispatches the updates to the
     * {@link IoTSysUpdatesDelegate IoTSysUpdatesDelegate} delegate which has been set up with
     * {@link #setDelegate(IoTSysUpdatesDelegate) setDelegate(IoTSysUpdatesDelegate)} and acts for specific
     * behaviour related to Bluetooth updates.</p>
     *
     * @param rep The complementary information sent with the Bluetooth Update event.
     * @param canUpdate True if the changes should be dispatched to the {@link IoTSysUpdatesDelegate IoTSysUpdatesDelegate}.
     */
    private void onObserveBluetoothUpdate(OcRepresentation rep, boolean canUpdate) throws OcException {
      // update the states
      BluetoothAttr attributes = mState.getBluetoothState().update(rep);

      if (attributes == null) {
        Log.w(TAG, "[onObserveBluetoothUpdate] Unpacking values failed");
        return;
      }

      // getting the list of updated Bluetooth properties
      Set<String> properties = rep.getValues().keySet();
      if (properties.isEmpty()) {
        Log.w(TAG, "[onObserveBluetoothUpdate] no properties within the representation");
        return;
      }

      // dispatch event to listener
      if (canUpdate && mUpdatesDelegate != null) {
        broadcastBluetoothUpdates(properties, attributes);
      }
      else if (canUpdate /*&& mUpdatesDelegate == null*/) {
        // no delegate to receive the events
        Log.i(TAG, "[onObserveBluetoothUpdate] UpdatesDelegate is null, observations not dispatched.");
      }

      else /*if (!canUpdate)*/ {
        // device cannot update the delegate at the moment
        Log.i(TAG, "[onObserveBluetoothUpdate] observations not dispatched: device cannot update.");
      }
      // manage specific behaviours and errors
      handleBluetoothUpdates(properties, attributes, canUpdate);
    }

    /**
     * <p>This method is called when {@link IoTSysObserver IoTSysObserver} observes a request fail of type
     * {@link IoTSysObserveOrigin#IoTSysObserveOriginBluetooth IoTSysObserveOriginBluetooth}.</p>
     * <p>This method transfers the exception to the {@link IoTSysUpdatesDelegate IoTSysUpdatesDelegate} delegate
     * which has been set up with {@link #setDelegate(IoTSysUpdatesDelegate) setDelegate(IoTSysUpdatesDelegate)}.</p>
     * <p>The exception is transferred to the delegate through
     * {@link IoTSysUpdatesDelegate#btError(IoTDevice, IoTBluetoothError) btError()}.</p>
     *
     * @param exception
     *              Any complementary information about the issue.
     */
    private void onObserveBluetoothFail(String exception) {
      mUpdatesDelegate.btError(this, new IoTBluetoothError("", exception));
    }

    /**
     * <p>This method calls the event methods from the {@link IoTSysUpdatesDelegate IoTSysUpdatesDelegate} delegate
     * which has been set up with {@link #setDelegate(IoTSysUpdatesDelegate) setDelegate(IoTSysUpdatesDelegate)}
     * which correspond to the given <code>properties</code>. It builds the methods parameters from the given
     * <code>attributes</code>.</p>
     *
     * @param properties
     *      The list of properties which requires an update to be dispatched to an upper layer.
     * @param attributes
     *      The values to dispatch.
     */
    private void broadcastBluetoothUpdates(@NonNull Set<String> properties, BluetoothAttr attributes) {
      if (properties.contains(ResourceAttributes.Prop_adapterState)) {
        mUpdatesDelegate
            .btAdapterStateDidChange(this, matchBluetoothAdapterState(attributes.mAdapterState));
      }
      if (properties.contains(ResourceAttributes.Prop_discoverable)) {
        mUpdatesDelegate.btDiscoverableStateDidChange(this,
            matchBluetoothDiscoverableState(attributes.mDiscoverable));
      }
      if (properties.contains(ResourceAttributes.Prop_pairedDevices)) {
        mUpdatesDelegate.btPairedDevicesDidChange(this,
            buildPairedBluetoothDevicesList(attributes.mPairedDevices,
                getConnectedBluetoothDevice()));
      }
      if (properties.contains(ResourceAttributes.Prop_pairedState)) {
        BtDeviceStatusAttr deviceAttr = attributes.mPairedState;
        if (deviceAttr != null) {
          IoTBluetoothDevice device = new IoTBluetoothDevice(deviceAttr.mName, deviceAttr.mAddress);
          mUpdatesDelegate.btPairStateDidChange(this, device, deviceAttr.mStatus);
        }
      }
      if (properties.contains(ResourceAttributes.Prop_connectedState)) {
        BtDeviceStatusAttr statusAttr = attributes.mConnectedState;
        if (statusAttr != null) {
          mUpdatesDelegate
              .btConnectedDeviceHasChanged(this, new IoTBluetoothDevice(statusAttr.mName,
                  statusAttr.mAddress,
                  statusAttr.mStatus));
        }
      }
      if (properties.contains(ResourceAttributes.Prop_scanResult)) {
        BtDeviceAttr device = attributes.mScanResult;
        if (device != null) {
          mUpdatesDelegate.btScanDidDiscoverBtDevice(this, new IoTBluetoothDevice(device.mName, device.mAddress));
        }
      }
      if (properties.contains(ResourceAttributes.Prop_error)) {
        IoTBluetoothError error = buildBluetoothError(attributes.mError);
        mUpdatesDelegate.btError(this, error);
      }
      if (properties.contains(ResourceAttributes.Prop_signal)
          && attributes.mSignal.mName == Name.kScanStopped) {
        mUpdatesDelegate.btScanStateDidChange(this, false);
      }
    }

    /**
     * <p>This method analyses the Bluetooth updates and acts depending on them:
     * <ul>
     * <li>Adapter state update: requests the paired devices.</li>
     * <li>Paired state update: requests the paired devices.</li>
     * <li>Signal update: in the case of {@link BtSignalAttr.Name#kPairedListCleared
     * kPairedListCleared}, requests the paired devices.</li>
     * </ul></p>
     *
     * @param properties The list of properties which requires an update to be dispatched to an upper
     * layer.
     * @param attributes The values to dispatch.
     * @param canUpdate True if the delegate can be updated, false otherwise.
     */
    private void handleBluetoothUpdates(@NonNull Set<String> properties, BluetoothAttr attributes,
        boolean canUpdate) {
      if (properties.contains(ResourceAttributes.Prop_adapterState)) {
        if (getBluetoothAdapterState() == IoTBluetoothAdapterState.Enabled) {
          // the paired devices can only be retrieved if the Bluetooth is enabled
          Log.i(TAG, "[handleBluetoothUpdates] requesting paired devices after adapter state update");
          updateBluetoothPairedDevices(null);
          // **workaround** this seems to not be working as no list is dispatched, so this is requested for any
          // observation received related to Bluetooth.
        }
      }
      if (properties.contains(ResourceAttributes.Prop_pairedState)
          && getBluetoothAdapterState() == IoTBluetoothAdapterState.Enabled) {
        // a device has been paired or unpaired, the updated list of paired devices is requested
        Log.i(TAG, "[handleBluetoothUpdates] requesting paired devices after paired state update");
        updateBluetoothPairedDevices(null);
      }
      if (properties.contains(ResourceAttributes.Prop_error)) {
        IoTBluetoothError error = buildBluetoothError(attributes.mError);
        Log.w(TAG, "[handleBluetoothUpdates] Bluetooth error received for device " + error.getAddress() + ": "
            + error.getStatus());
        // **workaround** updating the states depending on the error are they seem to be outdated.
        onBluetoothError(error, canUpdate);
      }
      if (properties.contains(ResourceAttributes.Prop_signal)
          && attributes.mSignal.mName == BtSignalAttr.Name.kPairedListCleared
          && getBluetoothAdapterState() == IoTBluetoothAdapterState.Enabled) {
        Log.i(TAG, "[handleBluetoothUpdates] requesting paired devices after signal update");
        // the paired devices had been cleared -> the list needs to be updated
        updateBluetoothPairedDevices(null);
      }
    }

    /**
     * <p>This method analyses the Bluetooth error which had been received and act dependently.</p>
     *
     * @param error The error which has been received.
     * @param canUpdate True if the delegate can be updated, false otherwise.
     */
    private void onBluetoothError(IoTBluetoothError error, boolean canUpdate) {
      // get the status of the error
      BluetoothStatus status;
      try {
        status = BluetoothStatus.get(error.getStatus());
      } catch (Throwable throwable) {
        // the list of status is unofficial and can have some missing values
        Log.w(TAG, "[onBluetoothError] status is unknown");
        return;
      }

      OcRepresentation rep = null;

      switch (status) {
        case device_already_connected:
          // the connected state of the device seems outdated.
          Log.i(TAG,
              "[onBluetoothError] received error \'device_already_connected\': setting up the device has"
                  + " connected.");
          // **workaround** manual update of the connected state
          // -> The connected state is only triggered when a device is connected or disconnected.
          // -> It is not triggered by updateBluetoothState and does not have a dedicated method.
          rep = getBluetoothAttributesRepresentation(ResourceAttributes.Prop_connectedState, "",
              error.getAddress(), true);
          break;

        case device_not_connected:
          // the connected state of the device seems outdated.
          Log.i(TAG,
              "[onBluetoothError] received error \'device_not_connected\': setting up the device has"
                  + " disconnected.");
          // **workaround** manual update of the connected state
          // -> The connected state is only triggered when a device is connected or disconnected.
          // -> It is not triggered by updateBluetoothState and does not have a dedicated method.
          rep = getBluetoothAttributesRepresentation(ResourceAttributes.Prop_connectedState, "",
              error.getAddress(), false);
          break;

        case bt_not_enabled:
          // the adapter state of the device seems outdated.
          Log.i(TAG,
              "[onBluetoothError] received error \'bt_not_enabled\': requesting an update of the Adapter"
                  + " state.");
          updateBluetoothAdapterState(null);
          return;

        case set_discoverable_bt_not_enabled:
          // the discoverable state of the device seems outdated.
          Log.i(TAG,
              "[onBluetoothError] received error \'set_discoverable_bt_not_enabled\': requesting an "
                  + "update of the Bluetooth states.");
          updateBluetoothState();
          return;

        case exit_discoverable_bt_not_enabled:
          // the discoverable state of the device seems outdated.
          Log.i(TAG,
              "[onBluetoothError] received error \'exit_discoverable_bt_not_enabled\': requesting an "
                  + "update of the Bluetooth states.");
          updateBluetoothState();
          return;

        case device_already_paired:
          // the paired devices list seems outdated.
          Log.i(TAG, "[onBluetoothError] received error \'device_already_paired\': requesting an " +
              "update of the paired devices list.");
          if (getBluetoothAdapterState() == IoTBluetoothAdapterState.Enabled) {
            updateBluetoothPairedDevices(null);
          }
          break;

        case device_not_paired:
          // the paired devices list seems outdated.
          Log.i(TAG, "[onBluetoothError] received error \'device_not_paired\': requesting an " +
              "update of the paired devices list.");
          if (getBluetoothAdapterState() == IoTBluetoothAdapterState.Enabled) {
            updateBluetoothPairedDevices(null);
          }
          break;

        case device_not_in_interface_list:
        case bt_last_connected_peer_device_entry_missing:
        case bt_last_connected_peer_device_empty:
        case device_object_not_created:
        case command_not_supported:
        case invalid_address:
        case make_discoverable_invalid_parameter:
          break;
      }
      if (rep != null) {
        // **workaround** manual update of states provided through the errors
        try {
          onObserveBluetoothUpdate(rep, canUpdate);
        } catch (OcException e) {
          e.printStackTrace();
        }
      }
    }

    /**
     * <p>This method builds the OC representation which corresponds to the given property.</p>
     * <p>This method expects the following arguments:
     * <ul>
     * <li><code>Property</code> = {@link ResourceAttributes#Prop_connectedState
     * Prop_connectedState}<table>
     * <tr>
     * <td><code>values[0]</code></td>
     * <td>device name as a {@link String String}</td>
     * </tr>
     * <tr>
     * <td><code>values[1]</code></code></td>
     * <td>device bluetooth address as a {@link String String}</td>
     * </tr>
     * <tr>
     * <td><code>values[2]</code></code></td>
     * <td>device connection state as a {@link Boolean Boolean}</td>
     * </tr>
     * </table></li>
     * <li><code>Property</code> = {@link ResourceAttributes#Prop_adapterState
     * Prop_adapterState}<table>
     * <tr>
     * <td><code>values[0]</code></code></td>
     * <td>one of{@link BluetoothAttr.AdapterState AdapterState}</td>
     * </tr>
     * </table></li>
     * </ul></p>
     *
     * @param property The property to build an OcRepresentation for.
     * @param values THe values to add to the representation.
     * @return The OcRepresentation built with the given property and values.
     */
    @VisibleForTesting
    static OcRepresentation getBluetoothAttributesRepresentation(String property, Object... values) {
      OcRepresentation representation = new OcRepresentation();

      try {
        switch (property) {
          case ResourceAttributes.Prop_connectedState:
            if (values != null && values.length >= 3 && values[0] instanceof String
                & values[1] instanceof String & values[2] instanceof Boolean) {
              OcRepresentation btDeviceStatusAttr = new OcRepresentation();
              btDeviceStatusAttr.setValue(ResourceAttributes.Prop_name, (String) values[0]);
              btDeviceStatusAttr.setValue(ResourceAttributes.Prop_address, (String) values[1]);
              btDeviceStatusAttr.setValue(ResourceAttributes.Prop_status, (Boolean) values[2]);
              representation.setValue(ResourceAttributes.Prop_connectedState, btDeviceStatusAttr);
            }
            break;

          case ResourceAttributes.Prop_adapterState:
            if (values != null && values.length >= 1
                && values[0] instanceof BluetoothAttr.AdapterState) {
              BluetoothAttr.AdapterState state = (BluetoothAttr.AdapterState) values[0];
              String value = state == BluetoothAttr.AdapterState.kEnabled ? "enabled"
                  : state == BluetoothAttr.AdapterState.kDisabled ? "disabled"
                      : "unknown";
              representation.setValue(ResourceAttributes.Prop_adapterState, value);
            }
            break;
        }
      }
      catch (OcException e) {
        Log.w(TAG,
            "[getBluetoothAttributesRepresentation] Exception occurred for building OcRepresentation:\n"
                + e.getMessage());
      }
      return representation;
    }


  @Override // IoTRepository
  public List<? extends IoTRepository> getList() {
    List<IoTRepository> result = new ArrayList<>();
    // add paired devices
    // note: this should add the connected device instead, however at the moment the connected
    //       device is not populated. Instead the paired devices are used.
    if (isBluetoothAvailable() && getBluetoothAdapterState() == IoTBluetoothAdapterState.Enabled) {
      result.addAll(getPairedBluetoothDevices());
    }
    // add Zigbee devices
    if (isZigbeeAvailable() && isZigbeeEnabled()) {
      result.addAll(getZGJoinedDevices());
    }
    return result;
  }


    private List<IoTVoiceUIClient> getVoiceUiClients() {

      List<IoTVoiceUIClient> retList= new ArrayList<>();
      List<VoiceUIClientAttr> clients = mState.getVoiceUiState().getVoiceUIClients();
      for(VoiceUIClientAttr attr: clients) {
        IoTVoiceUIClientType type = getClientTypeForName(attr.mName);
        boolean onBoarded = false;
        boolean wakewordEnabled = false;
        VoiceUiState state = mState.getVoiceUiState();
        switch (type) {
          case IoTVoiceUIClientTypeAVS:
            onBoarded = state.isAVSOnboarded();
            wakewordEnabled = state.getWakewordStatus(AVSClientName);
            break;
          case IoTVoiceUIClientTypeCortana:
            wakewordEnabled = state.getWakewordStatus(VoiceUiState.CortanaClientName);
            break;
          case IoTVoiceUIClientTypeGVA:
            wakewordEnabled = state.getWakewordStatus(VoiceUiState.GVAClientName);
            break;
          case IoTVoiceUIClientTypeModular:
            onBoarded = true;
            wakewordEnabled = state.getWakewordStatus(VoiceUiState.ModularClientName);
            break;
          default:
            break;
        }
        retList.add(new IoTVoiceUIClient(attr.mName,true,onBoarded,wakewordEnabled));
      }
      return retList;
    }

    public IoTVoiceUIClient getDefaultVoiceUIClient()
    {
      List<IoTVoiceUIClient> clients = getVoiceUiClients();
      String defaultClientName = mState.getVoiceUiState().getDefaultVoiceUIClient();
      for (IoTVoiceUIClient client : clients) {
          if (client.name.equalsIgnoreCase(defaultClientName)) {
              return client;
          }
      }

      return new IoTVoiceUIClient("",false,false,false);
    }

    public String getDefaultVoiceUIClientFromPlayer() {
      return mState.getVoiceUiState().getDefaultVoiceUIClient();
    }


    private IoTVoiceUIClientType getClientTypeForName(String name) {

      IoTVoiceUIClientType type = IoTVoiceUIClientTypeUnknown;
      if (name.equalsIgnoreCase(AVSClientName)) {
         type = IoTVoiceUIClientType.IoTVoiceUIClientTypeAVS;
      } else if (name.equalsIgnoreCase(VoiceUiState.CortanaClientName)) {
         type = IoTVoiceUIClientType.IoTVoiceUIClientTypeCortana;
      } else if (name.equalsIgnoreCase(VoiceUiState.GVAClientName)) {
         type = IoTVoiceUIClientType.IoTVoiceUIClientTypeGVA;
      } else if (name.equalsIgnoreCase(VoiceUiState.ModularClientName)) {
         type = IoTVoiceUIClientType.IoTVoiceUIClientTypeModular;
      }
      return type;
    }

    public boolean enableVoiceUI(boolean enabled, IoTCompletionCallback callback) {
      try{
        VoiceUIAttr attr = mState.getVoiceUiState().getAttribute();
        attr.mEnableVoiceUI = enabled;
        mClient.postVoiceUI(attr, new VoiceUIListener(){

          @Override
          public void OnGetVoiceUICompleted(VoiceUIAttr attribute, boolean status) {
            callback.onCompletion(status);
          }

          @Override
          public void OnVoiceUICompleted(boolean status) {
            callback.onCompletion(status);
          }
        });
      } catch (OcException e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }

    public boolean enableWakeWord(String name, boolean wakeWord, IoTCompletionCallback callback) {
      //validate client name
      IoTVoiceUIClientType type = getClientTypeForName(name);
      if(type == IoTVoiceUIClientTypeUnknown) return false;

      try{
        SelectClientInAttr attr = new SelectClientInAttr();
        attr.mClient = name;
        attr.mWakewordStatus = wakeWord;
        mClient.postEnableVoiceUIClient(attr, success ->
          callback.onCompletion(success)
        );
      } catch (OcException e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }

    public void updateWithCompletion(final IoTCompletionCallback callback) {

        TaskExecutors.getExecutor().executeOnRequestExecutor(()-> {
          Log.d(TAG, "**** Setting up IoT Device initial state... ****");
          int numOfTasks = 0;

          List<Callable<Void>> taskList = new ArrayList<>();

          Callable<Void> task = () -> {
            mClient.getBluetoothWithCompletion((attribute, success) -> {
              if (success) {
                Log.d(TAG, "***** Get Bluetooth attributes success *****");
                if (attribute.mAdapterState != AdapterState.kUnknown) {
                  mState.getBluetoothState().setState(attribute);
                  mLatch.countDown();
                }
                else {
                  // received default values, attempting to get at least the adapter state
                  updateBluetoothAdapterState(success1 -> mLatch.countDown());
                }
              } else {
                Log.e(TAG, "***** Get Bluetooth attributes failed *****");
                mLatch.countDown();
              }
            });
            return null;
          };
          numOfTasks++;
          taskList.add(task);

          task = () -> {
            mClient.getNetworkWithCompletion((attribute, success) -> {
              if (success) {
                Log.d(TAG, "***** Get Network attributes success *****");
                mState.getNetworkState().update(attribute);
              } else {
                Log.e(TAG, "***** Get Network attributes failed *****");
              }
              mLatch.countDown();
            });
            return null;
          };
          numOfTasks++;
          taskList.add(task);

          task = () -> {
            mClient.getVoiceUIWithCompletion((attribute, success) -> {
              if (success) {
                Log.d(TAG, "***** Get Voice UI attribute success *****");
                mState.getVoiceUiState().update(attribute);
                Log.d(TAG, "AVS OnBoarding :" + isAvsOnBoarded());
              } else {
                Log.e(TAG, "***** Get Voice UI attribute failed *****");
              }
              mLatch.countDown();
            });
            return null;
          };
          numOfTasks++;
          taskList.add(task);

          task = () -> {
            mClient.getSystemWithCompletion((attribute, success) -> {
              if (success) {
                Log.d(TAG, "***** Get System attributes success *****");
                mState.getSystemState().update(attribute);

              } else {
                Log.e(TAG, "***** Get System attributes failed *****");
              }
              mLatch.countDown();
            });
            return null;
          };
          numOfTasks++;
          taskList.add(task);

          task = () -> {
            mClient.getZigbeeWithCompletion((attribute, success) -> {
              if (success) {
                Log.d(TAG, "***** Get Zigbee attributes success *****");
                mState.getZigbeeState().update(attribute);

              } else {
                Log.e(TAG, "***** Get Zigbee attributes failed *****");
              }
              mLatch.countDown();
            });
            return null;
          };
          numOfTasks++;
          taskList.add(task);

          mLatch = new CountDownLatch(numOfTasks);
          for (Callable<Void> call : taskList) {
            try {
              mExecutor.submit(call).get();
            } catch (ExecutionException |
                     InterruptedException |
                     RejectedExecutionException e) {
              e.printStackTrace();
              callback.onCompletion(false);
            }
          }

          try {
            mLatch.await(3000, TimeUnit.MILLISECONDS);

          } catch (InterruptedException e) {
            e.printStackTrace();
            callback.onCompletion(false);
            taskList.clear();
            return;
          }

          isFirstTimeFetchComplete.compareAndSet(false, true);

          taskList.clear();

          callback.onCompletion(true);
        });
    }

    public void startAvsOnBoarding(IoTCompletionCallback callback) {
      try {
        mClient.postStartAVSOnboardingWithCompletion(success -> callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
      }
    }

    public boolean removeAVSCredential(IoTCompletionCallback callback) {
      ClientInAttr attrIn = new ClientInAttr();
      attrIn.mClient = AVSClientName;
      return removeAVSCredential(attrIn, callback);
    }

    private boolean removeAVSCredential(ClientInAttr attrIn, IoTCompletionCallback callback) {
      try {
        Log.d(TAG,"Post request to remove AVS credential");
        mClient.postDeleteCredential(attrIn,success -> callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }

    public void rebootDevice(IoTCompletionCallback callback) {
      try {
        mClient.postRestartWithCompletion(success -> callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
      }
    }

    public void setDeviceName(String name, IoTCompletionCallback callback) {
      try {
        SystemAttr attr = mState.getSystemState().getData();
        if(name != null && !name.isEmpty() ) {
          attr.mDeviceFriendlyName = name;
          mClient.postSystem(attr, (attribute, success) -> callback.onCompletion(success));
        }
      } catch (OcException e) {
        e.printStackTrace();
      }
    }

    public VoiceUIAttr getVoiceSetting() {
      return mState.getVoiceUiState().getAttribute();
    }

    public boolean isOnboardingSupported() {
      return true;
    }


    // Bluetooth

    /**
     * <p>To know if the Bluetooth feature is available for this IoT Device.</p>
     *
     * @return True if the feature is available, false otherwise.
     */
    public boolean isBluetoothAvailable() {
      BluetoothState btState = mState.getBluetoothState();
      if (btState == null) {
        // no Bluetooth state to read
        return false;
      }
      return btState.isAvailable();
    }

    /**
     * <p>To get the IoT Device Bluetooth Adapter state. That state determines if the Bluetooth of the
     * device is
     * enabled or disabled.</p>
     *
     * @return the latest reported state of the IoT Device Bluetooth Adapter.
     */
    public IoTBluetoothAdapterState getBluetoothAdapterState() {
      BluetoothState btState = mState.getBluetoothState();
      if (btState == null) {
        // no Bluetooth state to read
        Log.w(TAG, "[getBluetoothAdapterState] No Bluetooth state, returning Unknown");
        return IoTBluetoothAdapterState.Unknown;
      }

      BluetoothAttr.AdapterState state = btState.getBluetoothAdapterState();
      if (state == null) {
        // no adapter state to read
        Log.w(TAG, "[getBluetoothAdapterState] No Bluetooth Adapter state, returning Unknown");
        return IoTBluetoothAdapterState.Unknown;
      }

      // matching the state with the controller enumeration
      return matchBluetoothAdapterState(state);
    }

    /**
     * <p>To get the discoverable state of the device.</p>
     * <p>This method takes the state known by the system and determines if the device
     * discoverable.</p>
     *
     * @return The matched state from {@link IoTBluetoothDiscoverableState
     * IoTBluetoothDiscoverableState}.
     */
    public IoTBluetoothDiscoverableState getBluetoothDiscoverableState() {
      BluetoothState btState = mState.getBluetoothState();
      if (btState == null) {
        // no Bluetooth state to read
        Log.w(TAG, "[getBluetoothDiscoverableState] No Bluetooth state, returning Unknown");
        return IoTBluetoothDiscoverableState.Unknown;
      }

      BluetoothAttr.Discoverable state = btState.getDiscoverableState();
      if (state == null) {
        // no adapter state to read
        Log.w(TAG, "[getBluetoothDiscoverableState] No Discoverable state, returning Unknown");
        return IoTBluetoothDiscoverableState.Unknown;
      }

      return matchBluetoothDiscoverableState(state);
    }

    /**
     * <p>To get the device connected over Bluetooth with this IoT device.</p>
     * <p>This method creates the {@link IoTBluetoothDevice IoTBluetoothDevice} based on the
     * {@link BtDeviceStatusAttr BtDeviceStatusAttr} connected device from the system.</p>
     *
     * @return The device which is connected. If no device is connected, this method returns null.
     */
    public IoTBluetoothDevice getConnectedBluetoothDevice() {
      BtDeviceStatusAttr device = mState.getBluetoothState().getConnectedDevice();
      if (!device.mStatus) {
        // no connected device
        return null;
      }

      return new IoTBluetoothDevice(device.mName, device.mAddress, true);
    }

    /**
     * <p>To get the list of devices which are paired over Bluetooth with the IoT device.</p>
     * <p>This method builds a list of {@link IoTBluetoothDevice IoTBluetoothDevice} based on the list
     * of paired
     * devices of type {@link BtDeviceAttr BtDeviceAttr} from the system.</p>
     *
     * @return The list of {@link IoTBluetoothDevice IoTBluetoothDevice} which are paired with this
     * IoT device.
     */
    public List<IoTBluetoothDevice> getPairedBluetoothDevices() {
      return buildPairedBluetoothDevicesList(mState.getBluetoothState().getPairedDevices(),
          getConnectedBluetoothDevice());
    }

    /**
     * <p>To update all the Bluetooth states of this IoT device.</p>
     * <p>If this method started the process successfully, the results are dispatched through updates
     * of the different
     * Bluetooth states using the {@link IoTSysUpdatesDelegate IoTSysUpdatesDelegate} which has been
     * set with {@link #setDelegate(IoTSysUpdatesDelegate) setDelegate(IoTSysUpdatesDelegate)}. </p>
     *
     * @return {@link IoTError#NONE IoTError.NONE} if no error occurs when starting the process.
     */
    public IoTError updateBluetoothState() {
      try {
        mClient.getBluetoothWithCompletion((attribute, success) -> {
          if (success) {
            mState.getBluetoothState().setState(attribute);
          } else {
            Log.w(TAG, "[updateBluetoothStates->BluetoothListener] updating Bluetooth states failed");
          }
        });
      }
      catch (OcException exception) {
        Log.w(TAG, "[updateBluetoothStates] exception occurred: " + exception.getMessage());
        return IoTError.UNKNOWN;
      }

      return IoTError.NONE;
    }

    /**
     * <p>To enable or the Bluetooth Adapter on the IoT Device. When the Bluetooth adapter is enabled
     * the Bluetooth
     * can be used, otherwise it cannot.</p>
     * <p>If this call is successful, the IoT Device should notify the new state of its Bluetooth
     * Adapter and
     * {@link IoTSysUpdatesDelegate#btAdapterStateDidChange(IoTDevice, IoTBluetoothAdapterState)
     * btAdapterStateDidChange(IoTDevice, IoTBluetoothAdapterState)} would be called.</p>
     *
     * @param enabled True to enable the Bluetooth Adapter, false otherwise.
     * @param callback The callback to be notified when the request has completed.
     * @return True if this could be initiated, false otherwise.
     */
    public boolean enableBluetoothAdapter(boolean enabled, IoTCompletionCallback callback) {
      try {
        EnableBtAdapterInAttr attributes = new EnableBtAdapterInAttr();
        attributes.mAdapterState = enabled;
        return mClient.postEnableBtAdapter(attributes, success -> {
          if (callback != null) {
            callback.onCompletion(success);
          }
        });
      }
      catch (OcException e) {
        Log.w(TAG, "[enableBluetoothAdapter] request failed with exception\n" + e.getMessage());
        if (callback != null) {
          callback.onCompletion(false);
        }
        return false;
      }
    }

    /**
     * <p>To put the IoT device in discoverable mode making other Bluetooth devices to see this
     * device<.</p>
     * <p>If this call is successful, the IoT Device should notify the new discoverable state and
     * {@link IoTSysUpdatesDelegate#btDiscoverableStateDidChange(IoTDevice,
     * IoTBluetoothDiscoverableState) btDiscoverableStateDidChange(IoTDevice,
     * IoTBluetoothDiscoverableState)} would be called.</p>
     *
     * @param timeout The time before to stop the discoverable mode in <b>seconds</b>.
     * @param callback The callback to be notified when the request has completed.
     * @return True if this could be initiated, false otherwise.
     */
    public boolean startBluetoothDiscoveryMode(int timeout, IoTCompletionCallback callback) {
      try {
        TimeoutSecInAttr attributes = new TimeoutSecInAttr();
        attributes.mTimeoutSecs = timeout;
        return mClient.postEnterBtDiscoveryMode(attributes, success -> {
          if (callback != null) {
            callback.onCompletion(success);
          }
        });
      }
      catch (OcException e) {
        Log.w(TAG, "[startBluetoothDiscoveryMode] request failed with exception\n" + e.getMessage());
        if (callback != null) {
          callback.onCompletion(false);
        }
        return false;
      }
    }

    /**
     * <p>To stop the discoverability of the IoT device if it was in discoverable mode.</p>
     * <p>If this call is successful, the IoT Device should notify the new discoverable state and
     * {@link IoTSysUpdatesDelegate#btDiscoverableStateDidChange(IoTDevice,
     * IoTBluetoothDiscoverableState) btDiscoverableStateDidChange(IoTDevice,
     * IoTBluetoothDiscoverableState)} would be called.</p>
     *
     * @param callback The callback to be notified when the request has completed.
     *
     * @return True if this could be initiated, false otherwise.
     */
    public boolean stopBluetoothDiscoveryMode(IoTCompletionCallback callback) {
      try {
        return mClient.postCancelBtDiscoveryModeWithCompletion(success -> {
          if (callback != null) {
            callback.onCompletion(success);
          }
        });
      }
      catch (OcException e) {
        Log.w(TAG, "[startBluetoothDiscoveryMode] request failed with exception\n" + e.getMessage());
        return false;
      }
    }

    /**
     * <p>To ask the IoT Device to start to scan for other Bluetooth devices.</p>
     * <p>The IoT device does not notify its scan state, it only informs on devices it has found
     * while scanning and once the scan has stopped.</p>
     * <p>Once the scan is enabled, the IoTDevice will send the Bluetooth devices it has found and
     * {@link IoTSysUpdatesDelegate#btScanDidDiscoverBtDevice(IoTDevice device, IoTBluetoothDevice
     * scanResult) btScanDidDiscoverBtDevice()} would be called.</p>
     * <p>Once the scan has stopped, the IoT Device notifies the change to this controller and
     * {@link IoTSysUpdatesDelegate#btScanStateDidChange(IoTDevice, boolean)
     * btScanStateDidChange(IoTDevice, false)} would be called.</p>
     *
     * @param callback The callback to be notified when the request has completed.
     *
     * @return True if this could be initiated, false otherwise.
     */
    public boolean startBluetoothScan(IoTCompletionCallback callback) {
      try {
        return mClient.postStartBtScanWithCompletion(success -> {
          if (callback != null) {
            callback.onCompletion(success);
          }
        });
      }
      catch (OcException e) {
        Log.w(TAG, "[startBluetoothScan] request failed with exception\n" + e.getMessage());
        return false;
      }
    }

    /**
     * <p>To ask the IoT Device to stop to scan for other Bluetooth devices.</p>
     * <p>If this call is successful, the IoT Device should notify the new scan state and
     * {@link IoTSysUpdatesDelegate#btScanStateDidChange(IoTDevice, boolean)
     * btScanStateDidChange(IoTDevice, boolean)} would be called.</p>
     *
     * @param callback The callback to be notified when the request has completed.
     *
     * @return True if this could be initiated, false otherwise.
     */
    public boolean stopBluetoothScan(IoTCompletionCallback callback) {
      try {
        return mClient.postStopBtScanWithCompletion(success -> {
          if (callback != null) {
            callback.onCompletion(success);
          }
        });
      }
      catch (OcException e) {
        Log.w(TAG, "[startBluetoothScan] request failed with exception\n" + e.getMessage());
        return false;
      }
    }

    /**
     * <p>To ask the IoT Device to connect with the given Bluetooth device.</p>
     * <p>If this call is successful, the IoT Device should notify the connection state and
     * {@link IoTSysUpdatesDelegate#btConnectedDeviceHasChanged(IoTDevice, IoTBluetoothDevice)
     * btConnectedDeviceHasChanged} would be called.</p>
     * <p><i>Note</i>: only one device can be connected at time. Disconnect any connected device
     * before to call this method.</p>
     *
     * @param device The Bluetooth device to connect with.
     * @param callback The callback to be notified when the request has completed.
     * @return True if this could be initiated, false otherwise.
     */
    public boolean connectBluetoothDevice(IoTBluetoothDevice device, IoTCompletionCallback callback) {
      if (device == null) {
        Log.w(TAG, "[connectBluetoothDevice] device is null.");
        return false;
      }

      try {
        AddressInAttr attributes = new AddressInAttr();
        attributes.mAddress = device.getAddress();
        return mClient.postConnectBtDevice(attributes, status -> {
          if (callback != null) {
            callback.onCompletion(status);
          }
        });
      }
      catch (OcException e) {
        Log.w(TAG, "[connectBluetoothDevice] request failed with exception:\n" + e.getMessage());
        return false;
      }
    }

    /**
     * <p>To ask the IoT Device to disconnect from the given Bluetooth device.</p>
     * <p>If this call is successful, the IoT Device should notify the connection state and
     * {@link IoTSysUpdatesDelegate#btConnectedDeviceHasChanged(IoTDevice, IoTBluetoothDevice)
     * btConnectedDeviceHasChanged} would be called.</p>
     *
     * @param device The Bluetooth device to disconnect from.
     * @param callback The callback to be notified when the request has completed.
     * @return True if this could be initiated, false otherwise.
     */
    public boolean disconnectBluetoothDevice(IoTBluetoothDevice device,
        IoTCompletionCallback callback) {
      if (device == null) {
        Log.w(TAG, "[disconnectBluetoothDevice] device is null.");
        return false;
      }

      try {
        AddressInAttr attributes = new AddressInAttr();
        attributes.mAddress = device.getAddress();
        return mClient.postDisconnectBtDevice(attributes, status -> {
          if (callback != null) {
            callback.onCompletion(status);
          }
        });
      }
      catch (OcException e) {
        Log.w(TAG, "[disconnectBluetoothDevice] request failed with exception:\n" + e.getMessage());
        return false;
      }
    }

    /**
     * <p>To ask the IoT Device to clear all its pairing information.</p>
     * <p>If this call is successful, the IoT Device should notify the new list of paired devices
     * (empty) and
     * {@link IoTSysUpdatesDelegate#btPairedDevicesDidChange(IoTDevice, List)
     * btPairedDevicesDidChange(IoTDevice, List)} would be called.</p>
     *
     * @param callback The callback to be notified when the request has completed.
     *
     * @return True if this could be initiated, false otherwise.
     */
    public boolean clearPairedBluetoothDevices(IoTCompletionCallback callback) {
      try {
        return mClient.postClearBtPairedListWithCompletion(status -> {
          if (!status) {
            Log.w(TAG, "[postClearBtPairedListWithCompletion->ClearBtPairedListListener] " +
                "clearing the paired devices list from the IoT device failed");
          }
          if (callback != null) {
            callback.onCompletion(status);
          }
        });
      }
      catch (Exception exception) {
        Log.w(TAG, "[updateBluetoothPairedDevices] exception occurred: " + exception.getMessage());
        return false;
      }
    }

    /**
     * <p>To ask the IoT Device to pair with the given Bluetooth device.</p>
     * <p>If this call is successful, the IoT Device should notify the pairing result and
     * {@link IoTSysUpdatesDelegate#btConnectedDeviceHasChanged(IoTDevice, IoTBluetoothDevice)
     * btConnectedDeviceHasChanged} would be called.</p>
     *
     * @param device The Bluetooth device to pair with.
     * @param callback The callback to be notified when the request has completed.
     *
     * @return True if this could be initiated, false otherwise.
     */
    public boolean pairBluetoothDevice(IoTBluetoothDevice device, IoTCompletionCallback callback) {
      if (device == null) {
        Log.w(TAG, "[pairBluetoothDevice] device is null.");
        return false;
      }

      try {
        AddressInAttr attributes = new AddressInAttr();
        attributes.mAddress = device.getAddress();

        return mClient.postPairBtDevice(attributes, status -> {
          if (!status) {
            Log.w(TAG, "[pairBluetoothDevice->PairBtDeviceListener] pairing the device failed.");
          }
          if (callback != null) {
            callback.onCompletion(status);
          }
        });
      }
      catch (Exception exception) {
        Log.w(TAG, "[updateBluetoothPairedDevices] exception occurred: " + exception.getMessage());
        return false;
      }
    }

    /**
     * <p>To ask the IoT Device to unpair from the given Bluetooth device.</p>
     * <p>If this call is successful, the IoT Device should notify the unpairing result and get the
     * new list of
     * paired devices resulting in {@link IoTSysUpdatesDelegate#btPairedDevicesDidChange(IoTDevice,
     * List) btPairedDevicesDidChange(IoTDevice, List)} to be called.</p>
     *
     * @param device The Bluetooth device to unpair from.
     * @param callback The callback to be notified when the request has completed.
     * @return True if this could be initiated, false otherwise.
     */
    public boolean unpairBluetoothDevice(IoTBluetoothDevice device, IoTCompletionCallback callback) {
      if (device == null) {
        Log.w(TAG, "[pairBluetoothDevice] device is null.");
        return false;
      }

      try {
        AddressInAttr attributes = new AddressInAttr();
        attributes.mAddress = device.getAddress();

        return mClient.postUnpairBtDevice(attributes, status -> {
          if (!status) {
            Log.w(TAG, "[pairBluetoothDevice->PairBtDeviceListener] pairing the device failed.");
          }
          if (callback != null) {
            callback.onCompletion(status);
          }
        });
      }
      catch (Exception exception) {
        Log.w(TAG, "[updateBluetoothPairedDevices] exception occurred: " + exception.getMessage());
        return false;
      }
    }

    /**
     * <p>To update all the Bluetooth adapter state of this IoT device.</p>
     * <p>If this method started the process successfully, the result should be dispatched with an
     * update of the
     * Bluetooth Adapter state through the method {@link IoTSysUpdatesDelegate#btAdapterStateDidChange(IoTDevice,
     * IoTBluetoothAdapterState) btAdapterStateDidChange} of the delegate which has been set with
     * {@link #setDelegate(IoTSysUpdatesDelegate) setDelegate(IoTSysUpdatesDelegate)}. </p>
     *
     * @param callback The callback to be notified when the request has completed.
     * @return {@link IoTError#NONE IoTError.NONE} if no error occurs when starting the process.
     */
    public IoTError updateBluetoothAdapterState(IoTCompletionCallback callback) {
      try {
        mClient.postGetBtAdapterStateWithCompletion(status -> {
          if (!status) {
            Log.w(TAG, "[updateBluetoothAdapterState->GetBtAdapterStateListener] " +
                "updating Bluetooth adapter state from the IoT device failed");
          }
          if (callback != null) {
            callback.onCompletion(status);
          }
        });
      }
      catch (OcException exception) {
        Log.w(TAG, "[updateBluetoothAdapterState] exception occurred: "
            + exception.getMessage());
        return IoTError.UNKNOWN;
      }

      return IoTError.NONE;
    }

    /**
     * <p>To update the list of devices paired with the IoT Device.</p>
     * <p>If this method started the process successfully, the result should be dispatched with an
     * update of the
     * paired devices list through the method {@link IoTSysUpdatesDelegate#btPairedDevicesDidChange(IoTDevice,
     * List) btPairedDevicesDidChange} of the delegate which has been set with {@link
     * #setDelegate(IoTSysUpdatesDelegate) setDelegate(IoTSysUpdatesDelegate)}. </p>
     *
     * @param callback The callback to be notified when the request has completed.
     * @return {@link IoTError#NONE IoTError.NONE} if no error occurs when starting the process.
     */
    public IoTError updateBluetoothPairedDevices(IoTCompletionCallback callback) {
      try {
        mClient.postGetBtPairedDevicesWithCompletion(status -> {
          if (!status) {
            Log.w(TAG, "[updateBluetoothPairedDevices->GetBtAdapterStateListener] " +
                "getting the paired devices list from the IoT device failed");
          }
          if (callback != null) {
            callback.onCompletion(status);
          }
        });
      }
      catch (Exception exception) {
        Log.w(TAG, "[updateBluetoothPairedDevices] exception occurred: " + exception.getMessage());
        return IoTError.UNKNOWN;
      }

      return IoTError.NONE;
    }

    /**
     * <p>This method matches the given IoTSys {@link BluetoothAttr.AdapterState AdapterState} state
     * with its
     * equivalent {@link IoTBluetoothAdapterState IoTBluetoothAdapterState} state.</p>
     *
     * @param state The state to get the {@link IoTBluetoothAdapterState IoTBluetoothAdapterState}
     * state for.
     * @return The matching {@link IoTBluetoothAdapterState IoTBluetoothAdapterState} state.
     */
    private static IoTBluetoothAdapterState matchBluetoothAdapterState(
        @NonNull BluetoothAttr.AdapterState state) {
      switch (state) {
        case kDisabled:
          return IoTBluetoothAdapterState.Disabled;
        case kEnabled:
          return IoTBluetoothAdapterState.Enabled;
        case kUnknown:
        default:
          return IoTBluetoothAdapterState.Unknown;
      }
    }

    /**
     * <p>This method matches the given IoTSys {@link BluetoothAttr.Discoverable Discoverable} state
     * with its
     * equivalent {@link IoTBluetoothDiscoverableState IoTBluetoothDiscoverableState} state.</p>
     *
     * @param state The state to get the {@link IoTBluetoothDiscoverableState
     * IoTBluetoothDiscoverableState} state for.
     * @return The matching {@link IoTBluetoothAdapterState IoTBluetoothAdapterState} state.
     */
    private static IoTBluetoothDiscoverableState
    matchBluetoothDiscoverableState(@NonNull BluetoothAttr.Discoverable state) {
      switch (state) {
        case kDiscoverable:
          return IoTBluetoothDiscoverableState.Discoverable;
        case kNonDiscoverable:
          return IoTBluetoothDiscoverableState.NonDiscoverable;
        case kUnknown:
        default:
          return IoTBluetoothDiscoverableState.Unknown;
      }
    }

    /**
     * <p>This method builds a new {@link IoTBluetoothError IoTBluetoothError} from the given
     * {@link BtErrorAttr BtErrorAttr}.</p>
     *
     * @param error The error to get the equivalent {@link IoTBluetoothError IoTBluetoothError} from.
     * @return A new instance of {@link IoTBluetoothError IoTBluetoothError} set up with the values of
     * the given parameter.
     */
    private static IoTBluetoothError buildBluetoothError(@NonNull BtErrorAttr error) {
      return new IoTBluetoothError(error.mAddress, error.mStatus);
    }

    /**
     * <p>This method builds a new list of {@link IoTBluetoothDevice IoTBluetoothDevice} from the
     * given list of
     * {@link BtDeviceAttr BtDeviceAttr}.</p>
     *
     * @param devices The list of {@link BtDeviceAttr BtDeviceAttr} to build their equivalent list of
     * {@link IoTBluetoothDevice IoTBluetoothDevice}.
     * @return The built list of {@link IoTBluetoothDevice IoTBluetoothDevice}.
     */
    private static List<IoTBluetoothDevice> buildPairedBluetoothDevicesList(
        List<BtDeviceAttr> devices,
        IoTBluetoothDevice connectedDevice) {
      List<IoTBluetoothDevice> result = new ArrayList<>();
      for (BtDeviceAttr device : devices) {
        if (device.mAddress != null && device.mAddress.length() > 0) {
          IoTBluetoothDevice newDevice = new IoTBluetoothDevice(device.mName,
              device.mAddress);
          newDevice.setConnected(newDevice.equals(connectedDevice));
          result.add(newDevice);
        }
      }

      return result;
    }


    public boolean setZbAdapterEnabled(boolean enabled, IoTCompletionCallback callback) {
      try {
        Log.d(TAG,String.format("Set Zb AdapterEnabled [%s]: %b", mClient.getHostName(), enabled));
        EnableZbAdapterInAttr attr = new EnableZbAdapterInAttr();
        attr.mAdapterState = enabled;
        mClient.postEnableZbAdapter(attr, success -> callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
        return false;
      }

      return true;
    }

    public CoordinatorState getCoordinatorState() {
      return mState.getZigbeeState().getCoordinatorState();
    }

    public JoiningState getJoinZbJoiningState() {
      return mState.getZigbeeState().getJoiningState();
    }

    public boolean startFormZbNetworkWithCompletion(IoTCompletionCallback callback) {
      CoordinatorState coState = mState.getZigbeeState().getCoordinatorState();
      if(coState == CoordinatorState.kNominated) {
        return false;
      }

      try {
        return mClient.postFormZbNetworkWithCompletion(success -> callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
      }
      return false;
    }

    public boolean startZbJoiningWithTimeout(int timeout,IoTCompletionCallback callback ) {
      CoordinatorState coState = mState.getZigbeeState().getCoordinatorState();
      if(coState != CoordinatorState.kNominated) return false;
      try {
        TimeoutSecInAttr attr = new TimeoutSecInAttr();
        attr.mTimeoutSecs = timeout;
        return mClient.postStartZbJoining(attr,success -> callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
      }
      return false;
    }

    public void startSetZbDeviceName(String name, int id, IoTCompletionCallback callback) {
      CoordinatorState coState = mState.getZigbeeState().getCoordinatorState();
      if(coState != CoordinatorState.kNominated) return;
      try {
        SetZbFriendlyNameInAttr attr = new SetZbFriendlyNameInAttr();
        attr.mFriendlyName = name;
        attr.mDeviceIdentifier = id;
        mClient.postSetZbFriendlyName(attr,success -> callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
      }
    }

    public IoTError checkForNewFirmware() {
      return IoTError.NONE;
    }

    public IoTError updateFirmware() {
      return IoTError.NONE;
    }

    public UpdateStatus getUpdateStatus() {
      return UpdateStatus.NONE;
    }

    public boolean haveNewFirmware() {
      return false;
    }

    public boolean isPhysicalRebootRequired() {
      return false;
    }

    public OnboardingError getLastOnboardingError() {
      return null;
    }

    public List<ScanInfo> getScanInfoList() {
      return null;
    }

    public List<ScanInfo> getWifiScanList() {
      return null;
    }

    public IoTError updateScanInfoList() {
      return IoTError.NONE;
    }

    public NetworkInterface getNetworkInterface() {
      return NetworkInterface.WIFI;
    }


    public IoTError disconnect() {
      return IoTError.NONE;
    }

    public IoTError connect() {
      return IoTError.NONE;
    }


    public boolean hasValidConnection() {
      return false;
    }

    public boolean isOnboarded() {
      return false;
    }

    public String getSoftAPDisplayName() {
      return "";
    }

    public String getWifiSSID() {
      return mWifiSSID;
    }

    public IoTError setPassword(String password) {
      return IoTError.NONE;
    }

    public IoTError updateNetworkInfo() {
      return IoTError.NONE;
    }

    public IoTError factoryReset() {
      return IoTError.NONE;
    }

    public IoTError reboot() {
      return IoTError.NONE;
    }

    public IoTError onboard(ScanInfo scanInfo, String password) {
    return IoTError.NONE;
  }

    public int getFirmwareUpdateProgress() {
      return 0;
    }


}
