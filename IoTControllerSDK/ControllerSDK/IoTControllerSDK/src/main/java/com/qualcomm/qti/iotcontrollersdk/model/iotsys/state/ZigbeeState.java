/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.iotsys.state;

import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZbDeviceAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZbSignalAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZbSignalAttr.SignalName;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.AdapterState;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.CoordinatorState;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.JoiningState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.ResourceState;
import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class ZigbeeState extends ResourceState {

  private final ZigbeeAttr mZigbeeAttr = new ZigbeeAttr();
  private boolean  isCoordinatorStateChanged = false;

  public enum ZigbeeDeviceType {
    IoTZigbeeDeviceTypeUnknown,
    IoTZigbeeDeviceTypeLight,
    IoTZigbeeDeviceTypeThermostat
  }

  public ZigbeeState() {
    isAvailable = false;
    mZigbeeAttr.mCoordinatorState = CoordinatorState.kUnknown;
    mZigbeeAttr.mAdapterState = AdapterState.kUnknown;
    isCoordinatorStateChanged = false;
  }

  public synchronized void update(ZigbeeAttr attr) {
    isAvailable = true;
    GenericStateApi.setState(mZigbeeAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
    isAvailable = true;
    OcRepresentation[] reps;
    boolean didUnpack = false;
    isCoordinatorStateChanged = false;

    if(rep == null) return didUnpack;

    if (rep.hasAttribute(ResourceAttributes.Prop_adapterState)) {
        didUnpack = true;
        mZigbeeAttr.mAdapterState = mZigbeeAttr.adapterStateFromString(
        ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_adapterState));
        if(mZigbeeAttr.mAdapterState == AdapterState.kDisabled) {
          mZigbeeAttr.mCoordinatorState = CoordinatorState.kNotNominated;
        }
    }

    if (rep.hasAttribute(ResourceAttributes.Prop_coordinatorState)) {
        didUnpack = true;
        CoordinatorState state = mZigbeeAttr.coordinatorStateFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_coordinatorState));
        if(state != CoordinatorState.kUnknown)
          mZigbeeAttr.mCoordinatorState = state;
    }

    if (rep.hasAttribute(ResourceAttributes.Prop_error)) {
        didUnpack = true;
        mZigbeeAttr.mError = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_error);
    }

    if (rep.hasAttribute(ResourceAttributes.Prop_joinedDevices)) {
        didUnpack = true;
        reps = rep.getValue(ResourceAttributes.Prop_joinedDevices);
        if(reps != null) {
            mZigbeeAttr.mJoinedDevices.clear();
            for (OcRepresentation elem_rep: reps) {
                ZbDeviceAttr obj = new ZbDeviceAttr();
                if(obj.unpack(elem_rep))
                    mZigbeeAttr.mJoinedDevices.add(obj);
            }

            if(reps.length > 0 && mZigbeeAttr.mCoordinatorState != CoordinatorState.kNominated) {
              //mZigbeeAttr.mCoordinatorState = CoordinatorState.kNominated;
              //isCoordinatorStateChanged = true;
            }
        }
    }

    if (rep.hasAttribute(ResourceAttributes.Prop_joiningState)) {
        didUnpack = true;
        mZigbeeAttr.mJoiningState = mZigbeeAttr.joiningStateFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_joiningState));
    }

    if (rep.hasAttribute(ResourceAttributes.Prop_signal)) {
        didUnpack = true;
        ZbSignalAttr signalAttr = new ZbSignalAttr();
        if(signalAttr.unpack(rep.getValue(ResourceAttributes.Prop_signal))) {
          if(signalAttr.mSignalName == SignalName.kNetworkFormed) {
            mZigbeeAttr.mCoordinatorState = CoordinatorState.kNominated;
            isCoordinatorStateChanged = true;
            mZigbeeAttr.mSignal = signalAttr;
          }

        }
    }
    return didUnpack;
  }

  public synchronized ZigbeeAttr getAttribute() {
     return GenericStateApi.getState(mZigbeeAttr);
  }

  public synchronized AdapterState getAdapterState() {
     return mZigbeeAttr.mAdapterState;
  }

  public synchronized CoordinatorState getCoordinatorState() {
     return mZigbeeAttr.mCoordinatorState;
  }

  public synchronized void setJoinedDevices(List<ZbDeviceAttr> joinedDevices) {
    GenericStateApi.setStateList(mZigbeeAttr.mJoinedDevices ,joinedDevices);
  }

  public synchronized List<ZbDeviceAttr> getJoinedDevices() {
    return GenericStateApi.getState(mZigbeeAttr.mJoinedDevices);
  }

  public synchronized void setJoiningState(JoiningState joiningState) {
    mZigbeeAttr.mJoiningState = joiningState;
  }

  public synchronized JoiningState getJoiningState() {
    return mZigbeeAttr.mJoiningState;
  }

  public synchronized ZbSignalAttr getSignal() {
    return GenericStateApi.getState(mZigbeeAttr.mSignal);
  }

  public synchronized boolean isCoordinatorStateChanged() {
    return isCoordinatorStateChanged;
  }
}
