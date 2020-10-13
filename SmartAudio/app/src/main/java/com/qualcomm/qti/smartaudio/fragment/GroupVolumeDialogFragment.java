/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupVolumeChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupListChangedListener;
import com.qualcomm.qti.smartaudio.util.CustomTextAppearanceSpan;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.smartaudio.util.Utils;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnPlayerVolumeChangedListener;

public class GroupVolumeDialogFragment extends CustomDialogFragment implements View.OnClickListener,
    OnPlayerVolumeChangedListener, OnGroupListChangedListener,
    OnCurrentGroupVolumeChangedListener {
	private static final int ZONE_VOLUME_DIALOG = 4;

	private static final String KEY_ZONE_ID = "keyZoneID";
	private static final String TAG = GroupVolumeDialogFragment.class.getSimpleName();

	private String mZoneID = null;
	private ListView mListView = null;
	private PlayerVolumeAdapter mAdapter = null;
	private SeekBar mSeekBar;
	private TextView mTextViewVolume;

	public static GroupVolumeDialogFragment newDialog(final String tag, final String zoneID) {
		GroupVolumeDialogFragment fragment = new GroupVolumeDialogFragment();
		Bundle args = new Bundle();
		args.putInt(KEY_TYPE, ZONE_VOLUME_DIALOG);
		args.putString(KEY_TAG, tag);
		if (!Utils.isStringEmpty(zoneID)) {
			args.putString(KEY_ZONE_ID, zoneID.trim());
		}
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		if (getArguments() != null) {
			mZoneID = getArguments().getString(KEY_ZONE_ID);
		}
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	protected Dialog createDialogFromType() {
		return createZoneVolumeDialog();
	}

	private Dialog createZoneVolumeDialog() {
		final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final View view = inflater.inflate(R.layout.dialog_custom, null);
		final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.custom_dialog_frame);
		inflater.inflate(R.layout.frame_zone_volume, frameLayout, true);

		TextView textView = (TextView) view.findViewById(R.id.custom_dialog_title_text);
		textView.setVisibility(View.GONE);
		textView = (TextView) view.findViewById(R.id.custom_dialog_message_text);
		textView.setVisibility(View.GONE);

		RelativeLayout buttonLayout = view.findViewById(R.id.custom_dialog_button_layout);
		buttonLayout.setVisibility(View.GONE);

		dialog.setContentView(view);

		if (!mBaseActivity.isTablet()) {
			DisplayMetrics metrics = new DisplayMetrics();
			WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			display.getMetrics(metrics);

			WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
			params.width = metrics.widthPixels -
					getResources().getDimensionPixelSize(R.dimen.zone_volume_horizontal_margin);
			dialog.getWindow().setAttributes(params);
		}

		ImageButton closeButton = view.findViewById(R.id.zone_volume_close_button);
		closeButton.setOnClickListener(this);

		mListView = view.findViewById(R.id.zone_volume_list_view);

    mSeekBar = view.findViewById(R.id.list_item_player_volume_seekbar);
    mTextViewVolume = view.findViewById(R.id.list_item_player_volume_text);
    final IoTGroup zone = getZoneFromId();
    if(zone != null) {

      mSeekBar.setProgress(zone.getVolume());

      updateText(mTextViewVolume,
          getString(R.string.player_volume, getString(R.string.master), zone.getVolume())
              + getString(R.string.percentage_symbol));
      mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          if (!fromUser) {
            return;
          }

          if (zone == null) {
            return;
          }
          updateText(mTextViewVolume,
              getString(R.string.player_volume, getString(R.string.master), zone.getVolume()) +
                  getString(R.string.percentage_symbol));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          if (zone == null) {
            return;
          }
          zone.setVolume(seekBar.getProgress());
          updateText(mTextViewVolume,
              getString(R.string.player_volume, getString(R.string.master), zone.getVolume()) +
                  getString(R.string.percentage_symbol));
        }
      });
    }
		mAdapter = new PlayerVolumeAdapter();
		mListView.setAdapter(mAdapter);

		return dialog;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.zone_volume_close_button:
				mBaseActivity.dismissDialog(mTag);
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		final SmartAudioApplication smartAudioApplication = (SmartAudioApplication)mBaseActivity.getApplication();
		if ((smartAudioApplication != null) && smartAudioApplication.isInit()) {
			final AllPlayManager allPlayManager = smartAudioApplication.getAllPlayManager();
			allPlayManager.addOnPlayerVolumeChangedListener(this);
			allPlayManager.addOnZoneListChangedListener(this);
			allPlayManager.addOnCurrentZoneVolumeChangedListener(this);

		}
		update();
	}

	@Override
	public void onPause() {
		super.onPause();
		final SmartAudioApplication smartAudioApplication = (SmartAudioApplication)mBaseActivity.getApplication();
		if ((smartAudioApplication != null) && smartAudioApplication.isInit()) {
			final AllPlayManager allPlayManager = smartAudioApplication.getAllPlayManager();
			allPlayManager.removeOnPlayerVolumeChangedListener(this);
			allPlayManager.removeOnZoneListChangedListener(this);
			allPlayManager.removeOnCurrentZoneVolumeChangedListener(this);
		}
	}

	private void updateInUiThread() {
		if (Utils.isActivityActive(mBaseActivity)) {
			mBaseActivity.runOnUiThread(() -> {
        if (Utils.isActivityActive(mBaseActivity)) {
          update();
        }
      });
		}
	}

	private IoTGroup getZoneFromId() {
		final SmartAudioApplication smartAudioApplication = (SmartAudioApplication)mBaseActivity.getApplication();
		return smartAudioApplication.getAllPlayManager().getZone(mZoneID);
	}

	private String getZoneID() {
		synchronized (this) {
			return mZoneID;
		}
	}

	private void update() {
		final String zoneID = getZoneID();
		if (zoneID == null) {
			return;
		}

		final IoTGroup zone = getZoneFromId();
		if (zone == null) {
			return;
		}
		mAdapter.updatePlayers(zone.getPlayers());
	}

	@Override
	public void onZoneListChanged() {
		updateInUiThread();
	}



	@Override
	public void onPlayerMuteStateChanged(IoTPlayer player, boolean muted) {
	  Log.d(TAG,"onPlayerMuteStateChanged :" + muted );
	}

	@Override
	public void onCurrentGroupVolumeStateChanged(final int volume, boolean user) {
		Log.d(TAG,"onCurrentGroupVolumeStateChanged " + volume + ", user " + user);

		// TODO : verify user is always false
		if (user || !mSeekBar.isPressed()) {
			mBaseActivity.runOnUiThread(() -> {
        if (Utils.isActivityActive(mBaseActivity)) {
          mSeekBar.setProgress(volume);
          updateText(mTextViewVolume,
              getString(R.string.player_volume, getString(R.string.master), volume) +
                  getString(R.string.percentage_symbol));
        }
      });
		}
	}

	@Override
	public void onPlayerVolumeStateChanged(IoTPlayer player, int volume, boolean user) {
		// user is true when master volume changes volume, but if another controller app is changing volume user will be false
		Log.d(TAG,"onPlayerVolumeStateChanged: " + volume + ", user: " + user);
		if (user || !mSeekBar.isPressed()) {
			IoTGroup currentZone = getZoneFromId();
			List<IoTPlayer> zonePlayers = currentZone.getPlayers();
			if (!zonePlayers.contains(player)) {
				return;
			}
			updateInUiThread();
		}

	}

	@Override
	public void onPlayerVolumeEnabledChanged(IoTPlayer player, boolean enabled) {
    UiThreadExecutor.getInstance().execute(()->update());
	}

	@Override
	public void onCurrentZoneVolumeEnabledChanged(boolean enabled) {

	}

	@Override
	public void onCurrentZoneMuteStateChanged(boolean muted) {

	}

	private class PlayerVolumeAdapter extends BaseAdapter {

    final List<IoTPlayer> mPlayers = Collections.synchronizedList(new ArrayList<>());

    public void updatePlayers(final List<IoTPlayer> players) {
      synchronized (mPlayers) {
        mPlayers.clear();
        mPlayers.addAll(players);
        notifyDataSetChanged();
      }
    }

    @Override
    public int getCount() {
      return mPlayers.size();
    }

    @Override
    public Object getItem(int position) {
      return mPlayers.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup viewGroup) {
      final IoTPlayer player = (IoTPlayer) getItem(position);
      if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_item_player_volume, viewGroup, false);
      }

      final TextView textView = convertView.findViewById(R.id.list_item_player_volume_text);
      SeekBar seekBar = convertView.findViewById(R.id.list_item_player_volume_seekbar);

      updateText(textView, getString(R.string.player_volume, player.getName().toUpperCase(), player.getVolume()) +
              getString(R.string.percentage_symbol));

      seekBar.setMax(player.getMaxVolume());
      seekBar.setProgress(player.getVolume());
      seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          if (!fromUser) {
            return;
          }
          updateText(textView,
              getString(R.string.player_volume, player.getName().toUpperCase(), player.getVolume())
                  + getString(R.string.percentage_symbol));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          setVolume(player, seekBar.getProgress(), textView);
        }
      });

      return convertView;
    }

    private void setVolume(final IoTPlayer player, final int volume, final TextView textView) {
      player.setVolume(((double)volume) / 100.0f, success ->
          UiThreadExecutor.getInstance().execute(()->
          updateText(textView,
              getString(R.string.player_volume, player.getName().toUpperCase(), player.getVolume())
                  + getString(R.string.percentage_symbol))
      ));
    }
  }

	private void updateText(final TextView textView, final String text) {
		Spannable sb = new SpannableString(text);
		sb.setSpan(new CustomTextAppearanceSpan(getContext(), R.style.PlayerVolumePercentageText),
				text.indexOf(" - ") + 3,
				text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (textView != null) {
			textView.setText(sb);
		}
	}
}
