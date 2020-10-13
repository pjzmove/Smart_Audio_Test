/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model.playlist;

import com.qualcomm.qti.smartaudio.model.ContentList;

public class PlaylistContentList extends ContentList {
	private static final long serialVersionUID = -7190466376738690058L;

	public PlaylistContentList() {
		super();
	}

	@Override
	public ContentListType getContentListType() {
		return ContentListType.PLAYLIST;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isPlayAll() {
		return true;
	}
}
