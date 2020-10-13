/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model.playlist;

import com.qualcomm.qti.smartaudio.model.ContentList;

public class PlaylistNameContentList extends ContentList {
	private static final long serialVersionUID = 3065560105095842089L;

	@Override
	public ContentListType getContentListType() {
		return ContentListType.PLAYLIST;
	}

	@Override
	public boolean isPlayAll() {
		return false;
	}

	@Override
	public boolean isEditable() {
		return false;
	}
}
