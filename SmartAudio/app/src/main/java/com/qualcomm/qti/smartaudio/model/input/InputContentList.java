/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model.input;

import com.qualcomm.qti.smartaudio.model.ContentList;

public class InputContentList extends ContentList {
	private static final long serialVersionUID = -4427191668924754339L;

	@Override
	public ContentListType getContentListType() {
		return ContentListType.INPUT;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isPlayAll() {
		return false;
	}

	@Override
	public boolean isSearchable() {
		return false;
	}
}
