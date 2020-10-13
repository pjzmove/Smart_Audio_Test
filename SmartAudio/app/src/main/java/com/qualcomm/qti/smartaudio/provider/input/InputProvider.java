/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.input;

import android.content.Context;


import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.model.ContentList;
import com.qualcomm.qti.smartaudio.model.input.InputOutputSourceItem;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.model.DataList;
import com.qualcomm.qti.smartaudio.model.input.InputContentList;
import com.qualcomm.qti.smartaudio.provider.Provider;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import java.util.List;

public class InputProvider implements Provider {

	private static InputProvider sInstance = null;

	public static InputProvider getInstance() {
		InputProvider inputProvider = sInstance;
		synchronized (InputProvider.class) {
			if (inputProvider == null) {
				sInstance = inputProvider = new InputProvider();
			}
		}
		return inputProvider;
	}

	@Override
	public ContentList getContent(Context context, ContentSearchRequest contentSearchRequest, ContentList appendContentList) {
		if (appendContentList == null) {
			appendContentList = new InputContentList();
		}
		appendContentList.setSearchable(false);

		final DataList dataList = appendContentList.getDataList();

		AllPlayManager allPlayManager = ((SmartAudioApplication)context.getApplicationContext()).getAllPlayManager();
		if (allPlayManager != null) {
			final IoTGroup currentZone = allPlayManager.getCurrentGroup();
			if (currentZone != null) {
				List<IoTPlayer> players = currentZone.getPlayers();
				players.forEach(player -> {
          if (player != null && player.isInputSelectorModeSupported()) {
            List<String> inputs = player.getInputSelectorNameList();
            inputs.forEach(inputSource -> {
              InputOutputSourceItem item = new InputOutputSourceItem(player.getPlayerId(),
                  player.getName(), inputSource);
              item.setSelected(inputSource != null && !inputSource.trim().isEmpty()
                  && inputSource.equalsIgnoreCase(player.getActiveInputSource()));
              dataList.addItem(item);
            });
          }
        });
			}
		}
		return appendContentList;
	}

	@Override
	public ContentList searchContent(Context context, ContentSearchRequest contentSearchRequest, String searchText) {
		return null;
	}
}
