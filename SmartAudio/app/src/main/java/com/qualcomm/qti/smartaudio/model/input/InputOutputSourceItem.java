/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model.input;

import android.os.Parcel;
import android.os.Parcelable;

import com.qualcomm.qti.smartaudio.model.ContentItem;

public class InputOutputSourceItem extends ContentItem {
	private static final long serialVersionUID = -2232078200097522996L;

	protected String mTitle = null;
	protected String mInput = null;
	protected boolean mSelected = false;
	protected String mID = null;
	String mSourceName;

	public InputOutputSourceItem(final String id, final String displayName, final String sourceName) {
		mSourceName = sourceName;
		setTitle(displayName + " - " + mSourceName);
		setID(id);
		setInput(sourceName);
	}

	@Override
	public String getTitle() {
		return mTitle;
	}

	public String getSourceName() {
	  return mSourceName;
	}

	@Override
	public void setTitle(String title) {
		mTitle = title;
	}

	@Override
	public String getSubtitle() {
		return null;
	}

	@Override
	public void setSubtitle(String subtitle) {}

	@Override
	public String getThumbnailUrl() {
		return null;
	}

	@Override
	public void setThumbnailUrl(String thumbnailUrl) {}

	public boolean isSelected() {
		return mSelected;
	}

	public void setSelected(final boolean selected) {
		mSelected = selected;
	}

	public String getID() {
		return mID;
	}

	public void setID(final String id) {
		mID = id;
	}

	public String getInput() {
		return mInput;
	}

	public void setInput(final String input) {
		mInput = input;
	}

	@Override
	public ContentType getContentType() {
		return ContentType.INPUT;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mTitle);
	}

	public static final Creator<InputOutputSourceItem> CREATOR = new Parcelable.Creator<InputOutputSourceItem>() {
		@Override
		public InputOutputSourceItem createFromParcel(final Parcel in) {
			return new InputOutputSourceItem(in);
		}

		@Override
		public InputOutputSourceItem[] newArray(final int size) {
			return new InputOutputSourceItem[size];
		}
	};

	protected InputOutputSourceItem(final Parcel in) {
		mTitle = in.readString();
	}
}
