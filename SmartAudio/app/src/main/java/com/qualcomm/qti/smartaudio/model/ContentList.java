/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

public abstract class ContentList implements Serializable, Parcelable {

	private static final long serialVersionUID = -617684723103660063L;

	protected String mTitle;
	protected DataList mDataList;
	protected String mEmptyString;

	protected boolean mEditable = false;
	protected boolean mPlayAll = false;
	protected boolean mSearchable = true;

	public enum ContentListType {
		MEDIA,
		PLAYLIST,
		INPUT,
		UPNP
	}

	public ContentList() {
		mDataList =  new DataList();
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(final String title) {
		mTitle = title;
	}

	public DataList getDataList() {
		return mDataList;
	}

	public void setDataList(final DataList dataList) {
		this.mDataList = dataList;
	}

	public boolean isEditable() {
		return mEditable;
	}

	public void setEditable(final boolean editable) {
		mEditable = editable;
	}

	public boolean isPlayAll() {
		return mPlayAll;
	}

	public void setPlayAll(final boolean playAll) {
		mPlayAll = playAll;
	}

	public void setSearchable(final boolean searchable) {
		mSearchable = searchable;
	}

	public boolean isSearchable() {
		return mSearchable;
	}

	public void setEmptyString(final String emptyString) {
		mEmptyString = emptyString;
	}

	public String getEmptyString() {
		return mEmptyString;
	}

	public int getPlayableCount() {
		int count = 0;
		List<ContentItem> items = mDataList.getItems();
		for (ContentItem item : items) {
			switch (item.getContentType()) {
				case MEDIA:
					count++;
					break;
				case GROUP:
					ContentGroup contentGroup = (ContentGroup)item;
					count += contentGroup.getPlayableCount();
					break;
				default:
					break;
			}
		}
		return count;
	}

	public int getSize() {
		return mDataList.size();
	}

	public boolean isEmpty() {
		return mDataList.isEmpty();
	}

	public void clear() {
		mDataList.clear();
	}

	public abstract ContentListType getContentListType();

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel destination, final int flag) {
		destination.writeString(mTitle);
		destination.writeSerializable(mDataList);
		destination.writeInt((mEditable) ? 1 : 0);
		destination.writeInt((mPlayAll) ? 1 : 0);
		destination.writeInt((mSearchable) ? 1 : 0);
		destination.writeString(mEmptyString);
	}

	protected ContentList(final Parcel in) {
		mTitle = in.readString();
		mDataList = (DataList) in.readSerializable();
		mEditable = (in.readInt() == 1);
		mPlayAll = (in.readInt() == 1);
		mSearchable = (in.readInt() == 1);
		mEmptyString = in.readString();
	}
}
