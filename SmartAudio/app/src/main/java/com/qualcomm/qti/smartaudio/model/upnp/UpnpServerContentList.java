/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model.upnp;

import com.qualcomm.qti.smartaudio.model.ContentList;

public class UpnpServerContentList extends ContentList {
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
		return false;
	}
}
