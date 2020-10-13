/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ContentSection extends ContentItem {
	private static final long serialVersionUID = -2065046274523302746L;

	private String mTitle = null;

	public ContentSection() {
		super();
	}

	@Override
	public String getTitle() {
		return mTitle;
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

	@Override
	public ContentType getContentType() {
		return ContentType.SECTION;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel destination, final int flags) {
		destination.writeString(mTitle);
	}

	public static final Creator<ContentSection> CREATOR = new Parcelable.Creator<ContentSection>() {
		@Override
		public ContentSection createFromParcel(final Parcel in) {
			return new ContentSection(in);
		}

		@Override
		public ContentSection[] newArray(final int size) {
			return new ContentSection[size];
		}
	};

	protected ContentSection(final Parcel in) {
		mTitle = in.readString();
	}
}
