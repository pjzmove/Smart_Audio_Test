/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;

public class ContentGroup extends ContentItem {

	private static final long serialVersionUID = 136568767526275860L;

	protected ContentSearchRequest mContentSearchRequest = null;
	protected String mTitle = null;
	protected String mSubtitle = null;
	protected String mThumbnailUrl = null;
	protected int mPlayableCount = 0;

	public ContentGroup() {
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
	public ContentType getContentType() {
		return ContentType.GROUP;
	}

	@Override
	public String getSubtitle() {
		return mSubtitle;
	}

	@Override
	public void setSubtitle(final String subtitle) {
		mSubtitle = subtitle;
	}

	@Override
	public String getThumbnailUrl() {
		return mThumbnailUrl;
	}

	@Override
	public void setThumbnailUrl(final String thumbnailUrl) {
		mThumbnailUrl = thumbnailUrl;
	}

	public ContentSearchRequest getRequest() {
		return mContentSearchRequest;
	}

	public void setRequest(final ContentSearchRequest request) {
		mContentSearchRequest = request;
	}

	public int getPlayableCount() {
		return mPlayableCount;
	}

	public void setPlayableCount(final int playableCount) {
		mPlayableCount = playableCount;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel destination, final int flag) {
		destination.writeString(mTitle);
		destination.writeString(mSubtitle);
		destination.writeString(mThumbnailUrl);
		destination.writeInt(mPlayableCount);
		destination.writeSerializable(mContentSearchRequest);
	}

	public static final Creator<ContentGroup> CREATOR = new Parcelable.Creator<ContentGroup>() {
		@Override
		public ContentGroup createFromParcel(final Parcel in) {
			return new ContentGroup(in);
		}

		@Override
		public ContentGroup[] newArray(final int size) {
			return new ContentGroup[size];
		}
	};

	protected ContentGroup(final Parcel in) {
		mTitle = in.readString();
		mSubtitle = in.readString();
		mThumbnailUrl = in.readString();
		mPlayableCount = in.readInt();
		mContentSearchRequest = (ContentSearchRequest) in.readSerializable();
	}
}
