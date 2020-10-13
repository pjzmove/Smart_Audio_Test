/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;

public class ApplicationService extends IntentService {
	private static final String TAG = ApplicationService.class.getSimpleName();

	public static final String EXTRA_COMMAND = "command";
	public static final int STATUS_STARTED = 1;
	public static final int STATUS_RUNNING = 2;
	public static final int STATUS_FINISHED = 3;

	public static final String EXTRA_RETCODE = "retCode";
	public static final int RETURN_FINALIZE_RESET = 9;

	public static final int COMMAND_RESET = 4;
	public static final int NO_ERROR = 0;

	public static final String PREFERENCE_SETTINGS = "preference_settings";
	public static final String PREFERENCE_EULA_ACCEPTED = "preference_eula_accepted";

	private static final String ACTION_INIT = "com.qualcomm.qti.smartaudio.service.applicationservice.action.init";
	public static final String EXTRA_RECEIVER = "com.qualcomm.qti.smartaudio.service.applicationservice.extra.receiver";

	public static void startActionInit(Context context, ResultReceiver receiver) {
		Intent intent = new Intent(context, ApplicationService.class);
		intent.setAction(ACTION_INIT);
		intent.putExtra(EXTRA_RECEIVER, receiver);
		context.startService(intent);
	}

	public ApplicationService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);

			final String action = intent.getAction();
			if (ACTION_INIT.equals(action)) {
				handleActionInit(receiver);
			}

			int ret = NO_ERROR;
			int command = intent.getIntExtra(EXTRA_COMMAND, -1);
			Bundle bundle = new Bundle();
			bundle.putInt(EXTRA_COMMAND, command);
			if (receiver != null) {
				receiver.send(STATUS_STARTED, bundle);
			}

			if (COMMAND_RESET == command) {
				ret = reset();
			}

			if (receiver != null) {
				if (ret == NO_ERROR) {
					receiver.send(STATUS_FINISHED, bundle);
				} else if (ret == RETURN_FINALIZE_RESET) {
					bundle.putInt(EXTRA_RETCODE, ret);
					receiver.send(STATUS_FINISHED, bundle);
				}
			}
		}
	}

	private void handleActionInit(final ResultReceiver receiver) {
		if (receiver == null) {
			return;
		}

		SmartAudioApplication app = (SmartAudioApplication) getApplicationContext();
		if ((app != null) && !app.isInit()) {
			app.init();
		}

		receiver.send(STATUS_FINISHED, null);
	}

	public int reset() {
		SharedPreferences settings = getSharedPreferences(PREFERENCE_EULA_ACCEPTED, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		editor.commit();

		settings = getSharedPreferences(PREFERENCE_SETTINGS, 0);
		settings.edit().clear().commit();

		return RETURN_FINALIZE_RESET;
	}
}
