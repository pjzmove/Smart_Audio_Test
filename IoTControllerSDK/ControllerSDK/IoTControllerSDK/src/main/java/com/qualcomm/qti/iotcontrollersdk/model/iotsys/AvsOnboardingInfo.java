/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.iotsys;

import android.os.Parcel;
import android.os.Parcelable;

public class AvsOnboardingInfo implements Parcelable {
    public String mUrl;
    public String mCode;
    public boolean isAVSOnboarding;
    public boolean isAVSEnabled;


  public AvsOnboardingInfo(String url, String code) {
    mUrl = url;
    mCode = code;
    isAVSOnboarding =false;
    isAVSEnabled =false;
  }

  public AvsOnboardingInfo(Parcel in) {
		mUrl = in.readString();
		mCode = in.readString();
		isAVSOnboarding = (in.readByte() == (byte)1);
		isAVSEnabled = (in.readByte() == (byte)1);
	}


  public static final Creator<AvsOnboardingInfo> CREATOR = new Creator<AvsOnboardingInfo>() {
    @Override
    public AvsOnboardingInfo createFromParcel(Parcel in) {
      return new AvsOnboardingInfo(in);
    }

    @Override
    public AvsOnboardingInfo[] newArray(int size) {
      return new AvsOnboardingInfo[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(mUrl);
      dest.writeString(mCode);
      dest.writeByte(isAVSOnboarding?(byte)1:(byte)0);
      dest.writeByte(isAVSEnabled?(byte)1:(byte)0);
  }
}
