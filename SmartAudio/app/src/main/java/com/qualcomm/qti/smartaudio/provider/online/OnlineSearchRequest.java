/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.online;

import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.Provider;

public class OnlineSearchRequest extends ContentSearchRequest {
	private static final long serialVersionUID = 1556684650841806729L;

	@Override
	public ContentSearchType getSearchType() {
		return ContentSearchType.ONLINE;
	}

	@Override
	protected Provider getProvider() {
		return null;
	}
}
