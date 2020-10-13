/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SwitchCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.activity.MultichannelSetupActivity;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupListChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupInfoStateChangedListener;
import com.qualcomm.qti.smartaudio.service.HttpServer;
import com.qualcomm.qti.smartaudio.util.BaseAsyncTask;
import com.qualcomm.qti.smartaudio.util.CustomTextAppearanceSpan;
import com.qualcomm.qti.smartaudio.util.RequestAsyncTask;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.smartaudio.view.VolumeSliderLockButton;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.InputSelectorAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnHomeTheaterChannelChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnPlayerInputSelectorChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnPlayerVolumeChangedListener;
import org.iotivity.base.OcException;

public class AdjustAudioFragment extends SetupFragment implements SeekBar.OnSeekBarChangeListener,
    OnHomeTheaterChannelChangedListener, OnPlayerVolumeChangedListener,
    OnGroupListChangedListener, CustomDialogFragment.OnCustomDialogButtonClickedListener,
		CompoundButton.OnCheckedChangeListener, OnPlayerInputSelectorChangedListener,
    OnGroupInfoStateChangedListener {
	private final static String TAG = AdjustAudioFragment.class.getSimpleName();

	private boolean mFirstTime = true;
	private boolean mUsePreviousSettings = false;

	private static int DEFAULT_SURROUND_SYSTEM_VOLUME = 30;
	private static int DEFAULT_SURROUND_VOLUME = 30;
	private static int DEFAULT_SUBWOOFER_VOLUME = 20;

	private String mSoundbarID = null;
	private IoTPlayer mSoundbar = null;
	private TextView mInstructionTextView = null;
	private OnMoreInfoClickedListener mOnMoreInfoClickedListener = null;

	private LinearLayout mSubwooferLayout = null;
	private ImageView mSubwooferVolumeLowImage = null;
	private ImageView mSubwooferVolumeHighImage = null;
	private TextView mSubwooferText = null;
	private SeekBar mSubwooferSeekbar = null;

	private LinearLayout mLeftSurroundLayout = null;
	private ImageView mLeftSurroundVolumeLowImage = null;
	private ImageView mLeftSurroundVolumeHighImage = null;
	private TextView mLeftSurroundText = null;
	private SeekBar mLeftSurroundSeekbar = null;

	private VolumeSliderLockButton mLockButton = null;

	private LinearLayout mRightSurroundLayout = null;
	private ImageView mRightSurroundVolumeLowImage = null;
	private ImageView mRightSurroundVolumeHighImage = null;
	private TextView mRightSurroundText = null;
	private SeekBar mRightSurroundSeekbar = null;

	private TextView mSystemText = null;
	private SeekBar mSystemSeekbar = null;

	private SwitchCompat mTestSwith = null;

	private Handler mHandler = null;
	private Runnable mUpdateLockRunnable = new UpdateLockRunnable();
	private final int UPDATE_LOCK_DELAY = 1500;

	private final int PERCENT_VALUE = 100;

	private static final String DIALOG_CHANNEL_NOT_FOUND_TAG = "DialogChannelNotFoundTag";
	private static final String DIALOG_REMOVE_GROUP_TAG = "DialogRemoveGroupTag";
	private static final String DIALOG_UNINTERRUPTIBLE_SOUNDBAR_TAG = "DialogUninterruptibleSoundbarTag";

	private Boolean mTestStarted = false;

	private PlayPinkNoiseTask mPlayPinkNoiseTask = null;
	private RemoveGroupTask mRemoveGroupTask = null;
	private StopPinkNoiseTask mStopPinkNoiseTask = null;
	private UsingDefaultSettingsTask mUsingDefaultSettingsTask = null;

	private String mInput = null;

	@Override
	public void onPlayerInputSelectorChanged(IoTPlayer player, InputSelectorAttr attr) {
		final IoTPlayer soundbar  = getSoundbar();
		if (player.equals(soundbar) && isTestStarted()) {
			checkPlayerInputSelectorInUiThread();
		}
	}

	private enum LastTouched {
		NONE,
		LEFT,
		RIGHT
	}
	private LastTouched mLastTouched = LastTouched.NONE;

	public static AdjustAudioFragment newInstance(final String tag, final String soundbarID, final boolean usePreviousSettings) {
		AdjustAudioFragment fragment = new AdjustAudioFragment();
		Bundle args = new Bundle();
		args.putString(SetupFragment.SETUP_TAG_KEY, tag);
		args.putString(MultichannelSetupActivity.SOUNDBAR_ID_KEY, soundbarID);
		args.putInt(MultichannelSetupActivity.PREVIOUSLY_KNOWN_KEY, (usePreviousSettings) ? 1 : 0);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach (Context context) {
		super.onAttach(context);
		mSoundbarID = getArguments().getString(MultichannelSetupActivity.SOUNDBAR_ID_KEY);
		mSoundbar = mAllPlayManager.getPlayer(mSoundbarID);
		mUsePreviousSettings = (getArguments().getInt(MultichannelSetupActivity.PREVIOUSLY_KNOWN_KEY) == 1);
		mOnMoreInfoClickedListener = (OnMoreInfoClickedListener)context;
		mHandler = new Handler();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View frameView = inflater.inflate(R.layout.frame_adjust_audio, mFrameLayout, true);

		mInstructionTextView = (TextView)frameView.findViewById(R.id.setup_instruction_text);

		final String instruction = getString(R.string.set_volume_level_instruction);
		final String moreInfo = getString(R.string.more_info);
		final String completeText = instruction + moreInfo;

		Spannable sb = new SpannableString(completeText);
		sb.setSpan(new CustomTextAppearanceSpan(getContext(), R.style.SetupAdjustAudioMoreInfoText), completeText.indexOf(moreInfo),
				completeText.indexOf(moreInfo) + moreInfo.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sb.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View view) {
				if (mOnMoreInfoClickedListener != null) {
					mOnMoreInfoClickedListener.onMoreInfoClicked(mTag);
				}
			}

			@Override
			public void updateDrawState(TextPaint ds) {}
		}, completeText.indexOf(moreInfo), completeText.indexOf(moreInfo) + moreInfo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		mInstructionTextView.setText(sb);
		mInstructionTextView.setMovementMethod(LinkMovementMethod.getInstance());

		mActionBarTitleTextView.setText(getString(R.string.set_volume_levels));

		mSubwooferLayout = (LinearLayout)frameView.findViewById(R.id.layout_subwoofer_volume);
		mSubwooferText = (TextView) mSubwooferLayout.findViewById(R.id.channel_volume_text);
		mSubwooferSeekbar = (SeekBar) mSubwooferLayout.findViewById(R.id.channel_volume_seekbar);
		mSubwooferVolumeLowImage = (ImageView) mSubwooferLayout.findViewById(R.id.channel_volume_ic_low);
		mSubwooferVolumeHighImage = (ImageView) mSubwooferLayout.findViewById(R.id.channel_volume_ic_high);
		ImageButton imageButton = (ImageButton) mSubwooferLayout.findViewById(R.id.channel_volume_lock_button);
		imageButton.setVisibility(View.GONE);
		mSubwooferSeekbar.setOnSeekBarChangeListener(this);

		mLeftSurroundLayout = (LinearLayout)frameView.findViewById(R.id.layout_left_surround_volume);
		mLeftSurroundText = (TextView) mLeftSurroundLayout.findViewById(R.id.channel_volume_text);
		mLeftSurroundSeekbar = (SeekBar) mLeftSurroundLayout.findViewById(R.id.channel_volume_seekbar);
		mLeftSurroundVolumeLowImage = (ImageView) mLeftSurroundLayout.findViewById(R.id.channel_volume_ic_low);
		mLeftSurroundVolumeHighImage = (ImageView) mLeftSurroundLayout.findViewById(R.id.channel_volume_ic_high);
		imageButton = (ImageButton) mLeftSurroundLayout.findViewById(R.id.channel_volume_lock_button);
		imageButton.setVisibility(View.INVISIBLE);
		mLeftSurroundSeekbar.setOnSeekBarChangeListener(this);

		mRightSurroundLayout = (LinearLayout)frameView.findViewById(R.id.layout_right_surround_volume);
		mRightSurroundText = (TextView) mRightSurroundLayout.findViewById(R.id.channel_volume_text);
		mRightSurroundSeekbar = (SeekBar) mRightSurroundLayout.findViewById(R.id.channel_volume_seekbar);
		mRightSurroundVolumeLowImage = (ImageView) mRightSurroundLayout.findViewById(R.id.channel_volume_ic_low);
		mRightSurroundVolumeHighImage = (ImageView) mRightSurroundLayout.findViewById(R.id.channel_volume_ic_high);
		mLockButton = (VolumeSliderLockButton) mRightSurroundLayout.findViewById(R.id.channel_volume_lock_button);
		mLockButton.setOnClickListener(this);
		mRightSurroundSeekbar.setOnSeekBarChangeListener(this);

		LinearLayout layout = (LinearLayout)frameView.findViewById(R.id.layout_system_volume);
		mSystemText = (TextView) layout.findViewById(R.id.channel_volume_text);
		mSystemSeekbar = (SeekBar) layout.findViewById(R.id.channel_volume_seekbar);
		imageButton = (ImageButton) layout.findViewById(R.id.channel_volume_lock_button);
		imageButton.setVisibility(View.INVISIBLE);
		mSystemSeekbar.setOnSeekBarChangeListener(this);

		mTestSwith = (SwitchCompat)frameView.findViewById(R.id.setup_test_loop_switch);
		mTestSwith.setOnCheckedChangeListener(this);

		setOneButtonSetup();

		mBottomButton.setText(getString(R.string.next));

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mApp.isInit()) {
			mAllPlayManager.addOnHomeTheaterChannelChangedListener(this);
			mAllPlayManager.addOnZoneListChangedListener(this);
			mAllPlayManager.addOnPlayerVolumeChangedListener(this);
			mAllPlayManager.addOnPlayerInputSelectorChangedListener(this);
			mAllPlayManager.addOnZoneStateChangedListener(this);
		}
		updateState();
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mApp.isInit()) {
			mAllPlayManager.removeOnHomeTheaterChannelChangedListener(this);
			mAllPlayManager.removeOnZoneListChangedListener(this);
			mAllPlayManager.removeOnPlayerVolumeChangedListener(this);
			mAllPlayManager.removeOnPlayerInputSelectorChangedListener(this);
			mAllPlayManager.removeOnZoneStateChangedListener(this);
		}

		if (isTestStarted()) {
			mTestSwith.setChecked(false);
		}
	}


	protected void updateState() {
		final IoTPlayer soundbar = getSoundbar();
		if (soundbar == null) {
			return;
		}

		boolean subwooferConnected = soundbar.isHomeTheaterChannelPlayerInfoAvailable(
        HomeTheaterChannel.SUBWOOFER);
		boolean haveSubwoofer = soundbar.haveHomeTheaterChannel(HomeTheaterChannel.SUBWOOFER);
		boolean leftSurroundConnected = soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_SURROUND);
		boolean rightSurroundConnected = soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_SURROUND);
		boolean haveLeftSurround = soundbar.haveHomeTheaterChannel(HomeTheaterChannel.LEFT_SURROUND);
		boolean haveRightSurround = soundbar.haveHomeTheaterChannel(HomeTheaterChannel.RIGHT_SURROUND);

		mSubwooferLayout.setVisibility(haveSubwoofer ? View.VISIBLE : View.GONE);
		int percentProgress = 0;
		if (subwooferConnected) {
			percentProgress = (mFirstTime && !mUsePreviousSettings) ? DEFAULT_SUBWOOFER_VOLUME :
					getPercentProgress(soundbar.getHomeTheaterChannelVolume(HomeTheaterChannel.SUBWOOFER),
							soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.SUBWOOFER));
		}
		updateChannelAlpha(HomeTheaterChannel.SUBWOOFER, subwooferConnected);
		updateChannelSeekbarAndText(HomeTheaterChannel.SUBWOOFER, percentProgress);


		percentProgress = 0;
		if (leftSurroundConnected) {
			percentProgress = (mFirstTime && !mUsePreviousSettings) ? DEFAULT_SURROUND_VOLUME :
					getPercentProgress(soundbar.getHomeTheaterChannelVolume(HomeTheaterChannel.LEFT_SURROUND),
							soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.LEFT_SURROUND));
		}
		updateChannelAlpha(HomeTheaterChannel.LEFT_SURROUND, leftSurroundConnected);
		updateChannelSeekbarAndText(HomeTheaterChannel.LEFT_SURROUND, percentProgress);

		percentProgress = 0;
		if (rightSurroundConnected) {
			percentProgress = (mFirstTime && !mUsePreviousSettings) ? DEFAULT_SURROUND_VOLUME :
					getPercentProgress(soundbar.getHomeTheaterChannelVolume(HomeTheaterChannel.RIGHT_SURROUND),
							soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.RIGHT_SURROUND));

		}
		updateChannelAlpha(HomeTheaterChannel.RIGHT_SURROUND, rightSurroundConnected);
		updateChannelSeekbarAndText(HomeTheaterChannel.RIGHT_SURROUND, percentProgress);
		mLockButton.setLocked((rightSurroundConnected && leftSurroundConnected) ?
				(mLeftSurroundSeekbar.getProgress() == mRightSurroundSeekbar.getProgress()) : false);
		mLockButton.setEnabled((rightSurroundConnected && leftSurroundConnected));
		mLockButton.setAlpha((rightSurroundConnected && leftSurroundConnected) ? 1f : 0.5f);

		mLeftSurroundLayout.setVisibility((haveLeftSurround || haveRightSurround) ? View.VISIBLE : View.GONE);
		mRightSurroundLayout.setVisibility((haveLeftSurround || haveRightSurround) ? View.VISIBLE : View.GONE);

		percentProgress = (mFirstTime && !mUsePreviousSettings) ? DEFAULT_SURROUND_SYSTEM_VOLUME :
				getPercentProgress(soundbar.getVolume(), soundbar.getMaxVolume());
		updateSystemSeekbarAndText(percentProgress);

		if (showChannelNotFound(haveSubwoofer, subwooferConnected,
				haveLeftSurround, leftSurroundConnected,
				haveRightSurround, rightSurroundConnected)) {

		} else if (mFirstTime && !mUsePreviousSettings) {
			mFirstTime = false;
			useDefaultSettings();
		}
	}

	private boolean showChannelNotFound(final boolean haveSubwoofer, final boolean subwooferConnected,
								final boolean haveLeftSurround, final boolean leftSurroundConnected,
								final boolean haveRightSurround, final boolean rightSurroundConnected) {
		final IoTPlayer soundbar = getSoundbar();
		if (soundbar == null) {
			return false;
		}
		List<String> names = new ArrayList<>();
		if (haveSubwoofer && !subwooferConnected) {
			names.add(getString(R.string.subwoofer));
		}
		if (haveLeftSurround && !leftSurroundConnected) {
			names.add(getString(R.string.left_surround));
		}
		if (haveRightSurround && !rightSurroundConnected) {
			names.add(getString(R.string.right_surround));
		}

		if (names.isEmpty()) {
			mBaseActivity.dismissDialog(DIALOG_CHANNEL_NOT_FOUND_TAG);
		} else {
			String text = new String();
			for (int i = 0; i < names.size(); i++) {
				text += names.get(i);
				if (i < names.size() - 1) {
					text += ",";
				}
			}
			final String message = getString(R.string.channel_not_found,
					((names.size() > 1) ? getString(R.string.channel_speakers) : getString(R.string.channel_speaker)));
			final String toContinue = getString(R.string.do_you_want_to_continue);
			final String completeText = message + text + toContinue;
			SpannableString sb = new SpannableString(completeText);
			sb.setSpan(new CustomTextAppearanceSpan(getContext(), R.style.CustomDialogSpecialMessageTextView),
					completeText.indexOf(text),
					completeText.indexOf(text) + text.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			CustomDialogFragment spannableStringFragment = CustomDialogFragment.newSpannableStringDialog(DIALOG_CHANNEL_NOT_FOUND_TAG, null, sb,
					getString(R.string.to_continue), getString(R.string.exit_setup));

			spannableStringFragment.setButtonClickedListener(this);
			mBaseActivity.showDialog(spannableStringFragment, DIALOG_CHANNEL_NOT_FOUND_TAG);

			return true;
		}
		return false;
	}

	private int getPercentProgress(int volume, int maxVolume) {
		return (int)(((double)volume / maxVolume) * PERCENT_VALUE);
	}

	private int getVolume(int percentVolume, int maxVolume) {
		return (int)(((double)percentVolume / PERCENT_VALUE) * maxVolume);
	}

	private boolean channelSeekbarsInTouchMode() {
		return (mSubwooferSeekbar.isInTouchMode() || mLeftSurroundSeekbar.isInTouchMode() || mRightSurroundSeekbar.isInTouchMode());
	}

	private boolean channelSeekbarInTouchMode(final HomeTheaterChannel channel) {
		switch (channel) {
			case LEFT_SURROUND:
				return mLeftSurroundSeekbar.isInTouchMode();
			case RIGHT_SURROUND:
				return mRightSurroundSeekbar.isInTouchMode();
			case SUBWOOFER:
			default:
				return mSubwooferSeekbar.isInTouchMode();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void updateChannelAlpha(final HomeTheaterChannel channel, final boolean enabled) {
		switch (channel) {
			case LEFT_SURROUND:
				mLeftSurroundSeekbar.setEnabled(enabled);
				mLeftSurroundText.setEnabled(enabled);
				mLeftSurroundVolumeHighImage.setAlpha(enabled ? 1f : 0.5f);
				mLeftSurroundVolumeLowImage.setAlpha(enabled ? 1f : 0.5f);
				mLeftSurroundSeekbar.setAlpha(enabled ? 1f : 0.5f);
				break;
			case RIGHT_SURROUND:
				mRightSurroundSeekbar.setEnabled(enabled);
				mRightSurroundText.setEnabled(enabled);
				mRightSurroundVolumeHighImage.setAlpha(enabled ? 1f : 0.5f);
				mRightSurroundVolumeLowImage.setAlpha(enabled ? 1f : 0.5f);
				mRightSurroundSeekbar.setAlpha(enabled ? 1f : 0.5f);
				break;
			case SUBWOOFER:
				mSubwooferSeekbar.setEnabled(enabled);
				mSubwooferText.setEnabled(enabled);
				mSubwooferVolumeHighImage.setAlpha(enabled ? 1f : 0.5f);
				mSubwooferVolumeLowImage.setAlpha(enabled ? 1f : 0.5f);
				mSubwooferSeekbar.setAlpha(enabled ? 1f : 0.5f);
			default:
				break;
		}
	}

	private void startUpdateLockRunnable() {
		mHandler.removeCallbacks(mUpdateLockRunnable);
		mHandler.postDelayed(mUpdateLockRunnable, UPDATE_LOCK_DELAY);
	}

	private class UpdateLockRunnable implements Runnable {
		@Override
		public void run() {
			mLockButton.setLocked(mLeftSurroundSeekbar.getProgress() == mRightSurroundSeekbar.getProgress());
		}
	}

	private IoTPlayer getSoundbar() {
		synchronized (this) {
			return mSoundbar;
		}
	}

	private boolean isTestStarted() {
		synchronized (mTestStarted) {
			return mTestStarted.booleanValue();
		}
	}

	private void setTestStarted(final boolean started) {
		synchronized (mTestStarted) {
			mTestStarted = started;
		}
	}

	private String getInput() {
		synchronized (this) {
			return mInput;
		}
	}

	private void setInput(final String input) {
		synchronized (this) {
			mInput = input;
		}
	}

	private void resetTest() {
		setTestStarted(false);
		setInput(null);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void updateChannelSeekbarAndText(final HomeTheaterChannel channel, final int volume) {
		switch (channel) {
			case LEFT_SURROUND:
				updateText(mLeftSurroundText, getString(R.string.left_surround_volume, volume) +
						getString(R.string.percentage_symbol));
				mLeftSurroundSeekbar.setProgress(volume);
				break;
			case RIGHT_SURROUND:
				updateText(mRightSurroundText, getString(R.string.right_surround_volume, volume) +
						getString(R.string.percentage_symbol));
				mRightSurroundSeekbar.setProgress(volume);
				break;
			case SUBWOOFER:
			default:
				updateText(mSubwooferText, getString(R.string.subwoofer_volume, volume) +
						getString(R.string.percentage_symbol));
				mSubwooferSeekbar.setProgress(volume);
				break;
		}
	}

	private void updateSystemSeekbarAndText(final int volume) {
		updateText(mSystemText, getString(R.string.surround_system_volume, volume) + getString(R.string.percentage_symbol));
		mSystemSeekbar.setProgress(volume);
	}

	private void updateText(final TextView textView, final String text) {
		Spannable sb = new SpannableString(text);
		sb.setSpan(new CustomTextAppearanceSpan(getContext(), R.style.ChannelVolumePercentageText), text.indexOf('-') + 1,
				text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (textView != null) {
			textView.setText(sb);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
		if (compoundButton.getId() != R.id.setup_test_loop_switch) {
			return;
		}
		if (checked) {
			final IoTGroup zone = mAllPlayManager.getZoneFromPlayerID(mSoundbarID);
			if (zone != null) {
				if (zone.getPlayers().size() > 1) {
					showRemoveGroup();
				} else {
					playPinkNoise();
				}
			}
		} else if (isTestStarted()) {
			stopPinkNoise();
		}
	}

	private void showRemoveGroup() {
		CustomDialogFragment removeGroupFragment = CustomDialogFragment.newDialog(DIALOG_REMOVE_GROUP_TAG, null,
				getString(R.string.remove_group), getString(R.string.to_continue), getString(R.string.cancel));

		removeGroupFragment.setButtonClickedListener(this);

		mBaseActivity.showDialog(removeGroupFragment, DIALOG_REMOVE_GROUP_TAG);
	}

	private void showUnInterruptibleSoundbar() {
		CustomDialogFragment uninterruptibleSoundbarFragment = CustomDialogFragment.newDialog(DIALOG_UNINTERRUPTIBLE_SOUNDBAR_TAG, null,
				getString(R.string.uninterruptible_soundbar), getString(R.string.to_continue), null);

		uninterruptibleSoundbarFragment.setButtonClickedListener(this);

		mBaseActivity.showDialog(uninterruptibleSoundbarFragment, DIALOG_UNINTERRUPTIBLE_SOUNDBAR_TAG);
	}

	private void removeGroup() {
		if (mRemoveGroupTask == null) {
			mRemoveGroupTask = new RemoveGroupTask();
			mBaseActivity.addTaskToQueue(mRemoveGroupTask);
		}
	}

	private void playPinkNoise() {
		if (mPlayPinkNoiseTask == null) {
			mPlayPinkNoiseTask = new PlayPinkNoiseTask();
			mBaseActivity.addTaskToQueue(mPlayPinkNoiseTask);
		}
	}

	private void stopPinkNoise() {
		if (mStopPinkNoiseTask == null) {
			mStopPinkNoiseTask = new StopPinkNoiseTask();
			mBaseActivity.addTaskToQueue(mStopPinkNoiseTask);
		}
	}

	private void useDefaultSettings() {
		if (mUsingDefaultSettingsTask == null) {
			mUsingDefaultSettingsTask = new UsingDefaultSettingsTask();
			mBaseActivity.addTaskToQueue(mUsingDefaultSettingsTask);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.setup_bottom_button:
				if (isTestStarted()) {
					mTestSwith.setChecked(false);
				}
				onBottomButtonClicked();
				break;
			case R.id.channel_volume_lock_button:
				final IoTPlayer soundbar = getSoundbar();
				if (soundbar == null) {
					return;
				}

				if (mLockButton.isLocked()) {
					switch (mLastTouched) {
						case LEFT:
							updateChannelSeekbarAndText(HomeTheaterChannel.RIGHT_SURROUND, mLeftSurroundSeekbar.getProgress());
							soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.RIGHT_SURROUND,
									getVolume(mRightSurroundSeekbar.getProgress(),
											soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.RIGHT_SURROUND)));
							break;
						case RIGHT:
							updateChannelSeekbarAndText(HomeTheaterChannel.LEFT_SURROUND, mRightSurroundSeekbar.getProgress());
							soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.LEFT_SURROUND,
									getVolume(mLeftSurroundSeekbar.getProgress(),
											soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.LEFT_SURROUND)));
							break;
						default:
						case NONE:
							int min = Math.min(mLeftSurroundSeekbar.getProgress(), mRightSurroundSeekbar.getProgress());
							updateChannelSeekbarAndText(HomeTheaterChannel.RIGHT_SURROUND, min);
							updateChannelSeekbarAndText(HomeTheaterChannel.LEFT_SURROUND, min);
							soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.RIGHT_SURROUND,
									getVolume(min, soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.RIGHT_SURROUND)));
							soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.LEFT_SURROUND,
									getVolume(min, soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.LEFT_SURROUND)));
							break;
					}
				}
				break;
			default:
				super.onClick(view);
				break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean user) {
		if (!user) {
			return;
		}
		final IoTPlayer soundbar = getSoundbar();
		if (soundbar == null) {
			return;
		}
		if (seekBar == mLeftSurroundSeekbar) {
			mLastTouched = LastTouched.LEFT;
			if (mLockButton.isLocked()) {
				updateChannelSeekbarAndText(HomeTheaterChannel.RIGHT_SURROUND, progress);
				soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.RIGHT_SURROUND,
						getVolume(progress, soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.RIGHT_SURROUND)));
			}
			int leftVolume = getVolume(progress, soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.LEFT_SURROUND));
			updateText(mLeftSurroundText, getString(R.string.left_surround_volume, leftVolume) +
					getString(R.string.percentage_symbol));
			soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.LEFT_SURROUND, leftVolume);
		} else if (seekBar == mRightSurroundSeekbar) {
			mLastTouched = LastTouched.RIGHT;
			if (mLockButton.isLocked()) {
				updateChannelSeekbarAndText(HomeTheaterChannel.LEFT_SURROUND, progress);
				soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.LEFT_SURROUND,
						getVolume(progress, soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.LEFT_SURROUND)));
			}
			int rightVolume = getVolume(progress, soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.RIGHT_SURROUND));
			updateText(mRightSurroundText, getString(R.string.right_surround_volume, rightVolume) +
					getString(R.string.percentage_symbol));
			soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.RIGHT_SURROUND, rightVolume);
		} else if (seekBar == mSubwooferSeekbar) {
			updateChannelSeekbarAndText(HomeTheaterChannel.SUBWOOFER, progress);
			soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.SUBWOOFER,
					getVolume(progress, soundbar.getHomeTheaterChannelMaxVolume(HomeTheaterChannel.SUBWOOFER)));
		} else if (seekBar == mSystemSeekbar) {
			updateSystemSeekbarAndText(progress);
			soundbar.setVolume(getVolume(progress, soundbar.getMaxVolume()), success -> {});
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onPositiveButtonClicked(String tag) {
		if (DIALOG_REMOVE_GROUP_TAG.equals(tag)) {
			removeGroup();
		} else if (DIALOG_CHANNEL_NOT_FOUND_TAG.equals(tag)) {
			if (mFirstTime && !mUsePreviousSettings) {
				useDefaultSettings();
				mFirstTime = false;
			}
		}
	}

	@Override
	public void onNegativeButtonClicked(String tag) {
		if (DIALOG_CHANNEL_NOT_FOUND_TAG.equals(tag) ||
				BaseActivity.DIALOG_COMMUNICATION_PROBLEM_TAG.equals(tag)) {
			if (mSetupFragmentListener != null) {
				mSetupFragmentListener.onMiddleButtonClicked(mTag);
			}
		} else if (DIALOG_REMOVE_GROUP_TAG.equals(tag)) {
			mTestSwith.setChecked(false);
		}
	}

	@Override
	public void onZoneListChanged() {
		final boolean needUpdate;
		final IoTPlayer soundbar;
		synchronized (this) {
			soundbar = mSoundbar;
			mSoundbar = mAllPlayManager.getPlayer(mSoundbarID);
			needUpdate = (soundbar != mSoundbar);
		}
		if (needUpdate) {
			updateInUiThread();
		}
		if (isTestStarted()) {
			checkZoneListInUiThread();
		}
	}

	private void checkZoneListInUiThread() {
		if (Utils.isActivityActive(mBaseActivity)) {
			mBaseActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final IoTGroup soundbarZone = mAllPlayManager.getZoneFromPlayerID(mSoundbarID);
					final IoTPlayer soundbar = mAllPlayManager.getPlayer(mSoundbarID);
					boolean resetTest = true;
					if ((soundbarZone != null) && (soundbar != null)) {
						if (isTestStarted()) {
							resetTest = (soundbarZone.getPlayers().size() > 1);
						}
					}

					if (resetTest) {
						mTestSwith.setChecked(false);
					}
				}
			});
		}
	}

	@Override
	public void onGroupInfoStateChanged() {
		final IoTGroup soundbar = mAllPlayManager.getZoneFromPlayerID(mSoundbarID);
		//TODO check zone
		/*if (zone.equals(soundbar) && isTestStarted()) {
			checkPlayerStateInUiThread();
		}*/
	}

	private void checkPlayerStateInUiThread() {
		if (Utils.isActivityActive(mBaseActivity)) {
			mBaseActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (!isTestStarted()) {
						return;
					}
					boolean resetTest = true;
					final IoTGroup soundbarZone = mAllPlayManager.getZoneFromPlayerID(mSoundbarID);
					final IoTPlayer soundbar = mAllPlayManager.getPlayer(mSoundbarID);
					if ((soundbarZone != null) && (soundbar != null)) {

            final MediaItem currentItem = soundbarZone.getCurrentItem();
            if ((currentItem != null) && (currentItem.getStreamUrl() != null)) {
              String url =
                  getAudioURL(soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.SUBWOOFER));
              final PlayState playerState = soundbarZone.getPlayerState();
              if (currentItem.getStreamUrl().equals(url) &&
                  (playerState != PlayState.kStopped) && (playerState != PlayState.kPaused)) {
                resetTest = false;
              }

						}
					}

					if (resetTest) {
						resetTest();
						mTestSwith.setChecked(false);
					}
				}
			});
		}
	}



	private void checkPlayerInputSelectorInUiThread() {
		final IoTPlayer soundbar  = getSoundbar();
		if (Utils.isActivityActive(mBaseActivity)) {
			mBaseActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (isTestStarted() && !Utils.isStringEmpty(soundbar.getActiveInputSource())) {
						mTestSwith.setChecked(false);
					}
				}
			});
		}
	}

	@Override
	public void onHomeTheaterChannelPlayerInfoAvailable(IoTPlayer player, HomeTheaterChannel channel, boolean available) {
		final IoTPlayer soundbar = getSoundbar();
		if (!player.equals(soundbar)) {
			return;
		}
		updateInUiThread();
	}

	@Override
	public void onHomeTheaterChannelDeviceInfoAvailable(IoTPlayer player, HomeTheaterChannel channel, boolean available) {}

	@Override
	public void onHomeTheaterChannelVolumeChanged(final IoTPlayer player, final HomeTheaterChannel channel, final int volume,
												  final boolean user) {
		final IoTPlayer soundbar = getSoundbar();
		if (!player.equals(soundbar)) {
			return;
		}
		if (user || (!mSystemSeekbar.isInTouchMode() && !channelSeekbarInTouchMode(channel))) {
			final boolean updateLock = (channel != HomeTheaterChannel.SUBWOOFER);
			if (Utils.isActivityActive(mBaseActivity)) {
				mBaseActivity.runOnUiThread(() -> {
          if (Utils.isActivityActive(mBaseActivity)) {
            updateChannelSeekbarAndText(channel, getPercentProgress(volume,
                player.getHomeTheaterChannelMaxVolume(channel)));
            if (updateLock && (!mSystemSeekbar.isInTouchMode() && !channelSeekbarsInTouchMode())) {
              startUpdateLockRunnable();
            }
          }
        });
			}
		}
	}

  @Override
  public void onHomeTheaterChannelUpdate(IoTPlayer player, HomeTheaterChannelMap channelMap) {

  }

  @Override
	public void onPlayerVolumeStateChanged(final IoTPlayer player, final int volume, boolean user) {
		final IoTPlayer soundbar = getSoundbar();
		if (!player.equals(soundbar)) {
			return;
		}

		if (user || (!mSystemSeekbar.isInTouchMode() && !channelSeekbarsInTouchMode())) {
			if (Utils.isActivityActive(mBaseActivity)) {
				mBaseActivity.runOnUiThread(() -> {
          if (Utils.isActivityActive(mBaseActivity)) {
            updateSystemSeekbarAndText(getPercentProgress(volume, player.getMaxVolume()));
          }
        });
			}
		}
	}

	@Override
	public void onPlayerVolumeEnabledChanged(IoTPlayer player, boolean enabled) {

	}

	@Override
	public void onPlayerMuteStateChanged(IoTPlayer player, boolean muted) {

	}

	private String getAudioURL(final boolean haveSub) {
		String ipAddress = mApp.getConnectivityReceiver().getLocalIPAddress(getContext());
		if (ipAddress == null) {
			return null;
		}
		return HttpServer.buildHttpUrl(
				(haveSub) ? HttpServer.TYPE_TEST_LS_LFE_LSRS : HttpServer.TYPE_TEST_LS_LSRS,
				ipAddress);
	}

	private class UsingDefaultSettingsTask extends RequestAsyncTask {
		public UsingDefaultSettingsTask() {
			super(getString(R.string.using_default_settings), null, mBaseActivity, null);
		}

		@Override
		protected Void doInBackground(Void... voids) {
			final IoTPlayer soundbar = getSoundbar();
			if (soundbar == null || !soundbar.isVolumeEnabled()) {
				return null;
			}
			boolean subwooferConnected = soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.SUBWOOFER);
			boolean leftSurroundConnected = soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_SURROUND);
			boolean rightSurroundConnected = soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_SURROUND);

			soundbar.setVolume(DEFAULT_SURROUND_SYSTEM_VOLUME, success -> {});
			quickWait();

			if (subwooferConnected) {
				soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.SUBWOOFER, DEFAULT_SUBWOOFER_VOLUME);
				quickWait();
			}
			if (leftSurroundConnected) {
				soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.LEFT_SURROUND, DEFAULT_SURROUND_VOLUME);
				quickWait();
			}
			if (rightSurroundConnected) {
				soundbar.setHomeTheaterChannelVolume(HomeTheaterChannel.RIGHT_SURROUND, DEFAULT_SURROUND_VOLUME);
				quickWait();
			}
			return null;
		}

		private void quickWait() {
			synchronized (this) {
				try {
					wait(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onPostExecute(final Void param) {
			mUsingDefaultSettingsTask = null;
			super.onPostExecute(param);
		}
	}

	private class RemoveGroupTask extends RequestAsyncTask {

		public RemoveGroupTask() {
			super(getString(R.string.removing_group), null, mBaseActivity, null);
			mListener = new RequestListener() {
				@Override
				public void onRequestSuccess() {
					playPinkNoise();
				}

				@Override
				public void onRequestFailed() {
				}
			};
		}

		@Override
		protected Void doInBackground(Void... voids) {
			final IoTGroup soundbarZone = mAllPlayManager.getZoneFromPlayerID(mSoundbarID);
			if (soundbarZone == null) {
				mResult = false;
				return null;
			}

			final List<IoTPlayer> players = soundbarZone.getPlayers();
			if (players.size() > 1) {
				players.remove(getSoundbar());
				final Set<IoTPlayer> playersSet = new HashSet<>();
				playersSet.addAll(players);

				mResult = mAllPlayManager.manipulateZone(soundbarZone, playersSet);
			}
			doWait(DEFAULT_WAIT_TIME);
			return null;
		}

		@Override
		protected void onPostExecute(final Void param) {
			mRemoveGroupTask = null;
			super.onPostExecute(param);
		}
	}

	private class PlayPinkNoiseTask extends BaseAsyncTask {
		public PlayPinkNoiseTask() {
			super(mBaseActivity, null);
			mListener = new RequestListener() {
				@Override
				public void onRequestSuccess() {
				}

				@Override
				public void onRequestFailed() {
					mTestSwith.setChecked(false);
					final IoTPlayer soundbar = getSoundbar();
					if (soundbar != null) {
						// Pop error for play issues
						if (!soundbar.isInterruptible()) {
							showUnInterruptibleSoundbar();
						} else {
							mBaseActivity.showCommunicationProblem(AdjustAudioFragment.this);
						}
					}
				}
			};
		}

		@Override
		protected Void doInBackground(Void... voids) {
			final IoTGroup soundbarZone = mAllPlayManager.getZoneFromPlayerID(mSoundbarID);
			final IoTPlayer soundbar = getSoundbar();
			if ((soundbarZone == null) || (soundbar == null) ||
					!soundbarZone.isPlayItemSupported() || !soundbar.isInterruptible()) {
				mResult = false;
				return null;
			}

			String url = getAudioURL(soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.SUBWOOFER));
			if (url == null) {
				// we may not be connected to network.
				mResult = false;
			}
			synchronized (AdjustAudioFragment.this) {
				mInput = soundbar.getActiveInputSource();
			}

			MediaItem mediaItem = new MediaItem();
			mediaItem.setTitle(getString(R.string.test_sound_loop));
			mediaItem.setStreamUrl(url);

			soundbarZone.playItem(mediaItem, success -> {
			  if(success)
			    setTestStarted(true);
			});

			return null;
		}

		@Override
		protected void onPostExecute(final Void param) {
			mPlayPinkNoiseTask = null;
			super.onPostExecute(param);
		}
	}

	private class StopPinkNoiseTask extends BaseAsyncTask {
		public StopPinkNoiseTask() {
			super(mBaseActivity);
		}

		@Override
		protected Void doInBackground(Void... voids) {
			final boolean isStarted = isTestStarted();
			final String input = getInput();
			resetTest();
			final IoTGroup soundbarZone = mAllPlayManager.getZoneFromPlayerID(mSoundbarID);
			final IoTPlayer soundbar = getSoundbar();
			if (isStarted && (soundbarZone != null) && (soundbar != null)) {
				try {
          soundbarZone.stop(null);
        } catch(OcException e) {
          e.printStackTrace();
        }
				if (!soundbar.getActiveInputSource().equalsIgnoreCase(input)) {
					soundbar.setInputSelector(input, success -> {
					});
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Void param) {
			mStopPinkNoiseTask = null;
			super.onPostExecute(param);
		}
	}

	public interface OnMoreInfoClickedListener {
		void onMoreInfoClicked(final String tag);
	}
}
