/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.iotsys;


import com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.BluetoothState;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.NetworkState;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.SystemState;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.VoiceUiState;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.ZigbeeState;

public class IoTSysStates {

  private final VoiceUiState mVoiceUiState = new VoiceUiState();
  private final SystemState mSystemState = new SystemState();
  private final NetworkState mNetworkState = new NetworkState();
  private final ZigbeeState mZigbeeState = new ZigbeeState();
  private final BluetoothState mBluetoothState = new BluetoothState();


  public BluetoothState getBluetoothState() {
    return mBluetoothState;
  }

  public NetworkState getNetworkState() {
    return mNetworkState;
  }

  public SystemState getSystemState() {
     return mSystemState;
  }

  public VoiceUiState getVoiceUiState() {
      return mVoiceUiState;
  }

  public ZigbeeState getZigbeeState() {
      return mZigbeeState;
  }

  public IoTSysStates() {
  }


}
