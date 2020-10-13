/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataList implements Serializable {
	private static final long serialVersionUID = 2754468572277837013L;

	private List<ContentItem> mItems = null;

	public DataList() {
		mItems = Collections.synchronizedList(new ArrayList<ContentItem>());
	}

	public DataList(final List<ContentItem> items) {
		super();
		mItems.addAll(items);
	}

	public List<ContentItem> getItems() {
		return mItems;
	}

	public void setItems(final List<ContentItem> items) {
		synchronized (mItems) {
			mItems.clear();
			mItems.addAll(items);
		}
	}

	public void addItem(final int position, final ContentItem item) {
		if ((position < 0) || (item == null)) {
			return;
		}

		if (position >= mItems.size()) {
			addItem(item);
		}

		mItems.add(position, item);
	}

	public void addItem(final ContentItem item) {
		if (item == null) {
			return;
		}
		mItems.add(item);
	}

	public void addItems(final List<ContentItem> items) {
		if ((items == null) || items.isEmpty()) {
			return;
		}
		mItems.addAll(items);
	}

	public void addItems(final int position, final List<ContentItem> items) {
		if ((position < 0) || (items == null)) {
			return;
		}

		if (position >= mItems.size()) {
			addItems(items);
		}

		mItems.addAll(position, items);
	}

	public void append(final DataList dataList) {
		if ((dataList == null) || dataList.isEmpty()) {
			return;
		}
		addItems(dataList.getItems());
	}

	public boolean isEmpty() {
		return mItems.isEmpty();
	}

	public int size() {
		return mItems.size();
	}

	public void clear() {
		mItems.clear();
	}
}
