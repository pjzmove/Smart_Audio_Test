/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model.upnp;

import com.qualcomm.qti.smartaudio.model.ContentItem;
import com.qualcomm.qti.smartaudio.model.ContentList;

import java.util.List;

public class UpnpContentList extends ContentList {
	private static final long serialVersionUID = 6148257609351059260L;

	@Override
	public ContentListType getContentListType() {
		return ContentListType.UPNP;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isSearchable() {
		return false;
	}

	@Override
	public boolean isPlayAll() {
		return true;
	}

	@Override
	public int getPlayableCount() {
		int count = 0;
		List<ContentItem> items = mDataList.getItems();
		for (ContentItem item : items) {
			switch (item.getContentType()) {
				case MEDIA:
					count++;
					break;
				default:
					break;
			}
		}
		return count;
	}
}
