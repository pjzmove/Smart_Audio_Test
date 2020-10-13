/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.BASS_BOOST;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.DIALOG_CLARITY;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.DOLBY;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.DOLBY_MODE;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.DTS;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.DTS_MODE;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.EQ;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.EQBANDS;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.REVERB;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.REVERB_PRESET;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.TRUMPET;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.TRUMPET_EQBANDS;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.TRUMPET_GAIN;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.TRUMPET_PRESET;
import static com.qualcomm.qti.smartaudio.fragment.AudioSettingFragment.SettingsItemType.TRUMPET_VIRTUALIZATION;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupAudioSettingListener;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.iotcontrollersdk.constants.EffectType;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.EqualizerBandSettings;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.TrumpetEqualizerBand;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.TrumpetEffectState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioSettingFragment extends BaseFragment implements ExpandableListView.OnChildClickListener,
    OnCurrentGroupAudioSettingListener {

    private final static String TAG = "AudioSettingFragment";
    private static final String EXTRA_ID= "DEVICE_ID";
	  private static final String EXTRA_HOST= "HOST_NAME";
    private AudioSettingsAdapter mAdapter;
    private List<SettingsItem> mSettingsItem;
	  private String mId;
	  private String mHost;
	  private Fragment mFragment;

	  final private ArrayList<String> VirtualXOutMode = new ArrayList<>(Arrays.asList("2.0", "2.1", "3.0", "3.1", "5.1"));
	  final private ArrayList<String> DolbyMode = new ArrayList<>(Arrays.asList("Movie", "Music", "Night"));

	  enum SettingsItemType {
	    DOLBY,
	    DOLBY_MODE,
	    DTS,
      DIALOG_CLARITY,
      DTS_MODE,
	    BASS_BOOST,
	    REVERB,
	    REVERB_PRESET,
	    EQ,
	    EQBANDS,
	    TRUMPET,
      TRUMPET_PRESET,
      TRUMPET_GAIN,
      TRUMPET_VIRTUALIZATION,
      TRUMPET_EQBANDS,
      AUDIO_MODE,
	    AUDIO_TREATMENT,
	    GAPLESS_PLAYBACK,
	    NORMALIZE,
	    LIP_SYNC_DELAY,
	    BASS,
	    MIDRANGE,
	    TREBLE
	  }

    public static AudioSettingFragment newInstance(String id, String host) {
      AudioSettingFragment fragment = new AudioSettingFragment();
      Bundle bundle = new Bundle();
      bundle.putString(EXTRA_ID,id);
      bundle.putString(EXTRA_HOST,host);
      fragment.setArguments(bundle);
      fragment.mFragment = fragment;
      return fragment;
    }

    public void onResume() {
      super.onResume();
      mAllPlayManager.addOnCurrentGroupAudioSettingListener(this);
      updateState();
    }

    public void onPause() {
      mAllPlayManager.removeOnCurrentGroupAudioSettingListener(this);
      super.onPause();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
        int childPosition, long id) {
      return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_settings, container, false);
      TextView titleView = view.findViewById(R.id.settings_app_bar_text_view);
      titleView.setText(getString(R.string.audio_setting));
      ExpandableListView expandableListView = view.findViewById(R.id.settings_activity_expand_listview);

      Bundle arg = getArguments();
      mId = arg.getString(EXTRA_ID);
      mHost = arg.getString(EXTRA_HOST);

      if(mSettingsItem != null)
        mSettingsItem.clear();

      if(mHost != null) {
        IoTPlayer player = mAllPlayManager.getPlayerByHostName(mHost);
        if(player != null) {
          mSettingsItem = new ArrayList<>();
        }
      }

		  mAdapter = new AudioSettingsAdapter();
		  expandableListView.setAdapter(mAdapter);

		  expandableListView.setOnChildClickListener(this);
		  return view;
    }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG,"onActivityResult  request code:"+ requestCode + ", resultCode:"+ resultCode);
    IoTPlayer player = mAllPlayManager.getPlayer(mId);
    if(player != null && resultCode >= 0) {
      if(requestCode == EffectType.DOLBY.ordinal()) {
          player.setDolbyMode(resultCode, success ->
              Log.d(TAG, "Set Dolby mode index to " + resultCode));
      } else if(requestCode ==  EffectType.DTS.ordinal()) {
        Log.d(TAG,"setVirtualXOutMode:" + resultCode);
        player.setVirtualXOutMode(resultCode, success ->
            Log.d(TAG, "Changing VirtualX out mode ")
        );
      } else if(requestCode == EffectType.REVERB.ordinal()) {
        List<String> reverbPresetList = player.getPresetReverbList();
        player.setCurrentReverbPreset(reverbPresetList.get(resultCode), success ->
            Log.d(TAG, "Set reverb to " + reverbPresetList.get(resultCode))
        );
      } else if( requestCode == EffectType.TRUMPET_PRESET.ordinal()) {
          List<String> trumpetPersetList = player.getTrumpetPresetList();
          player.setCurrentTrumpetPreset(trumpetPersetList.get(resultCode), success ->
              Log.d(TAG, "Set trumpet preset to " + trumpetPersetList.get(resultCode))
          );
      }
    }
  }

  @Override
	  public void updateState() {
	    mSettingsItem.clear();

	    if (mHost != null) {
        IoTPlayer player = mAllPlayManager.getPlayerByHostName(mHost);
        if (player != null) {
            int idx = 0;
            if(player.isDolbyAvailable()) {
              SettingsItem item = new SettingsItem(DOLBY, getString(R.string.audio_control_dolby),
                  "");
              mSettingsItem.add(item);
              idx++;
              if(player.isDolbyEnabled()) {
                int mode = player.getDolbyMode();
                mSettingsItem.get(idx -1).mDescription = (mode >=0 && mode < DolbyMode.size())?DolbyMode.get(mode):"";
                item = new SettingsItem(DOLBY_MODE, getString(R.string.audio_control_dolby_mode), "");
                mSettingsItem.add(item);
                idx++;
              }
            }

            if(player.isVirtualXAvailable()) {
              SettingsItem item = new SettingsItem(DTS, getString(R.string.audio_control_dts), "");
              mSettingsItem.add(item);
              idx++;

              if(player.isVirtualXEnabled()) {
                item = new SettingsItem(DIALOG_CLARITY, getString(R.string.audio_control_dialog_clarity), "");
                mSettingsItem.add(item);
                idx++;

                int mode = player.getOutMode();
                mSettingsItem.get(idx - 2).mDescription = (mode >=0 && mode < VirtualXOutMode.size())?VirtualXOutMode.get(mode):"";

                item = new SettingsItem(DTS_MODE, getString(R.string.audio_control_dts_mode), "");
                mSettingsItem.add(item);
                idx++;
              }
            }

            if(player.isBassBoostAvailable()) {
              SettingsItem item = new SettingsItem(BASS_BOOST, getString(R.string.audio_control_bass_boost), "");
              mSettingsItem.add(item);
              idx++;
            }

            if(player.isReverbAvaliable()) {
              SettingsItem item = new SettingsItem(REVERB, getString(R.string.audio_control_reverb),
                  "");
              mSettingsItem.add(item);
              idx++;

              if(player.isReverbEnabled()) {
                mSettingsItem.get(idx -1).mDescription = player.getCurrentReverbPreset();
                item = new SettingsItem(REVERB_PRESET, getString(R.string.audio_control_reverb_preset), "");
                mSettingsItem.add(REVERB_PRESET.ordinal(),item);
              }
            }

            if(player.isEqualizerAvailable()) {
              SettingsItem item = new SettingsItem(EQ, getString(R.string.audio_control_equalizer),
                  "");
              mSettingsItem.add(item);

              if(player.isEqualizerEnabled()) {
              List<EqualizerBandSettings>  eqBands= player.getEqualizerSettings();
              String preset = player.getCurrentEqPreset();
              int index = 0;
              if(!eqBands.isEmpty()) {
                for(EqualizerBandSettings band : eqBands) {
                  item = new SettingsItem(EQBANDS, preset, String.format("%.4f Hz",band.centerFrequency),index++);
                  mSettingsItem.add(item);
                  index++;
                }
              }
            }
           }

           if(player.isTrumpetAvailable()) {
            SettingsItem item = new SettingsItem(TRUMPET, getString(R.string.audio_control_trumpet),
                "");
            mSettingsItem.add(item);

            if(player.isTrumpetEnabled()) {

              item = new SettingsItem(TRUMPET_VIRTUALIZATION,getString(R.string.audio_control_trumpet_virtualization),"");
              mSettingsItem.add(item);

              int trumpetIdx = player.getTrumpetEffectState().getCurrentPreset().ordinal();
              String trumpetPreset = TrumpetEffectState.PresetList.get(trumpetIdx);
              item = new SettingsItem(TRUMPET_PRESET,getString(R.string.audio_control_trumpet_preset),trumpetPreset);
              mSettingsItem.add(item);

              item = new SettingsItem(TRUMPET_GAIN,getString(R.string.audio_control_trumpet_gain),"");
              mSettingsItem.add(item);

              List<TrumpetEqualizerBand>  eqBands= player.getTrumpetEqBands();
                String preset = player.getCurrentEqPreset();
                int index = 0;
                if(!eqBands.isEmpty()) {
                  for(TrumpetEqualizerBand band : eqBands) {
                    item = new SettingsItem(TRUMPET_EQBANDS, preset, String.format("%.4f Hz",band.mFrequency),index++);
                    mSettingsItem.add(item);
                  }
                }
            }
          }
        }
      }
      mAdapter.notifyDataSetChanged();
    }

    private class SettingsItem {

      private SettingsItemType mItemType;
      private String mTitle;
      private String mDescription;
      private int index;

      public SettingsItem(SettingsItemType type, String title, String description) {
        mItemType = type;
        mTitle = title;
        mDescription = description;
        index = -1;
      }

      public SettingsItem(SettingsItemType type, String title, String description, int index) {
        mItemType = type;
        mTitle = title;
        mDescription = description;
        this.index = index;
      }

      public SettingsItemType getItemType() {
        return mItemType;
      }

      public String getTitle() {
        return mTitle;
      }

      public String getDescription() {
			return mDescription;
		}
	 }

    private class AudioSettingsAdapter extends BaseExpandableListAdapter {

      @Override
      public int getGroupCount() {
        return 1;
      }

      @Override
      public int getChildrenCount(int groupPosition) {
        return mSettingsItem.size();
      }

      @Override
      public Object getGroup(int groupPosition) {
         return mSettingsItem;
      }

      @Override
      public Object getChild(int groupPosition, int childPosition) {
         return mSettingsItem.get(childPosition);
      }

      @Override
      public long getGroupId(int groupPosition) {
        return groupPosition;
      }

      @Override
      public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
      }

      @Override
      public boolean hasStableIds() {
        return false;
      }

      @Override
      public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
          LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          convertView = layoutInflater.inflate(R.layout.list_item_settings_headers, parent, false);
        }
			  ExpandableListView listView = (ExpandableListView) parent;
			  listView.expandGroup(groupPosition);
			  return convertView;
      }

      @Override
      public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (mHost != null) {
          IoTPlayer player = mAllPlayManager.getPlayerByHostName(mHost);
          if (player != null) {

                if (convertView == null) {
                  LayoutInflater layoutInflater = (LayoutInflater) getContext()
                      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                  convertView = layoutInflater.inflate(R.layout.audio_setting_item, parent, false);
                  convertView.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
                }

                SettingsItem itemDetails = ((SettingsItem) getChild(groupPosition, childPosition));
                if (itemDetails != null) {

                  TextView textView = convertView
                      .findViewById(R.id.device_settings_child_text_view);
                  textView.setText(itemDetails.getTitle());

                  TextView subTextView = convertView
                      .findViewById(R.id.device_settings_child_sub_text_view);

                  Switch switchView = convertView
                      .findViewById(R.id.device_settings_child_switch_view);
                  SeekBar seekBarView = convertView
                      .findViewById(R.id.device_settings_child_seekbar);

                  RelativeLayout textViewLayout = convertView
                      .findViewById(R.id.device_settings_child_text_view_layout);
                  ViewGroup.LayoutParams layoutParams = textViewLayout.getLayoutParams();
                  layoutParams.height = (int) getResources()
                      .getDimension(R.dimen.settings_activity_list_item_settings_child_height);

                  if (!Utils.isStringEmpty(itemDetails.getDescription())) {
                    layoutParams.height = (int) getResources().getDimension(
                        R.dimen.device_settings_activity_list_item_settings_sub_child_height);
                    subTextView.setVisibility(View.VISIBLE);
                    subTextView.setText(itemDetails.getDescription());
                  }

                  textViewLayout.setLayoutParams(layoutParams);
                  boolean isEnabled;
                  switch ((itemDetails.getItemType())) {
                    case DOLBY:
                      switchView.setVisibility(View.VISIBLE);
                      seekBarView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.VISIBLE);
                      isEnabled = player.isDolbyEnabled();
                      switchView.setChecked(isEnabled);

                      boolean enabled = isEnabled;
                      switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (!player.enableDolby(isChecked, success -> {
                          if (!success)
                            UiThreadExecutor.getInstance()
                                .execute(() -> switchView.setChecked(enabled));
                        })) {
                          switchView.setChecked(enabled);
                        }
                      });
                      break;
                    case DOLBY_MODE:
                      switchView.setVisibility(View.GONE);
                      seekBarView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.GONE);
                      textViewLayout.setOnClickListener( v -> {
                          EffectModeSelectionFragment fragment = EffectModeSelectionFragment.newInstance(
                              DolbyMode, EffectType.DOLBY, mId);
                          fragment.setTargetFragment(mFragment,DOLBY.ordinal());
                          fragment.show(getFragmentManager(), "Show Mode");
                      });
                      break;
                    case DTS:
                      switchView.setVisibility(View.VISIBLE);
                      seekBarView.setVisibility(View.GONE);
                      isEnabled = player.isVirtualXEnabled();
                      subTextView.setVisibility(View.VISIBLE);
                      switchView.setChecked(isEnabled);

                      enabled = isEnabled;
                      switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (!player.enableVirtualX(isChecked, success -> {
                          if (!success)
                            UiThreadExecutor.getInstance()
                                .execute(() -> switchView.setChecked(enabled));
                        })) {
                          switchView.setChecked(enabled);
                        }
                      });
                      break;
                    case DTS_MODE:
                      switchView.setVisibility(View.GONE);
                      seekBarView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.GONE);
                      textViewLayout.setOnClickListener( v -> {
                        EffectModeSelectionFragment fragment = EffectModeSelectionFragment.newInstance(
                            VirtualXOutMode, EffectType.DTS,
                            mId);
                        fragment.setTargetFragment(mFragment,EffectType.DTS.ordinal());
                        fragment.show(getFragmentManager(), "Show Mode");
                      });
                      break;
                    case BASS_BOOST:
                      switchView.setVisibility(View.VISIBLE);
                      seekBarView.setVisibility(
                          (isEnabled = player.isBassboostEnabled()) ? View.VISIBLE
                              : View.INVISIBLE);

                      switchView.setChecked(isEnabled);
                      enabled = isEnabled;
                      switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (!player.enableBassboost(isChecked, success -> {
                          if (!success)
                            UiThreadExecutor.getInstance()
                                .execute(() -> switchView.setChecked(enabled));
                        })) {
                          switchView.setChecked(enabled);
                        }
                      });

                      seekBarView.setMax(player.getBassboostsMax());
                      seekBarView.setProgress(player.getBassboostsStrength());
                      seekBarView.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                            boolean fromUser) {

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                          int value = seekBar.getProgress();
                          if (value > player.getBassboostsMin() && value < player
                              .getBassboostsMax())
                            player.setBassBoosts(value, success ->
                                Log.d(TAG, "Set Bass boost strength:" + value)
                            );
                        }
                      });

                      break;
                    case REVERB:
                      switchView.setVisibility(View.VISIBLE);
                      seekBarView.setVisibility(View.GONE);
                      isEnabled = player.isReverbEnabled();
                      switchView.setChecked(isEnabled);
                      enabled = isEnabled;
                      switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (!player.enableReverb(isChecked, success -> {
                          if (!success)
                            UiThreadExecutor.getInstance()
                                .execute(() -> switchView.setChecked(enabled));
                        })) {
                          switchView.setChecked(enabled);
                        }
                      });

                      break;
                    case REVERB_PRESET:
                      switchView.setVisibility(View.GONE);
                      seekBarView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.GONE);
                      textViewLayout.setOnClickListener( v -> {
                        EffectModeSelectionFragment fragment = EffectModeSelectionFragment.newInstance(
                            (ArrayList<String>) player.getPresetReverbList(), EffectType.REVERB,
                            mId);
                        fragment.setTargetFragment(mFragment,EffectType.REVERB.ordinal());
                        fragment.show(getFragmentManager(), "Show Mode");
                      });
                      break;
                    case DIALOG_CLARITY:
                        switchView.setVisibility(View.VISIBLE);
                        isEnabled = player.isDialogClarify();
                        switchView.setChecked(isEnabled);
                        enabled = isEnabled;
                        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                          if (!player.enableDialogClarity(isChecked, success -> {
                            if (!success) {
                              Log.d(TAG,"Post enable Dialog Clarity failed!");
                              UiThreadExecutor.getInstance()
                                  .execute(() -> switchView.setChecked(enabled));
                            }
                          })) {
                            switchView.setChecked(enabled);
                          }
                        });
                      break;
                    case EQ:
                      switchView.setVisibility(View.VISIBLE);
                      seekBarView.setVisibility(View.GONE);
                      isEnabled = player.isEqualizerEnabled();
                      switchView.setChecked(isEnabled);
                      enabled = isEnabled;
                      switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (!player.enabledEqualizer(isChecked, success -> {
                          if (!success)
                            UiThreadExecutor.getInstance()
                                .execute(() -> switchView.setChecked(enabled));
                        })) {
                          switchView.setChecked(enabled);
                        }
                      });
                      break;
                    case NORMALIZE:
                      switchView.setVisibility(View.VISIBLE);
                      subTextView.setVisibility(View.GONE);
                      break;
                    case AUDIO_MODE:
                    case AUDIO_TREATMENT:
                      subTextView.setVisibility(View.VISIBLE);
                      break;
                    case GAPLESS_PLAYBACK:
                    case LIP_SYNC_DELAY:
                    case BASS:
                    case MIDRANGE:
                    case TREBLE:
                      switchView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.GONE);
                      seekBarView.setVisibility(View.VISIBLE);
                      break;
                    case EQBANDS:
                      switchView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.VISIBLE);
                      textView.setText(itemDetails.getTitle());
                      subTextView.setText(itemDetails.mDescription);
                      final int index = itemDetails.index;
                      seekBarView.setMax(100);
                      List<EqualizerBandSettings> eqBands = player.getEqualizerSettings();
                      if(index >=0 && index < eqBands.size()) {
                        seekBarView.setProgress(eqBands.get(index).bandLevel);
                      }
                      seekBarView.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                            boolean fromUser) {

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                          int value = seekBar.getProgress();
                          if(index < eqBands.size() && index >= 0)
                          eqBands.get(index).bandLevel = seekBar.getProgress();
                          player.setEqualizer(eqBands, success ->
                              Log.d(TAG, String.format("Set eq band %d strength:%d",index, value))
                          );
                        }
                      });
                      break;
                    case TRUMPET:
                      switchView.setVisibility(View.VISIBLE);
                      seekBarView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.INVISIBLE);
                      isEnabled = player.isTrumpetEnabled();
                      switchView.setChecked(isEnabled);
                      enabled = isEnabled;
                      switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (!player.enabledTrumpet(isChecked, success -> {
                          if (!success)
                            UiThreadExecutor.getInstance()
                                .execute(() -> switchView.setChecked(enabled));
                        })){
                          switchView.setChecked(enabled);
                        }
                      });
                      break;
                    case TRUMPET_VIRTUALIZATION:
                      switchView.setVisibility(View.VISIBLE);
                      seekBarView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.INVISIBLE);
                      isEnabled = player.isTrumpetirtualizationEnabled();
                      switchView.setChecked(isEnabled);
                      enabled = isEnabled;
                      switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (!player.enabledTrumpetVirtualization(isChecked, success -> {
                          if (!success)
                            UiThreadExecutor.getInstance()
                                .execute(() -> switchView.setChecked(enabled));
                        })){
                          switchView.setChecked(enabled);
                        }
                      });
                    break;
                    case TRUMPET_PRESET:
                      switchView.setVisibility(View.GONE);
                      seekBarView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.GONE);
                      textViewLayout.setOnClickListener( v -> {
                          EffectModeSelectionFragment fragment = EffectModeSelectionFragment.newInstance(
                              (ArrayList<String>)player.getTrumpetPresetList(), EffectType.TRUMPET_PRESET, mId);
                          fragment.setTargetFragment(mFragment,EffectType.TRUMPET_PRESET.ordinal());
                          fragment.show(getFragmentManager(), "Show Mode");
                      });
                      break;
                    case TRUMPET_GAIN:
                      switchView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.VISIBLE);
                      textView.setText(itemDetails.getTitle());
                      subTextView.setText(itemDetails.mDescription);
                      seekBarView.setVisibility(View.VISIBLE);
                      seekBarView.setMax(100);
                      seekBarView.setProgress((int)(player.getTrumpetGain() * 100));
                      seekBarView.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                            boolean fromUser) {

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                          int gain = seekBar.getProgress();
                          player.setTrumpetGain(gain, success ->
                              Log.d(TAG, String.format("Set Trumpet gain  %d ",gain))
                          );
                        }
                      });
                    break;
                    case TRUMPET_EQBANDS:
                      switchView.setVisibility(View.GONE);
                      subTextView.setVisibility(View.VISIBLE);
                      textView.setText(itemDetails.getTitle());
                      subTextView.setText(itemDetails.mDescription);
                      final int eqIndex = itemDetails.index;
                      seekBarView.setVisibility(View.VISIBLE);
                      seekBarView.setMax(100);

                      final List<TrumpetEqualizerBand> trumpetEqBands = player.getTrumpetEqBands();
                      if(eqIndex >= 0 && eqIndex < trumpetEqBands.size()) {
                        double gain = trumpetEqBands.get(eqIndex).mGain;
                        int progress = 0;
                        if(gain >1.0f)
                          progress = 100;
                        else if(gain > 0.0f)
                          progress = (int)(gain * 100);
                        seekBarView.setProgress(progress);
                      }

                      seekBarView.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                            boolean fromUser) {

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                          if(eqIndex < trumpetEqBands.size() && eqIndex >= 0) {
                            trumpetEqBands.get(eqIndex).mGain = (double)seekBar.getProgress()/100;
                            int value = seekBar.getProgress();
                            player.setTrumpetEqBands(trumpetEqBands, success ->
                                Log.d(TAG, String
                                    .format("Set Trumpet eq band %d strength:%d", eqIndex, value))
                            );
                          }
                        }
                      });
                    break;
                  }
                }
            }
          }
        return convertView;
      }

      @Override
      public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
      }
    }

  @Override
  public void onCurrentGroupAudioEffectChanged(IoTPlayer player) {
     updateInUiThread();
  }

  @Override
  public void onCurrentGroupPresetReverbChanged(IoTPlayer player) {
    updateInUiThread();
  }

  @Override
  public void onCurrentGroupBassboostChanged(IoTPlayer player) {
    updateInUiThread();
  }

  @Override
  public void onCurrentGroupEqualizerChanged(IoTPlayer player) {
    updateInUiThread();
  }

  @Override
  public void onCurrentGroupVirtualXChanged(IoTPlayer player) {
    Log.d(TAG,"onCurrentGroupVirtualXChanged");
    updateInUiThread();
  }

  @Override
  public void onCurrentGroupDolbyChanged(IoTPlayer player) {
    Log.d(TAG,"onCurrentGroupDolbyChanged");
    updateInUiThread();
  }

  @Override
  public void onCurrentGroupTrumpetChanged(IoTPlayer player) {
    updateInUiThread();
  }
}

