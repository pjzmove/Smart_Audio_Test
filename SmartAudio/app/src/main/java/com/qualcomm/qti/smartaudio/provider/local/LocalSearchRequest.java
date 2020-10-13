/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.local;

import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.Provider;

public class LocalSearchRequest extends ContentSearchRequest {

	private static final long serialVersionUID = 1275837439862001804L;

	public LocalSearchRequest() {
		super();
	}

	@Override
	public ContentSearchType getSearchType() {
		return ContentSearchType.LOCAL;
	}

	@Override
	protected Provider getProvider() {
		return LocalProvider.getInstance();
	}
}
