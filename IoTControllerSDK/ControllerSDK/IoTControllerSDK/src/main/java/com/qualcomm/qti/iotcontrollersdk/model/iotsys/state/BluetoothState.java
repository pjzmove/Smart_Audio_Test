/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.iotsys.state;

import static android.content.ContentValues.TAG;

import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BluetoothAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BtDeviceAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BtDeviceStatusAttr;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.ResourceState;
import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

/**
   * <p>This class keeps information on the Bluetooth states of a device.</p>
   */
public class BluetoothState extends ResourceState {

  private final BluetoothAttr mBluetoothAttr = new BluetoothAttr();

  /**
   * <p>To build a new instance of the Bluetooth State of an IoT Device.</p>
   *
   */
  public BluetoothState() {
    isAvailable = false;
  }

  /**
   * <p>This method sets up the Bluetooth states with the given attributes.</p>
   *
   * @param attr The Bluetooth attributes to set up the states with.
   */
  public synchronized void setState(BluetoothAttr attr) {
    isAvailable = true;
    GenericStateApi.setState(mBluetoothAttr, attr);
  }

  /**
   * <p>This method updates the Bluetooth states with the values of the given IoTivity
   * representation.</p>
   *
   * @param rep The Bluetooth attributes to update the states with.
   * @return A copy of the updated Bluetooth attributes.
   */
  public synchronized BluetoothAttr update(OcRepresentation rep) throws OcException {
    if (!GenericStateApi.updateState(mBluetoothAttr, rep)) {
        Log.w(TAG, "[BluetoothState->update] attributes couldn't be unpacked.");
        return null;
      }
     return GenericStateApi.getState(mBluetoothAttr);
  }

  /**
     * <p>To get the device connected with the IoT device. This also gives the current connection state.</p>
     * <p>This method uses a lock to get the state.</p>
     *
     * @return The Bluetooth device the device is/was connected with and their current connection state.
     */
    public synchronized BtDeviceStatusAttr getConnectedDevice() {
       return GenericStateApi.getState(mBluetoothAttr.mConnectedState);
    }

    /**
     * <p>To get the list of devices which are paired with the device.</p>
     *
     * @return The list of paired devices.
     */
    public synchronized List<BtDeviceAttr> getPairedDevices() {
        return GenericStateApi.getState(mBluetoothAttr.mPairedDevices);
    }

    /**
     * <p>To get the state of the Bluetooth Adapter. This determines if the Bluetooth feature is on
     * or off.</p>
     *
     * @return the latest reported state of the Bluetooth Adapter.
     */
    public synchronized BluetoothAttr.AdapterState getBluetoothAdapterState() {
       return GenericStateApi.getState(mBluetoothAttr.mAdapterState);
    }

    /**
     * <p>To get the discoverable state of the device. This determines if the device can be
     * discovered by other
     * devices.</p>
     *
     * @return the latest reported discoverable state.
     */
    public synchronized BluetoothAttr.Discoverable getDiscoverableState() {
       return GenericStateApi.getState(mBluetoothAttr.mDiscoverable);
    }


}
