/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LoadDataResult implements Parcelable {

	private ContentList mContentList = null;
	private Exception mException = null;

	public LoadDataResult(final ContentList contentList) {
		mContentList = contentList;
	}

	public LoadDataResult(final Exception exception) {
		mException = exception;
	}

	public ContentList getContentList() {
		return mContentList;
	}

	public Exception getException() {
		return mException;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel destination, final int flag) {
		destination.writeSerializable(mContentList);
		destination.writeSerializable(mException);
	}

	public static final Creator<LoadDataResult> CREATOR = new Creator<LoadDataResult>() {
		@Override
		public LoadDataResult createFromParcel(final Parcel in) {
			return new LoadDataResult(in);
		}

		@Override
		public LoadDataResult[] newArray(final int size) {
			return new LoadDataResult[size];
		}
	};

	protected LoadDataResult(final Parcel in) {
		mContentList = (ContentList) in.readSerializable();
		mException = (Exception) in.readSerializable();
	}
}
