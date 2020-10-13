/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;


import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EnabledControlsAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.LoopMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.ShuffleMode;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupPlaylistChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupSelectedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupStateChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupVolumeChangedListener;
import com.qualcomm.qti.smartaudio.util.BaseAsyncTask;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.smartaudio.view.LoopModeButton;
import com.qualcomm.qti.smartaudio.view.LoopModeButton.OnLoopModeChangeListener;
import com.qualcomm.qti.smartaudio.view.NowPlayingPlayPauseButton;
import com.qualcomm.qti.smartaudio.view.ShuffleModeButton;
import com.qualcomm.qti.smartaudio.view.ShuffleModeButton.OnShuffleModeChangedListener;
import com.qualcomm.qti.smartaudio.view.TrackSeekBar;
import com.squareup.picasso.Picasso;

import org.iotivity.base.OcException;

public class NowPlayingFragment extends BaseFragment implements OnCurrentGroupSelectedListener,
        OnCurrentGroupStateChangedListener, OnCurrentGroupVolumeChangedListener,
        OnCurrentGroupPlaylistChangedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        OnLoopModeChangeListener, OnShuffleModeChangedListener {

    public static final String TAG = NowPlayingFragment.class.getSimpleName();
    private static final String DIALOG_ZONE_VOLUME_TAG = "DialogZoneVolumeTag";

    private View mEmptyView = null;
    private View mNowPlayingLayout = null;

    private ImageView mAlbumArt = null;
    private TrackSeekBar mTrackSeekBar = null;
    private TextView mTrackText = null;
    private TextView mArtistText = null;
    private TextView mAlbumText = null;
    private NowPlayingPlayPauseButton mPlayPauseButton = null;
    private ImageButton mSkipBack = null;
    private ImageButton mSkipForward = null;
    private ImageButton mVolumeSetting = null;
    private SeekBar mVolumeSeekBar = null;
    private LoopModeButton mLoopModeButton = null;
    private ShuffleModeButton mShuffleModeButton = null;

    private NextAsyncTask mNextAsyncTask = null;
    private PreviousAsyncTask mPreviousAsyncTask = null;

    private Boolean mSetLoop = false;
    private LoopMode mLoopModeTarget = LoopMode.kNone;
    private LoopAsyncTask mLoopAsyncTask = null;

    private Boolean mSetShuffle = false;

    private Boolean mSetSeek = false;
    private int mSeekTarget = 0;
    private SeekAsyncTask mSeekAsyncTask = null;

    public static NowPlayingFragment newInstance() {
        return new NowPlayingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_now_playing, container, false);

        mEmptyView = view.findViewById(R.id.empty_view_layout);
        mEmptyView.setVisibility(View.GONE);

        ImageView emptyIcon = (ImageView) view.findViewById(R.id.empty_view_icon);
        if (emptyIcon != null) {
            emptyIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                                                   R.drawable.ic_currently_playing_empty, null));
        }
        TextView emptyTextView = (TextView) view.findViewById(R.id.empty_view_text);
        if (emptyTextView != null) {
            emptyTextView.setText(R.string.empty);
        }
        TextView emptyDetailTextView = (TextView) view.findViewById(R.id.empty_view_detail_text);
        if (emptyDetailTextView != null) {
            emptyDetailTextView.setText(R.string.empty_now_playing_detail);
        }

        mNowPlayingLayout = view.findViewById(R.id.now_playing_layout);
        mAlbumArt = (ImageView) view.findViewById(R.id.now_playing_album_art);
        mTrackSeekBar = (TrackSeekBar) view.findViewById(R.id.now_playing_seekbar);
        mTrackSeekBar.setOnSeekBarChangeListener(this);
        mTrackSeekBar.setOnClickListener(this);

        mTrackText = (TextView) view.findViewById(R.id.now_playing_track_text);
        mArtistText = (TextView) view.findViewById(R.id.now_playing_artist_text);
        mAlbumText = (TextView) view.findViewById(R.id.now_playing_album_text);
        mPlayPauseButton = (NowPlayingPlayPauseButton) view.findViewById(R.id.now_playing_play_pause);
        mPlayPauseButton.setBaseActivity(mBaseActivity);

        mSkipBack = (ImageButton) view.findViewById(R.id.now_playing_skip_back);
        mSkipBack.setOnClickListener(this);
        mSkipForward = (ImageButton) view.findViewById(R.id.now_playing_skip_forward);
        mSkipForward.setOnClickListener(this);
        mVolumeSetting = (ImageButton) view.findViewById(R.id.now_playing_volume_settings);
        mVolumeSetting.setOnClickListener(this);

        mVolumeSeekBar = (SeekBar) view.findViewById(R.id.now_playing_volume_seekbar);
        mVolumeSeekBar.setOnSeekBarChangeListener(this);
        mVolumeSeekBar.setOnClickListener(this);

        mLoopModeButton = (LoopModeButton) view.findViewById(R.id.now_playing_repeat);
        mLoopModeButton.setOnLoopModeChangedListener(this);

        mShuffleModeButton = (ShuffleModeButton) view.findViewById(R.id.now_playing_shuffle);
        mShuffleModeButton.setOnShuffleModeChangedListener(this);

        return view;
    }

    @Override
    protected void updateState() {
        final IoTGroup group = mAllPlayManager.getCurrentGroup();

        if (group == null) {
            return;
        }

        updateVolume();

        updateUI();
		/*if(!group.getMediaItems(success -> getActivity().runOnUiThread(() -> updateUI()))) {
		  UiThreadExecutor.getInstance().execute(()->updateUI());
    }*/

    }

    @Override
    protected void updateUI() {

        IoTGroup group = mAllPlayManager.getCurrentGroup();
        if (group != null) {
            mPlayPauseButton.setGroup(group);
            mTrackSeekBar.setZone(group);
        }

        final MediaItem mediaItem = group.getCurrentItem();
        if (mediaItem == null) {
            mNowPlayingLayout.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            mNowPlayingLayout.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

            updateNowPlayingSeekbarWidth();

            if (Utils.isStringEmpty(mediaItem.getThumbnailUrl())) {
                mAlbumArt.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                                                       R.drawable.ic_album_art_default_large, null));
            }
            else {
                Picasso.get()
                        .load(mediaItem.getThumbnailUrl())
                        .placeholder(R.drawable.ic_album_art_default_large)
                        .error(R.drawable.ic_album_art_default_large)
                        .into(mAlbumArt);

            }
            String title = mediaItem.getTitle();
            if (Utils.isStringEmpty(title)) {
                title = getString(R.string.unknown_upper_case);
            }
            String artist = mediaItem.getArtist();
            if (Utils.isStringEmpty(artist)) {
                artist = getString(R.string.unknown_artist);
            }
            String album = mediaItem.getAlbum();
            if (Utils.isStringEmpty(album)) {
                album = getString(R.string.unknown_album);
            }
            mTrackText.setText(title.toUpperCase());
            mArtistText.setText(artist);
            mAlbumText.setText(album);
        }

        boolean enabled = group.isInterruptible();
        double alpha;

        boolean pauseEnabled = (enabled && group.isPauseEnabled());
        alpha = (pauseEnabled == true) ? 1.0 : 0.5;
        mPlayPauseButton.setEnabled(pauseEnabled);
        mPlayPauseButton.setAlpha((float) alpha);

        boolean nextEnabled = (enabled && group.isNextEnabled());
        alpha = (nextEnabled == true) ? 1.0 : 0.5;
        mSkipForward.setEnabled(nextEnabled);
        mSkipForward.setAlpha((float) alpha);

        boolean prevEnabled = (enabled && group.isPreviousEnabled());
        alpha = (prevEnabled == true) ? 1.0 : 0.5;
        mSkipBack.setEnabled(prevEnabled);
        mSkipBack.setAlpha((float) alpha);

        boolean seekEnabled = (enabled && group.isSeekEnabled());
        alpha = (seekEnabled && mediaItem != null && mediaItem.getDuration() > 0L) ? 1.0 : 0.5;

        mTrackSeekBar.setEnabled(seekEnabled && mediaItem != null && mediaItem.getDuration() > 0);
        mTrackSeekBar.setAlpha((float) alpha);

        mLoopModeButton.setLoopMode(group.getLoopMode());
        mShuffleModeButton.setShuffleMode(group.getShuffleMode());

    }

    private void updateVolume() {
        final IoTGroup group = mAllPlayManager.getCurrentGroup();
        ControllerSdkUtils.checkNotNull(group, "Group == null");
        mVolumeSeekBar.setMax(group.getMaxVolume());
        int volume = group.getVolume();
        mVolumeSeekBar.setProgress(volume);
        mVolumeSeekBar.setContentDescription(getString(R.string.cont_desc_playing_volume, group.getDisplayName(),
                                                       volume));
    }

    private void updateVolumeInUIThread() {
        if (Utils.isActivityActive(mBaseActivity)) {
            mBaseActivity.runOnUiThread(() -> {
                if (Utils.isActivityActive(mBaseActivity)) {
                    updateVolume();
                }
            });
        }
    }

    private void updateNowPlayingSeekbarWidth() {
        final int height = mAlbumArt.getHeight();
        final int width = mAlbumArt.getWidth();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTrackSeekBar.getLayoutParams();
        params.width = (height < width) ? height : width;
        params.width += (getResources().getDimension(R.dimen.now_playing_seekbar_padding) * 2);
        mTrackSeekBar.setLayoutParams(params);
        mTrackSeekBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mApp.isInit()) {
            mAllPlayManager.addOnCurrentZoneStateChangedListener(this);
            mAllPlayManager.addOnCurrentZoneSelectedListener(this);
            mAllPlayManager.addOnCurrentZoneVolumeChangedListener(this);
            mAllPlayManager.addOnCurrentZonePlaylistChangedListener(this);
        }
        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mApp.isInit()) {
            mAllPlayManager.removeOnCurrentZoneStateChangedListener(this);
            mAllPlayManager.removeOnCurrentZoneSelectedListener(this);
            mAllPlayManager.removeOnCurrentZoneVolumeChangedListener(this);
            mAllPlayManager.removeOnCurrentZonePlaylistChangedListener(this);
        }
    }

    @Override
    public void onCurrentZoneSelected(IoTGroup zone) {
        reset();
        updateInUiThread();
    }

    @Override
    public void onCurrentZoneRemoved() {
        reset();
    }

    @Override
    public void onCurrentGroupVolumeStateChanged(int volume, boolean user) {
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        if ((zone != null) && (user || !mVolumeSeekBar.isPressed())) {
            updateVolumeInUIThread();
        }
    }

    @Override
    public void onCurrentZoneVolumeEnabledChanged(boolean enabled) {
    }


    @Override
    public void onCurrentGroupEnabledControlsChanged(EnabledControlsAttr attr) {

        UiThreadExecutor.getInstance().execute(() -> {


            boolean pauseEnabled = (attr.mPause);
            double alpha = (pauseEnabled == true) ? 1.0 : 0.5;
            mPlayPauseButton.setEnabled(pauseEnabled);
            mPlayPauseButton.setAlpha((float) alpha);

            boolean nextEnabled = (attr.mNext);
            alpha = (nextEnabled == true) ? 1.0 : 0.5;
            mSkipForward.setEnabled(nextEnabled);
            mSkipForward.setAlpha((float) alpha);

            boolean prevEnabled = (attr.mPrevious);
            alpha = (prevEnabled == true) ? 1.0 : 0.5;
            mSkipBack.setEnabled(prevEnabled);
            mSkipBack.setAlpha((float) alpha);

            boolean seekEnabled = (attr.mSeek);

            IoTGroup group = mAllPlayManager.getCurrentGroup();
            if (group != null) {
                final MediaItem mediaItem = group.getCurrentItem();
                alpha = (seekEnabled && mediaItem != null && mediaItem.getDuration() > 0) ? 1.0 : 0.5;
                mTrackSeekBar.setEnabled(seekEnabled && mediaItem != null && mediaItem.getDuration() > 0);
                mTrackSeekBar.setAlpha((float) alpha);
            }
        });

    }

    @Override
    public void onCurrentZoneMuteStateChanged(boolean muted) {
    }

    @Override
    public void OnCurrentGroupStateChanged() {
        updateInUiThread();
    }

    @Override
    public void onCurrentZonePlaylistChanged() {
        updateInUiThread();
    }

    @Override
    public void onCurrentGroupLoopModeChanged(LoopMode loopMode) {
        UiThreadExecutor.getInstance().execute(() -> mLoopModeButton.setLoopMode(loopMode));
    }

    @Override
    public void onCurrentGroupShuffleModeChanged(ShuffleMode shuffleMode) {
        UiThreadExecutor.getInstance().execute(() -> mShuffleModeButton.setShuffleMode(shuffleMode));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.now_playing_skip_back:
                previous();
                break;
            case R.id.now_playing_skip_forward:
                next();
                break;
            case R.id.now_playing_volume_settings:
                showZoneVolume();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (seekBar.getId() == mVolumeSeekBar.getId()) {
                //setVolume(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getId() == mVolumeSeekBar.getId()) {
            setVolume(seekBar.getProgress());
        }
        else if (seekBar.getId() == mTrackSeekBar.getId()) {
            final IoTGroup group = mAllPlayManager.getCurrentGroup();
            if (group != null) {
                final MediaItem mediaItem = group.getCurrentItem();
                int duration = mediaItem.getDuration();
                Log.d(TAG, "current media item duration:" + duration);
                if ((mediaItem != null) && (duration > 0)) {
                    float progressPercentage = (float) seekBar.getProgress() / 100.0f;
                    int position = (int) ((float) duration * progressPercentage);
                    if (position == duration) {
                        next();
                    }
                    else {
                        try {
                            group.setPlayerPosition(position, status -> {

                            });
                        }
                        catch (OcException e) {
                            e.printStackTrace();

                        }
                    }
                }
            }
        }
    }

    @Override
    public void onLoopModeChanged(LoopModeButton button, LoopMode loopMode) {
        setLoopMode(loopMode);
    }

    @Override
    public void onShuffleModeChanged(ShuffleModeButton button, ShuffleMode shuffleMode) {
        setShuffleMode(shuffleMode);
    }

    private void showZoneVolume() {
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        if ((zone == null) || (zone.getPlayers().size() < 0)) {
            return;
        }
        mBaseActivity.showDialog(GroupVolumeDialogFragment.newDialog(DIALOG_ZONE_VOLUME_TAG, zone.getId()),
                                 DIALOG_ZONE_VOLUME_TAG);
    }

    private void reset() {
        if (mNextAsyncTask != null) {
            mNextAsyncTask.cancel(true);
            mNextAsyncTask = null;
        }

        if (mPreviousAsyncTask != null) {
            mPreviousAsyncTask.cancel(true);
            mPreviousAsyncTask = null;
        }

        resetLoopMode();
        resetShuffleMode();
        resetSeek();
    }

    private void resetLoopMode() {
        synchronized (mSetLoop) {
            mSetLoop = false;
            if (mLoopAsyncTask != null) {
                mLoopAsyncTask.cancel(true);
                mLoopAsyncTask = null;
            }
        }
    }

    private void resetShuffleMode() {
        synchronized (mSetShuffle) {
            mSetShuffle = false;
        }
    }

    private void resetSeek() {
        synchronized (mSetSeek) {
            mSetSeek = false;
            if (mSeekAsyncTask != null) {
                mSeekAsyncTask.cancel(true);
                mSeekAsyncTask = null;
            }
        }
    }

    private void setVolume(final int volume) {
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        if (zone == null) {
            return;
        }
        zone.setVolume(volume);
    }

    private void next() {
        final IoTGroup group = mAllPlayManager.getCurrentGroup();
        if (group == null) {
            return;
        }

        try {
            group.next(status -> {
                if (status) {
                    Log.d(TAG, "Play next track!");
                }
            });
        }
        catch (OcException e) {
            e.printStackTrace();
        }
    }

    private void previous() {
        final IoTGroup group = mAllPlayManager.getCurrentGroup();
        if (group == null) {
            return;
        }

        try {
            group.previous(status -> {
                if (status) {
                    Log.d(TAG, "Please previous track!");
                }
            });
        }
        catch (OcException e) {
            e.printStackTrace();
        }
    }

    private void setLoopMode(final LoopMode loopMode) {
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        if (zone == null) {
            return;
        }
        synchronized (mSetLoop) {
            mLoopModeTarget = loopMode;
            if (!mSetLoop) {
                mSetLoop = true;
            }
            else {
                mSetLoop = false;
            }
        }
        zone.setLoopMode(loopMode);
    }

    private void setShuffleMode(final ShuffleMode shuffleMode) {
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        if (zone == null) {
            return;
        }
        synchronized (mSetShuffle) {
            if (!mSetShuffle) {
                mSetShuffle = true;
            }
            else {
                mSetShuffle = false;
            }

            zone.setShuffleMode(shuffleMode, success -> {
                                    if (success) {
                                        Log.d(TAG, "set Shuffle mode");
                                    }
                                }
            );
        }
    }

    private class NextAsyncTask extends BaseAsyncTask {

        public NextAsyncTask() {
            super(mBaseActivity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            final IoTGroup zone = mAllPlayManager.getCurrentGroup();

            //TODO implement callback method
            try {
                zone.next(status -> {
                    if (status) {
                        Log.d(TAG, "Next");
                    }
                });
            }
            catch (OcException e) {
                e.printStackTrace();
            }
            mResult = true;
            return null;
        }

        @Override
        protected void clean() {
            mNextAsyncTask = null;
        }
    }

    private class PreviousAsyncTask extends BaseAsyncTask {

        public PreviousAsyncTask() {
            super(mBaseActivity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            final IoTGroup player = mAllPlayManager.getCurrentGroup();

            try {
                player.previous(status -> {
                    if (status) {
                        Log.d(TAG, "Previous");
                    }
                });
            }
            catch (OcException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void clean() {
            mPreviousAsyncTask = null;
        }
    }

    private class LoopAsyncTask extends BaseAsyncTask {

        final private LoopMode mLoopMode;

        public LoopAsyncTask(final LoopMode loopMode) {
            super(mBaseActivity);
            mLoopMode = loopMode;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final IoTGroup zone = mAllPlayManager.getCurrentGroup();
            if (zone != null) {
                mResult = (zone.setLoopMode(mLoopMode) == IoTError.NONE);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void param) {
            super.onPostExecute(param);
            final BaseActivity baseActivity = getActiveActivity();
            synchronized (mSetLoop) {
                if (mSetLoop) {
                    if (mLoopModeTarget == mLoopMode) {
                        mSetLoop = false;
                    }
                    else if (baseActivity != null) {
                        mLoopAsyncTask = new LoopAsyncTask(mLoopModeTarget);
                        baseActivity.addTaskToQueue(mLoopAsyncTask);
                    }
                }
            }
        }

        @Override
        protected void clean() {
            mLoopAsyncTask = null;
        }
    }

    private class SeekAsyncTask extends BaseAsyncTask {

        final private int mSeekValue;

        public SeekAsyncTask(final int seekValue) {
            super(mBaseActivity);
            mSeekValue = seekValue;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final IoTGroup zone = mAllPlayManager.getCurrentGroup();
            if ((zone != null) && mApp.startHttpService()) {
                try {
                    zone.setPlayerPosition(mSeekValue, status -> {

                    });
                }
                catch (OcException e) {
                    e.printStackTrace();

                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void param) {
            super.onPostExecute(param);
            final BaseActivity baseActivity = getActiveActivity();
            synchronized (mSetSeek) {
                if (mSetSeek) {
                    if (mSeekTarget == mSeekValue) {
                        mSetSeek = false;
                    }
                    else if (baseActivity != null) {
                        mSeekAsyncTask = new SeekAsyncTask(mSeekTarget);
                        baseActivity.addTaskToQueue(mSeekAsyncTask);
                    }
                }
            }
        }

        @Override
        protected void clean() {
            mSeekAsyncTask = null;
        }
    }
}
