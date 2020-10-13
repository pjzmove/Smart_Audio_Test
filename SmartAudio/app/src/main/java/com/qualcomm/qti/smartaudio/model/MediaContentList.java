/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model;

public class MediaContentList extends ContentList {
	private static final long serialVersionUID = 4374342303985805381L;

	public MediaContentList() {
		super();
		mPlayAll = true;
	}

	@Override
	public ContentListType getContentListType() {
		return ContentListType.MEDIA;
	}
}
