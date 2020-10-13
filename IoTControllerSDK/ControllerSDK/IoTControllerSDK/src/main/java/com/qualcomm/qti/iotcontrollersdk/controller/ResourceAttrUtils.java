/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class ResourceAttrUtils {

  public static int[] streamFromIntArray(List<Integer> attrs) {
    return attrs.stream().mapToInt(i->i).toArray();
  }

  public static String[] streamFromStringArray(List<String> attrs) {
    return attrs.toArray(new String[attrs.size()]);
  }

  public static String stringValueFromRepresentation(OcRepresentation rep, String key) throws OcException {
    if(rep != null) {
      return rep.getValue(key);
    }
    return null;
  }

  public static List<Integer> intArrayFromStream(OcRepresentation rep, String key) throws OcException {
    if(rep != null && rep.getValue(key) != null) {
      return IntStream.of(rep.getValue(key)).boxed().collect( Collectors.toList() );
    }
    return null;
  }

  public static boolean boolValueFromRepresentation(OcRepresentation rep, String key) throws OcException {
    if(rep != null) {
      return rep.getValue(key);
    }
    return false;
  }

  public static double doubleValueFromRepresentation(OcRepresentation rep, String key) throws OcException {
    if(rep != null) {
      return rep.getValue(key);
    }
    return 0f;
  }

  public static int intValueFromRepresentation(OcRepresentation rep, String key) throws OcException {
    if(rep != null) {
      return rep.getValue(key);
    }
    return -1;
  }

  public static List<String> stringArrayFromStream(OcRepresentation rep, String key) throws OcException {
    if(rep != null && rep.getValue(key) != null) {
      return Arrays.asList((String[])rep.getValue(key));
    }
    return null;
  }

}
