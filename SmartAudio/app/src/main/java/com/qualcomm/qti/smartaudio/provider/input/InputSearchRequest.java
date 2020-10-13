/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.input;

import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.Provider;

public class InputSearchRequest extends ContentSearchRequest {
	private static final long serialVersionUID = 1556684650841806725L;

	@Override
	public ContentSearchType getSearchType() {
		return ContentSearchType.INPUT;
	}

	@Override
	protected Provider getProvider() {
		return InputProvider.getInstance();
	}


}
