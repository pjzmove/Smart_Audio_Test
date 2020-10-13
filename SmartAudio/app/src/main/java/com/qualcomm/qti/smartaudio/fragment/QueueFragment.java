/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.LoopMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.ShuffleMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.iotcontrollersdk.controller.TaskExecutors;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupPlaylistChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupSelectedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupStateChangedListener;
import com.qualcomm.qti.smartaudio.provider.local.LocalProvider;
import com.qualcomm.qti.smartaudio.service.HttpServerService;
import com.qualcomm.qti.smartaudio.util.PleaseWaitAsyncTask;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.smartaudio.view.MoreOptionsPopupWindow;
import com.qualcomm.qti.smartaudio.view.MoreOptionsPopupWindow.MoreOptionType;
import com.qualcomm.qti.smartaudio.view.PlayingAnimationView;
import com.squareup.picasso.Picasso;

import org.iotivity.base.OcException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueueFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener,
        OnCurrentGroupSelectedListener, OnCurrentGroupStateChangedListener,
        OnCurrentGroupPlaylistChangedListener {

    public static final String TAG = QueueFragment.class.getSimpleName();

    private View mEmptyView = null;
    private View mQueueView = null;
    private TextView mQueueInfoTextView = null;
    private ImageButton mQueueMoreOptionsButton = null;

    private DragSortListView mDragSortListView = null;
    private DragSortController mDragSortController = null;
    private DataAdapter mAdapter = null;
    private MediaItem mMoveItem = null;

    private boolean mUserInteraction = false;

    private int mLastFirstVisibleItem;
    private boolean mIsScrollingUp;

    private boolean mScrollToIndexPlaying = false;
    private boolean mFirstShow = true;
    private int mScrollToIndex = 0;
    private int mStartIndex = 0;
    final private int mOffset = 30;
    final private int mDownOffset = 60;
    final private int mCount = 90;
    private int mPlaylistSize = 0;

    private static final String DIALOG_CLEAR_QUEUE_TAG = "DialogClearQueueTag";
    private static final String DIALOG_WARN_PERMISSION_TAG = "DialogWarnPermissionTag";
    private static final String DIALOG_HAVE_NON_LOCAL_CONTENT_TAG = "DialogHaveNonLocalContentTag";
    private static final String DIALOG_SAVE_PLAYLIST_TAG = "DialogSavePlaylistTag";
    private static final String DIALOG_REPLACE_PLAYLIST_TAG = "DialogReplacePlaylistTag";


    private DragSortListView.DropListener mOnDropListener = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            if ((from != to) && (mAdapter != null)) {
                mAdapter.moveItem(from, to);
                mMoveItem = null;
            }
        }
    };

    private DragSortListView.DragListener mOnDragListener = new DragSortListView.DragListener() {
        @Override
        public void drag(int from, int to) {
            if (mMoveItem == null) {
                mMoveItem = (MediaItem) mAdapter.getItem(from);
            }
        }
    };

    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (view.getId() == mDragSortListView.getId()) {
                final int currentFirstVisibleItem = mDragSortListView.getFirstVisiblePosition();
                if (mScrollToIndexPlaying) {
                    return;
                }

                if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                    mIsScrollingUp = false;
                }
                else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                    mIsScrollingUp = true;
                }

                mLastFirstVisibleItem = currentFirstVisibleItem;
                final int currentLastVisibleItem = mDragSortListView.getLastVisiblePosition();
                final int oldIndex = mStartIndex;
                int newIndex = oldIndex;
                if (!mIsScrollingUp && ((oldIndex + mCount) < mPlaylistSize) && (currentLastVisibleItem > mOffset)) {
                    newIndex = Math.abs((currentLastVisibleItem / mOffset) - 1) * mOffset;
                }
                else if (mIsScrollingUp && (oldIndex > 0)) {
                    newIndex = (((currentFirstVisibleItem - mDownOffset) / mOffset) + 1) * mOffset;
                    if (newIndex < 0) {
                        newIndex = 0;
                    }
                }
            }
        }
    };

    public static QueueFragment newInstance() {
        return new QueueFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_queue, container, false);

        mEmptyView = view.findViewById(R.id.empty_view_layout);
        mEmptyView.setVisibility(View.GONE);

        ImageView emptyIcon = (ImageView) view.findViewById(R.id.empty_view_icon);
        if (emptyIcon != null) {
            emptyIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_queue_empty, null));
        }
        TextView emptyTextView = (TextView) view.findViewById(R.id.empty_view_text);
        if (emptyTextView != null) {
            emptyTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.empty_queue_view_text, null));
            emptyTextView.setText(R.string.empty);
        }
        TextView emptyDetailTextView = (TextView) view.findViewById(R.id.empty_view_detail_text);
        if (emptyDetailTextView != null) {
            emptyDetailTextView.setText(R.string.empty_queue);
        }

        mQueueView = view.findViewById(R.id.queue_layout);
        mDragSortListView = (DragSortListView) view.findViewById(R.id.queue_list);
        mAdapter = new DataAdapter();
        mDragSortListView.setAdapter(mAdapter);

        mDragSortController = new DragSortController(mDragSortListView);
        mDragSortController.setDragHandleId(R.id.media_item_drag);
        mDragSortListView.setFloatViewManager(mDragSortController);
        mDragSortListView.setOnTouchListener((v, event) -> {
            mUserInteraction = true;
            return mDragSortController.onTouch(v, event);
        });
        mDragSortListView.setDragEnabled(true);
        mDragSortListView.setDropListener(mOnDropListener);
        mDragSortListView.setDragListener(mOnDragListener);
        mDragSortListView.setOnItemClickListener(this);

        mQueueMoreOptionsButton = (ImageButton) view.findViewById(R.id.queue_more_options);
        mQueueMoreOptionsButton.setOnClickListener(this);

        mQueueInfoTextView = (TextView) view.findViewById(R.id.queue_info);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mApp.isInit()) {
            mAllPlayManager.addOnCurrentZoneStateChangedListener(this);
            mAllPlayManager.addOnCurrentZoneSelectedListener(this);
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
            mAllPlayManager.removeOnCurrentZonePlaylistChangedListener(this);
        }
    }

    @Override
    public void updateState() {
        TaskExecutors.getExecutor().executeOnMain(() -> updatePlaylist(true));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.queue_more_options: {
                List<MoreOptionType> moreOptionTypes = new ArrayList<>();
                moreOptionTypes.add(MoreOptionsPopupWindow.MoreOptionType.CLEAR_QUEUE);
                MoreOptionsPopupWindow moreOptionsPopupWindow = new MoreOptionsPopupWindow(getContext(),
                                                                                           view, moreOptionTypes);
                moreOptionsPopupWindow
                        .setOnMoreOptionSelectedListener(moreOptionType -> {
                            switch (moreOptionType) {
                                case CLEAR_QUEUE:
                                    showClearQueue();
                                    break;
                            }
                        });
                moreOptionsPopupWindow.show();
            }
            break;
            default:
                break;
        }
    }

    private void updatePlaylistInUIThread(final boolean reset) {
        if (Utils.isActivityActive(mBaseActivity)) {
            TaskExecutors.getExecutor().executeOnMain(() -> {
                if (Utils.isActivityActive(mBaseActivity)) {
                    updatePlaylist(reset);
                }
            });
        }
    }

    private void updatePlaylist(boolean reset) {

        if (!isAdded()) {
            return;
        }

        final IoTGroup group = mAllPlayManager.getCurrentGroup();
        if (group == null) {
            return;
        }
        if ((group.getPlaylistSize() == 0)) {
            mQueueView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
        else {

            group.getCurrentItem();

            mQueueView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

            mQueueInfoTextView.setText(getString(R.string.number_of_songs, group.getPlaylistSize()));
        }
        mDragSortListView.invalidateViews();
        int playlistSize = group.getPlaylistSize();
        int indexPlaying = group.getIndexPlaying();
        mAdapter.setCurrentIndex(indexPlaying);

        mDragSortListView.setOnScrollListener(null);
        mPlaylistSize = playlistSize;
        mAdapter.setSize(mPlaylistSize);

        if ((reset == true) || (mStartIndex >= mPlaylistSize)) {
            mStartIndex = 0;
        }
        mScrollToIndexPlaying = false;

        if ((mFirstShow || !mUserInteraction)) {
            mFirstShow = false;

            mStartIndex = (((indexPlaying - mDownOffset) / mOffset) + 1) * mOffset;
            if (mStartIndex < 0) {
                mStartIndex = 0;
            }
            mScrollToIndexPlaying = true;
            mScrollToIndex = indexPlaying;
        }
        Log.d(TAG, "Play list start index is :" + mStartIndex);
        loadPlaylist(mStartIndex);
        mDragSortListView.setOnScrollListener(mOnScrollListener);
    }

    @Override
    public void onCurrentZoneSelected(IoTGroup zone) {
        mFirstShow = true;
        mUserInteraction = true;
        updatePlaylistInUIThread(true);
    }

    @Override
    public void onCurrentZoneRemoved() {

    }

    @Override
    public void onCurrentZonePlaylistChanged() {
        updatePlaylistInUIThread(true);
    }

    @Override
    public void onCurrentGroupLoopModeChanged(LoopMode loopMode) {
    }

    @Override
    public void onCurrentGroupShuffleModeChanged(ShuffleMode shuffleMode) {
    }

    @Override
    public void OnCurrentGroupStateChanged() {
        updatePlaylistInUIThread(false);
    }

    private void showClearQueue() {
        final String title = getString(R.string.clear);
        final String message = getString(R.string.clear_queue_message);
        final String positiveText = getString(R.string.clear);
        final String negativeText = getString(R.string.cancel);
        CustomDialogFragment customDialogFragment = CustomDialogFragment.newDialog(DIALOG_CLEAR_QUEUE_TAG,
                                                                                   title, message, positiveText,
                                                                                   negativeText);
        customDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(String tag) {
                IoTGroup group = mAllPlayManager.getCurrentGroup();
                group.clearPlaylist(success -> {
                    updatePlaylistInUIThread(false);
                });
            }

            @Override
            public void onNegativeButtonClicked(String tag) {

            }
        });
        mBaseActivity.showDialog(customDialogFragment, DIALOG_CLEAR_QUEUE_TAG);
    }

    private void showHaveNonLocalContent(final List<MediaItem> mediaItems) {
        final String title = getString(R.string.warning);
        final String message = getString(R.string.non_local_content_message);
        final String positiveText = getString(R.string.ok);
        final String negativeText = getString(R.string.cancel);
        CustomDialogFragment customDialogFragment = CustomDialogFragment.newDialog(DIALOG_HAVE_NON_LOCAL_CONTENT_TAG,
                                                                                   title, message, positiveText,
                                                                                   negativeText);
        customDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(String tag) {
                showSavePlaylist(mediaItems);
            }

            @Override
            public void onNegativeButtonClicked(String tag) {
            }
        });
        mBaseActivity.showDialog(customDialogFragment, DIALOG_HAVE_NON_LOCAL_CONTENT_TAG);
    }

    private void showSavePlaylist(final List<MediaItem> mediaItems) {

    }

    private void checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mBaseActivity, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(mBaseActivity,
                                                                         Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(mBaseActivity,
                                                      new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                                      BaseActivity.MY_PERMISSIONS_REQUEST_READ_STORAGE);
                }
                else {
                    warnReadStoragePermission();
                }
                return;
            }
        }
        mBaseActivity.addTaskToQueue(new CheckLocalContentAsyncTask(mAllPlayManager.getCurrentGroup()));
    }

    private void warnReadStoragePermission() {
        final String title = getString(R.string.need_storage_permission);
        final String message = getString(R.string.enable_storage_permission);
        final String positiveText = getString(R.string.ok);
        CustomDialogFragment customDialogFragment = CustomDialogFragment.newDialog(DIALOG_WARN_PERMISSION_TAG,
                                                                                   title, message, positiveText, null);
        customDialogFragment.setButtonClickedListener(null);
        mBaseActivity.showDialog(customDialogFragment, DIALOG_WARN_PERMISSION_TAG);
    }

    private void showPlaylistExists(final String playlistName, final List<MediaItem> mediaItems) {
        final String title = getString(R.string.playlist_name_exists);
        final String message = getString(R.string.playlist_name_exists_message, playlistName);
        final String positiveText = getString(R.string.replace);
        final String negativeText = getString(R.string.cancel);
        CustomDialogFragment customDialogFragment = CustomDialogFragment.newDialog(DIALOG_REPLACE_PLAYLIST_TAG,
                                                                                   title, message, positiveText,
                                                                                   negativeText);
        customDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(String tag) {

            }

            @Override
            public void onNegativeButtonClicked(String tag) {
            }
        });
        mBaseActivity.showDialog(customDialogFragment, DIALOG_REPLACE_PLAYLIST_TAG);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        playAtIndex(position);
    }

    private class DataAdapter extends BaseAdapter {

        final private List<MediaItem> mData = Collections.synchronizedList(new ArrayList<>());

        private int mDataStartIndex = 0;
        private int mDataEndIndex = 0;
        private int mSize = 0;
        private int mCurrentIndex = 0;

        public void setData(int index, List<MediaItem> mediaItemList) {

            if (mediaItemList == null) {
                return;
            }

            Log.d(TAG, "set Data:" + mediaItemList.size());

            mDataStartIndex = index;
            mDataEndIndex = mDataStartIndex + mediaItemList.size();
            mData.clear();
            mData.addAll(mediaItemList);
            mSize = mData.size();
            notifyDataSetChanged();
        }

        public void setSize(final int size) {
            mSize = size;
            notifyDataSetChanged();
        }

        public void setCurrentIndex(final int currentIndex) {
            mCurrentIndex = currentIndex;
            notifyDataSetChanged();
        }

        public int getCurrentIndex() {
            return mCurrentIndex;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Object getItem(final int position) {
            if (position < mData.size()) {
                return mData.get(position);
            }
            else {
                return null;
            }
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {

            Object itemObj = getItem(position);
            MediaItem mediaItem = null;
            if (itemObj != null) {
                mediaItem = (MediaItem) itemObj;
            }

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_media_item, parent, false);
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.media_item_image);
            TextView titleView = (TextView) convertView.findViewById(R.id.media_item_title);
            TextView subtitleView = (TextView) convertView.findViewById(R.id.media_item_subtitle);

            ImageButton moreOptionButton = (ImageButton) convertView
                    .findViewById(R.id.media_item_more_option);

            if (mediaItem != null) {
                convertView.setContentDescription(getString(R.string.cont_desc_queue_track, (position+1),
                                                            mediaItem.getTitle()));
                moreOptionButton.setContentDescription(getString(R.string.cont_desc_queue_track_options,
                                                                 mediaItem.getTitle()));

                titleView.setVisibility(View.VISIBLE);
                subtitleView.setVisibility(View.VISIBLE);
                if (Utils.isStringEmpty(mediaItem.getThumbnailUrl())) {
                    imageView.setImageDrawable(ResourcesCompat
                                                       .getDrawable(getResources(),
                                                                    R.drawable.ic_album_art_default_list, null));
                }
                else {
                    Picasso.get()
                            .load(mediaItem.getThumbnailUrl())
                            .placeholder(R.drawable.ic_album_art_default_list)
                            .error(R.drawable.ic_album_art_default_list)
                            .into(imageView);
                }

                String subtitle = mediaItem.getArtist();
                if (!Utils.isStringEmpty(subtitle) && !Utils.isStringEmpty(mediaItem.getAlbum())) {
                    subtitle += " - ";
                }
                subtitle += mediaItem.getAlbum();

                titleView.setText(mediaItem.getTitle().toUpperCase());
                subtitleView.setText(subtitle);
            }
            else {
                convertView.setContentDescription(getString(R.string.cont_desc_queue_track_unknown, (position+1)));
                moreOptionButton.setContentDescription(getString(R.string.cont_desc_queue_unknown_track_options,
                                                                 position));

                titleView.setVisibility(View.GONE);
                subtitleView.setVisibility(View.GONE);
                imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                                                       R.drawable.anim_progress_indeterminate_album,
                                                                       null));
                AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
                if (!animationDrawable.isRunning()) {
                    animationDrawable.start();
                }
            }

            ImageView dragImage = (ImageView) convertView.findViewById(R.id.media_item_drag);
            PlayingAnimationView playAnimation =
                    (PlayingAnimationView) convertView.findViewById(R.id.media_item_play_animation);

            moreOptionButton.setVisibility(View.VISIBLE);
            playAnimation.setVisibility(View.GONE);
            dragImage.setVisibility(View.VISIBLE);

            if (position == mCurrentIndex) {
                final IoTGroup zone = mAllPlayManager.getCurrentGroup();
                if (zone != null) {
                    playAnimation.setVisibility(View.VISIBLE);
                    moreOptionButton.setVisibility(View.GONE);
                    playAnimation.setPlayerState(zone.getPlayerState());
                }
            }

            if (moreOptionButton.getVisibility() == View.VISIBLE) {
                moreOptionButton.setFocusable(false);
            }
            moreOptionButton.setOnClickListener((mediaItem != null) ? (OnClickListener) view -> {
                List<MoreOptionType> moreOptionTypes = new ArrayList<>();
                moreOptionTypes.add(MoreOptionType.PLAY_NOW);
                if (position != mCurrentIndex) {
                    final IoTGroup zone = mAllPlayManager.getCurrentGroup();
                    if ((zone != null) && (zone.getPlayerState() == PlayState.kPlaying)) {
                        if (zone.getShuffleMode() == ShuffleMode.kLinear) {
                            moreOptionTypes.add(MoreOptionType.PLAY_NEXT);
                        }
                    }
                }
                moreOptionTypes.add(MoreOptionType.REMOVE_FROM_QUEUE);
                MoreOptionsPopupWindow moreOptionsPopupWindow = new MoreOptionsPopupWindow(getContext(),
                                                                                           view, moreOptionTypes);
                moreOptionsPopupWindow
                        .setOnMoreOptionSelectedListener(moreOptionType -> {
                            switch (moreOptionType) {
                                case PLAY_NOW:
                                    playAtIndex(position);
                                    break;
                                case PLAY_NEXT:
                                    if (position < mAdapter.getCount() && (mCurrentIndex + 1) < mAdapter.getCount()) {
                                        mAdapter.moveItem(position, mCurrentIndex + 1);
                                    }
                                    break;
                                case REMOVE_FROM_QUEUE:
                                    removeIndex(position);
                                    break;
                                default:
                                    break;
                            }
                        });
                moreOptionsPopupWindow.show();
            } : null);

            return convertView;
        }

        public void moveItem(int from, int to) {

            if (from >= 0 && from < mData.size() && to >= 0 && to < mData.size()) {
                Collections.swap(mData, from, to);
                moveMediaItems(from, to);
            }
            else {
                Log.e(TAG,
                      String.format("Indexes error, from[%d], to[%d], size[%d]", from, to, mData.size()));
            }
        }

        public synchronized void removeIndex(final int index) {
            if ((mDataStartIndex <= index) && (index < mDataEndIndex)) {
                if (removeItem(index, success -> {
                    if (success) {
                        UiThreadExecutor.getInstance().execute(() -> {
                            //It will be updated by {#onCurrentZonePlaylistChanged}
                            mData.remove(index - mDataStartIndex);
                            mDataEndIndex--;
                        });
                    }

                })) {
                    Log.d(TAG, String.format("Remove track#%d from playlist", index));
                }
            }
        }
    }

    private void loadPlaylist(int index) {

        IoTGroup group = mAllPlayManager.getCurrentGroup();
        group.updatePlaylistState(success ->
                                          UiThreadExecutor.getInstance().execute(() -> {
                                              mAdapter.setData(index, group.getPlayItem());
                                              if (mScrollToIndexPlaying) {
                                                  mDragSortListView.setSelection(mScrollToIndex);
                                                  mScrollToIndexPlaying = false;
                                              }
                                          })
        );

    }

    private void moveMediaItems(int from, int to) {
        IoTGroup group = mAllPlayManager.getCurrentGroup();
        if (group != null) {
            group.moveMediaItems(from, 1, to, success -> {
                if (success) {
                    Log.d(TAG, String.format("Move %d item to %d", from, to));
                    updateUI();
                }
                else {
                    Log.e(TAG, "Failed in moving items ");
                }
            });
        }
    }

    private boolean removeItem(int index, IoTCompletionCallback callback) {

        IoTGroup group = mAllPlayManager.getCurrentGroup();

        if (group != null) {
            return group.removeMediaItems(index, 1, callback) == IoTError.NONE;
        }

        return false;
    }

    private void playNext() {
        final IoTGroup group = mAllPlayManager.getCurrentGroup();
        if (group == null) {
            return;
        }

        try {
            group.next(status -> {
                if (status) {
                    Log.d(TAG, "Please next track!");
                }
            });
        }
        catch (OcException e) {
            e.printStackTrace();
        }
    }

    private boolean playAtIndex(int index) {

        IoTGroup group = mAllPlayManager.getCurrentGroup();

        if (HttpServerService.isHttpServerServiceStarted()) {
            if (group != null) {
                if ((group.getPlaylistSize() > index)) {
                    IoTError error = IoTError.NONE;
                    PlayState playState = group.getPlayerState();

                    if (mAdapter.getCurrentIndex() == index) {

                        if (playState == PlayState.kPaused) {
                            try {
                                group.play(success ->
                                                   TaskExecutors.getExecutor().executeOnMain(() -> mAdapter.setCurrentIndex(index))
                                );

                            }
                            catch (OcException e) {
                                e.printStackTrace();
                            }
                        }
                        else if ((playState == PlayState.kStopped)) {
                            error = group.playAtIndex(index);
                        }
                    }
                    else {
                        error = group.playAtIndex(index);
                    }

                    return (error == IoTError.NONE);
                }
            }
        }
        else {
            mApp.restartHttpService(new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == SmartAudioApplication.MESSAGE_HTTP_SERVER_STARTED) {
                        playAtIndex(index);
                    }
                }
            });
        }
        return true;
    }

    private class CheckLocalContentAsyncTask extends PleaseWaitAsyncTask {

        final private IoTGroup mZone;
        private boolean mHaveNoneLocalContent = false;

        public CheckLocalContentAsyncTask(final IoTGroup zone) {
            super(mBaseActivity, null);
            mZone = zone;
            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                    if (mHaveNoneLocalContent) {
                        showHaveNonLocalContent(mZone.getPlayItem());
                    }
                    else {
                        showSavePlaylist(mZone.getPlayItem());
                    }
                }

                @Override
                public void onRequestFailed() {

                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mZone != null) {
                mHaveNoneLocalContent = LocalProvider.getInstance().haveNonLocalContents(getContext(),
                                                                                         mZone.getPlayItem());
            }
            return null;
        }
    }

    protected void updateUI() {
        getActivity().runOnUiThread(() -> mAdapter.notifyDataSetChanged());
    }

}
