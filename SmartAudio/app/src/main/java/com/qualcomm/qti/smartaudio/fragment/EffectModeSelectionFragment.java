/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.iotcontrollersdk.constants.EffectType;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.TrumpetEffectState.TrumpetPreset;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import java.util.ArrayList;
import java.util.List;

public class EffectModeSelectionFragment extends DialogFragment {

  private static final String EXTRA_ID = "DEVICE_ID";
  private final static String EXTRA_KEY_LIST = "EFFECT_MODE_LIST";
  private final static String EXTRA_KEY_TYPE = "EFFECT_TYPE";
  private final static String TAG = "ModeSelection";

  private int mSelectionIdx = -1;

  protected AllPlayManager mAllPlayManager = null;

  public static EffectModeSelectionFragment newInstance(ArrayList<String> modeList, EffectType type,
      String playerId) {
    EffectModeSelectionFragment fragment = new EffectModeSelectionFragment();
    Bundle bundle = new Bundle();
    bundle.putStringArrayList(EXTRA_KEY_LIST, modeList);
    bundle.putInt(EXTRA_KEY_TYPE, type.ordinal());
    bundle.putString(EXTRA_ID, playerId);
    fragment.setArguments(bundle);
    return fragment;
  }

  public void onAttach(Context context) {
    super.onAttach(context);
    SmartAudioApplication app = (SmartAudioApplication) context.getApplicationContext();
    if ((app != null) && app.isInit()) {
      mAllPlayManager = app.getAllPlayManager();
    }
    mSelectionIdx = -1;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

    AlertDialog.Builder builder = new Builder(getActivity());

    Bundle arg = getArguments();
    List<String> modeList = arg.getStringArrayList(EXTRA_KEY_LIST);
    int type = arg.getInt(EXTRA_KEY_TYPE);
    String playerId = arg.getString(EXTRA_ID);
    IoTPlayer player = mAllPlayManager.getPlayer(playerId);
    String title = "No Player Found!";
    if (player != null) {
      if (type == EffectType.DOLBY.ordinal()) {
        title = "Set Dolby Mode";
        builder.setSingleChoiceItems(modeList.toArray(new String[modeList.size()]),
            player.getDolbyMode(), (dialog, which) -> mSelectionIdx = which
        );
      } else if (type == EffectType.DTS.ordinal()) {
        title = "Set VirtualX mode";
        builder.setSingleChoiceItems(modeList.toArray(new String[modeList.size()]),
            player.getOutMode(), (dialog, which) -> mSelectionIdx = which
        );
      } else if (type == EffectType.REVERB.ordinal()) {
        title = "Set Reverb Preset";
        String curReverb = player.getCurrentReverbPreset();
        int curIdx = modeList.indexOf(curReverb);
        builder.setSingleChoiceItems(modeList.toArray(new String[modeList.size()]), curIdx,
            (dialog, which) -> mSelectionIdx = which
        );
      } else if (type == EffectType.TRUMPET_PRESET.ordinal()) {
        title = "Set Trumpet Preset";
        TrumpetPreset presetIndex = player.getTrumpetCurrentPreset();
        builder.setSingleChoiceItems(modeList.toArray(new String[modeList.size()]),
            presetIndex.ordinal(), (dialog, which) -> mSelectionIdx = which
        );
      }

      builder.setTitle(title);

      builder.setPositiveButton("OK", (dialog, which) -> {
        Log.d(TAG,"Click OK!");
        if(getTargetFragment() != null) {
          Log.d(TAG,"Click OK 1!");
          getTargetFragment().onActivityResult(getTargetRequestCode(), mSelectionIdx, new Intent());
        }
      });

      builder.setNegativeButton("Cancel", (dialog, which)->dialog.dismiss());
    }

    return builder.create();
  }


}
