/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.InputOutputInfoAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.InputSelectorAttr;
import java.util.ArrayList;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class InputSourceSelectionState extends ResourceState {

  private final InputSelectorAttr inputSelectorAttr = new InputSelectorAttr();

  public class InputOutputInfo {

    public String id;
    public String name;

    public InputOutputInfo(String id, String name) {
      this.id = id;
      this.name = name;
    }

  }

  public InputSourceSelectionState() {
  }

  public synchronized InputSelectorAttr getInputSourceSelection() {
     return GenericStateApi.getState(inputSelectorAttr);
  }

  public synchronized void update(InputSelectorAttr attr) {
    GenericStateApi.setState(inputSelectorAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
      return inputSelectorAttr.checkDifference(rep) && GenericStateApi.updateState(inputSelectorAttr, rep);
  }

  public synchronized List<String> getInputListName() {
    List<String> retList= new ArrayList<>();
    for(InputOutputInfoAttr attr : inputSelectorAttr.mInputList) {
      retList.add(attr.mFriendlyName);
    }
    return retList;
  }

  public synchronized InputOutputInfo getActiveInput() {
    if(inputSelectorAttr.mActiveInput != null)
      return new InputOutputInfo(inputSelectorAttr.mActiveInput.mId, inputSelectorAttr.mActiveInput.mFriendlyName);
    else
      return null;
  }
}
