/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.qualcomm.qti.smartaudio.model.ContentList;

import java.io.Serializable;

public abstract class ContentSearchRequest implements Serializable, Parcelable {
	private static final long serialVersionUID = -8894048322954757629L;

	protected int mStart;
	protected int mCount;

	protected String mQuery = null;
	protected String mTitle = null;

	public enum ContentSearchType {
		LOCAL,
		PLAYLIST,
		INPUT,
		OUTPUT,
		UPNP,
		ONLINE,
		SEARCH
	}

	public ContentSearchRequest() {
		mStart = 0;
		mCount = 0;
		mQuery = new String();
	}

	public String getQuery() {
		return mQuery;
	}

	public int getCount() {
		return mCount;
	}

	public int getStart() {
		return mStart;
	}

	public void setQuery(final String query) {
		mQuery = query;
	}

	public void setCount(final int count) {
		mCount = count;
	}

	public void setStart(final int start) {
		mStart = start;
	}

	public abstract ContentSearchType getSearchType();

	public ContentList getContent(final Context context, ContentList appendContentList)
			throws ContentProviderException {
		Provider provider = getProvider();
		if (provider == null) {
			throw new ContentProviderException("null Provider");
		}
		return provider.getContent(context, this, appendContentList);
	}

	public ContentList searchContent(final Context context, final String searchText)
			throws ContentProviderException {
		Provider provider = getProvider();
		if (provider == null) {
			throw new ContentProviderException("null Provider");
		}
		return provider.searchContent(context, this, searchText);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel destination, final int flag) {
		destination.writeString(mQuery);
		destination.writeInt(mStart);
		destination.writeInt(mCount);
	}

	protected ContentSearchRequest(final Parcel in) {
		mQuery = in.readString();
		mStart = in.readInt();
		mCount = in.readInt();
	}

	protected abstract Provider getProvider();
}
