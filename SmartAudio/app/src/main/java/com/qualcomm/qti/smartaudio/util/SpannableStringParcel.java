/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.TextUtils;

public class SpannableStringParcel implements Parcelable {
	private SpannableString mSpannableString = null;

	public SpannableStringParcel(final SpannableString spannableString) {
		mSpannableString = spannableString;
	}

	protected SpannableStringParcel(Parcel parcel) {
		mSpannableString = (SpannableString) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
	}

	public SpannableString getSpannableString() {
		return mSpannableString;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		TextUtils.writeToParcel(mSpannableString, dest, flags);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<SpannableStringParcel> CREATOR = new Creator<SpannableStringParcel>() {
		@Override
		public SpannableStringParcel createFromParcel(Parcel in) {
			return new SpannableStringParcel(in);
		}

		@Override
		public SpannableStringParcel[] newArray(int size) {
			return new SpannableStringParcel[size];
		}
	};
}