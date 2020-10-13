/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.output;

import android.content.Context;
import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.model.ContentList;
import com.qualcomm.qti.smartaudio.model.DataList;
import com.qualcomm.qti.smartaudio.model.input.InputOutputSourceItem;
import com.qualcomm.qti.smartaudio.model.input.InputContentList;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.Provider;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.InputOutputSourceInfo;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import java.util.List;

public class OutputSourceProvider implements Provider {

	private static OutputSourceProvider sInstance = null;

	public static OutputSourceProvider getInstance() {
		OutputSourceProvider outputProvider = sInstance;
		synchronized (OutputSourceProvider.class) {
			if (outputProvider == null) {
				sInstance = outputProvider = new OutputSourceProvider();
			}
		}
		return outputProvider;
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
			    players.forEach(player-> {
            if (player != null && player.isOutSelectorModeSupported()) {
              List<InputOutputSourceInfo> outputSources = player.getOutputSource();
              outputSources.forEach(item -> {
                InputOutputSourceItem inOutItem = new InputOutputSourceItem(item.id,
                    player.getName(), item.name);
                inOutItem.setSelected(item.isActivated);
                dataList.addItem(inOutItem);
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
