/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.utils;

import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;
import java.util.ArrayList;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class GenericStateApi {

  private GenericStateApi(){
  }

  public static <T> T getState(T field) {
    T retVal = field;
    return retVal;
  }

  public static <T extends Enum> T getState(T field) {
    T retVal =  field;
    return retVal;
  }

  public static <T extends IResourceAttributes> T getState(T attribute) {
    T retVal =  (T)attribute.getData();
    return retVal;
  }

  public static <T extends MediaItem> List<T> getIoTMediaState(List<? extends MediaItem> items) {
    List<T> retList = new ArrayList<>();
    for(MediaItem obj : items) {
      MediaItem item = new MediaItem(obj);
      retList.add((T)item);
    }
    return retList;
  }

  public static <T extends IResourceAttributes> List<T> getState(List<? extends IResourceAttributes> fields) {
    List<T> retList = new ArrayList<>();
    for(IResourceAttributes obj : fields) {
      T item = (T)obj.getData();
      retList.add(item);
    }
    return retList;
  }

  public static <T> List<T> getPrimitiveTypeList(List<T> fields) {
    List<T> retList = new ArrayList<>();
    for(T value : fields) {
      retList.add(value);
    }
    return retList;
  }

  public static <T> void setPrimitiveTypeList(List<T> origin, List<T> current) {
    if(current == null) return;
    origin.clear();
    for(T value : current) {
      origin.add(value);
    }
  }

  public static <T extends IResourceAttributes> void setState(T origin , T value) {
    origin.setData(value);
  }

  public static <T extends IResourceAttributes> boolean updateState(T origin , OcRepresentation value) throws OcException {
    return origin.unpack(value);
  }

  public static <T extends IResourceAttributes> void setStateList(List<T> origin, List<T> values) {
    origin.clear();
    for(T item : values) {
      T newItem = (T)item.getData();
      origin.add(newItem);
    }
  }
}
