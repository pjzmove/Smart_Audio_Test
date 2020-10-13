/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;

public abstract class PleaseWaitAsyncTask extends RequestAsyncTask {
	public PleaseWaitAsyncTask(BaseActivity baseActivity, RequestListener listener) {
		super(baseActivity.getString(R.string.please_wait), null, baseActivity, listener);
	}
}
