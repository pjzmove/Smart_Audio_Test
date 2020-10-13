/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.utils;

import java.util.Arrays;
import java.util.List;

public class VoiceUINameHelper {

  public static List<String> getSpeakerNameList() {
    return Arrays.asList("Bedroom", "Kitchen", "Living Room");
  }

  public static List<String> getGroupNameList() {
    return Arrays
        .asList("Bedroom", "Kitchen", "Living Room", "Group One", "Group Two", "Group Three");
  }

  public static List<String> getZigbeeVoiceUiList() {
    return Arrays.asList("Bedroom", "Kitchen", "Living Room");
  }

  public static String convert2DutName(String name) {
    if (name != null && !name.isEmpty()) {
      if (name.equalsIgnoreCase("Bedroom")) {
        return "bedroom";
      } else if (name.equalsIgnoreCase("Kitchen")) {
        return "kitchen";
      } else if (name.equalsIgnoreCase("Living Room")) {
        return "living_room";
      } else if (name.equalsIgnoreCase("Group One")) {
        return "group_one";
      } else if (name.equalsIgnoreCase("Group Two")) {
        return "group_two";
      } else if (name.equalsIgnoreCase("Group Three")) {
        return "group_three";
      }
    }
    return name;
  }

  public static String convert2GroupUIName(String name) {
    if (name != null && !name.isEmpty()) {
      if (name.equalsIgnoreCase("bedroom")) {
        return "Bedroom";
      } else if (name.equalsIgnoreCase("kitchen")) {
        return "Kitchen";
      } else if (name.equalsIgnoreCase("living_room")) {
        return "Living Room";
      } else if (name.equalsIgnoreCase("group_one")) {
        return "Group One";
      } else if (name.equalsIgnoreCase("group_two")) {
        return "Group Two";
      } else if (name.equalsIgnoreCase("group_three")) {
        return "Group Three";
      }
    }
    return name;
  }

}
