/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;

public class EqualizerBandSettings {

     public  int bandLevel;
     public double centerFrequency;
     public float  percentage;
     int maxLevelRange;
     int minLevelRange;

     public EqualizerBandSettings(int level, double frequency, float percentage, int max, int min) {
      this.bandLevel = level;
      this.centerFrequency = frequency;
      this.percentage = percentage;
      this.maxLevelRange = max;
      this.minLevelRange = min;
     }

     public int getMax() {
      return maxLevelRange;
     }

     public int getMin() {
      return minLevelRange;
     }


}
