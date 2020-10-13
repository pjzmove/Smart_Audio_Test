/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.InputOutputInfoAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.OutputSelectorAttr;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class OutputSourceSelectionState extends ResourceState {

  private final OutputSelectorAttr outputSelectorAttr = new OutputSelectorAttr();

  public OutputSourceSelectionState() {
  }

  public synchronized OutputSelectorAttr getOutputSourceSelection() {
      return GenericStateApi.getState(outputSelectorAttr);
  }

  public synchronized void update(OutputSelectorAttr attr) {
    GenericStateApi.setState(outputSelectorAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
      return outputSelectorAttr.checkDifference(rep) && GenericStateApi.updateState(outputSelectorAttr, rep);
  }

  public synchronized List<InputOutputInfoAttr> getActiveOutputs() {
      return GenericStateApi.getState(outputSelectorAttr.mActiveOutputs);
  }

  public synchronized List<InputOutputInfoAttr>  getOutputList() {
      return GenericStateApi.getState(outputSelectorAttr.mOutputList);
  }

  public synchronized int getVersion() {
      return outputSelectorAttr.mVersion;
  }

}
