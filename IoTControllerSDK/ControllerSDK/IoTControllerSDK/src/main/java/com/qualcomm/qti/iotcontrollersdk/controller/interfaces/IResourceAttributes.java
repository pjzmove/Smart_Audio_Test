/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller.interfaces;

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

/**
 * The interface defines generic APIs for manipulating state objects
 */
public interface IResourceAttributes {

  /**
   * Converting a IResourceAttributes type to {@link OcRepresentation}
   *
   * @return OcRepresentation object
   * @throws OcException if failure
   */
  OcRepresentation pack() throws OcException;

  /**
   * Unboxing {@link OcRepresentation} object
   *
   * @param  rep is an unboxing OcRepresentation object
   * @return true if success
   * @throws OcException if failure
   */
  boolean unpack(OcRepresentation rep) throws OcException;

  /**
   * IResourceAttributes object clone
   *
   * @return cloned object
   */
  Object getData();

  /**
   * Set new value for the object
   *
   * @param data is the object with new value
   */
  void setData(Object data);

  /**
   * Check the difference between current IResourceAttributes and a given OcRepresentation
   *
   * @param rep is OcRepresentation object
   * @return true if there is different value
   * @throws OcException if failure
   */
  boolean checkDifference(OcRepresentation rep) throws OcException;

}
