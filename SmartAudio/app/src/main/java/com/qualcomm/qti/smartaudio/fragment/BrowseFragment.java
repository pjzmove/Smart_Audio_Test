/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.ShuffleMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.interfaces.IFragmentObserver;
import com.qualcomm.qti.smartaudio.model.ContentGroup;
import com.qualcomm.qti.smartaudio.model.ContentItem;
import com.qualcomm.qti.smartaudio.model.ContentList;
import com.qualcomm.qti.smartaudio.model.DataList;
import com.qualcomm.qti.smartaudio.model.LoadDataResult;
import com.qualcomm.qti.smartaudio.model.MediaContentItem;
import com.qualcomm.qti.smartaudio.model.MediaContentList;
import com.qualcomm.qti.smartaudio.provider.ContentProviderException;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest.ContentSearchType;
import com.qualcomm.qti.smartaudio.provider.local.LocalProvider;
import com.qualcomm.qti.smartaudio.service.HttpServer;
import com.qualcomm.qti.smartaudio.util.BaseAsyncTask;
import com.qualcomm.qti.smartaudio.util.PleaseWaitAsyncTask;
import com.qualcomm.qti.smartaudio.view.MoreOptionsPopupWindow;
import com.qualcomm.qti.smartaudio.view.MoreOptionsPopupWindow.MoreOptionType;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrowseFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<LoadDataResult>,
        View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String TAG = BrowseFragment.class.getSimpleName();

    protected WeakReference<IFragmentObserver> mOnBrowseControl = null;

    protected LinearLayout mPlayAllBarLayout = null;
    protected LinearLayout mPlayAllLayout = null;
    protected TextView mPlayAllText = null;
    protected ImageButton mMoreOptionsButton = null;

    protected DragSortListView mDragSortListView = null;
    private DragSortController mDragSortController = null;
    protected BrowseAdapter mAdapter = null;

    protected LinearLayout mBackView = null;
    protected TextView mBackTextView = null;
    protected ProgressBar mBackProgressBar = null;

    protected int mFragmentID;
    protected int mLoaderId = 0;

    protected ContentSearchRequest mRequest = null;
    protected ContentList mContentList = null;
    private Exception mException = null;
    private String mTitle = null;
    private boolean mReload = false;

    public static final String KEY_REQUEST = "keyRequest";
    public static final String KEY_TITLE = "keyTitle";

    private static final String DIALOG_EDIT_PLAYLIST_NAME_TAG = "DialogEditPlaylistNameTag";

    private View mEmptyView = null;
    private TextView mEmptyDetailTextView = null;

    public enum PlayItemType {
        PLAY_ITEM_NOW,
        PLAY_ITEM_NEXT,
        ADD_ITEM_TO_QUEUE
    }

    public enum PlayAllType {
        PLAY_ALL_NOW,
        PLAY_ALL_NEXT,
        ADD_ALL_TO_QUEUE
    }

    public enum BrowseFragmentType {
        MAIN,
        BROWSE,
        SEARCH,
        INPUT,
        OUTPUT,
        ONLINE
    }

    public static BrowseFragment newInstance(final String title, final ContentSearchRequest contentSearchRequest) {
        BrowseFragment fragment = new BrowseFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        bundle.putSerializable(KEY_REQUEST, contentSearchRequest);
        fragment.setArguments(bundle);
        return fragment;
    }

    public BrowseFragment() {
        mFragmentID = R.layout.fragment_browse;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnBrowseControl = new WeakReference<>((IFragmentObserver) context);
    }

    @Override
    public void onDetach() {
        mOnBrowseControl.clear();
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(KEY_TITLE);
            mRequest = (ContentSearchRequest) getArguments().getSerializable(KEY_REQUEST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(mFragmentID, container, false);

        mEmptyView = view.findViewById(R.id.empty_view_layout);
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);

            ImageView emptyIcon = (ImageView) view.findViewById(R.id.empty_view_icon);
            if (emptyIcon != null) {
                emptyIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_queue_empty,
                                                                       null));
            }
            TextView emptyTextView = (TextView) view.findViewById(R.id.empty_view_text);
            if (emptyTextView != null) {
                emptyTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.empty_queue_view_text,
                                                                    null));
                if (mRequest != null && (mRequest.getSearchType() == ContentSearchType.OUTPUT || mRequest.getSearchType() == ContentSearchType.INPUT)) {
                    emptyTextView.setText(R.string.no_source_list);
                }
                else {
                    emptyTextView.setText(R.string.no_music);
                }
            }
            mEmptyDetailTextView = (TextView) view.findViewById(R.id.empty_view_detail_text);
        }

        mPlayAllBarLayout = (LinearLayout) view.findViewById(R.id.browse_play_all_action);
        mPlayAllLayout = (LinearLayout) view.findViewById(R.id.browse_play_all);
        mPlayAllText = (TextView) view.findViewById(R.id.browse_play_all_text);
        mMoreOptionsButton = (ImageButton) view.findViewById(R.id.browse_more_options);

        if (mPlayAllBarLayout != null) {
            mPlayAllBarLayout.setOnClickListener(this);
        }

        if (mMoreOptionsButton != null) {
            mMoreOptionsButton.setOnClickListener(this);
            mMoreOptionsButton.setContentDescription(getString(R.string.cont_desc_item_more_options,
                                                               getString(R.string.cont_desc_browse_all_items)));
        }

        mDragSortListView = (DragSortListView) view.findViewById(R.id.browse_list);
        mDragSortListView.setAdapter(getBrowseAdapter());
        mDragSortController = new DragSortController(mDragSortListView);
        mDragSortController.setDragHandleId(R.id.media_item_drag);
        mDragSortListView.setFloatViewManager(mDragSortController);
        mDragSortListView.setOnTouchListener(mDragSortController);
        mDragSortListView.setDropListener(mOnDropListener);
        mDragSortListView.setOnItemClickListener(this);
        mDragSortController.setDragInitMode(DragSortController.ON_DRAG);

        mBackView = (LinearLayout) view.findViewById(R.id.browse_back_layout);
        mBackTextView = (TextView) view.findViewById(R.id.browse_back_text);
        mBackProgressBar = (ProgressBar) view.findViewById(R.id.browse_back_progress);

        startLoader();

        return view;
    }

    protected BrowseAdapter getBrowseAdapter() {
        mAdapter = new BrowseAdapter();
        return mAdapter;
    }

    @Override
    public Loader<LoadDataResult> onCreateLoader(int id, Bundle args) {
        DataLoader dataLoader = new DataLoader(getActivity(), mRequest, mContentList);
        return dataLoader;
    }

    @Override
    public void onLoadFinished(final Loader<LoadDataResult> loader, final LoadDataResult data) {
        mContentList = data.getContentList();
        mException = data.getException();
        updateState();
    }

    @Override
    public void onLoaderReset(Loader<LoadDataResult> loader) {
        if (mBackView != null) {
            mBackView.setVisibility(View.GONE);
        }
    }

    public BrowseFragmentType getBrowseFragmentType() {
        return BrowseFragmentType.BROWSE;
    }

    public boolean isSearchable() {
        if (mContentList != null) {
            return mContentList.isSearchable();
        }
        if (mRequest != null) {
            return (mRequest.getSearchType() != ContentSearchRequest.ContentSearchType.UPNP) &&
                    (mRequest.getSearchType() != ContentSearchRequest.ContentSearchType.INPUT);
        }
        return false;
    }

    public String getTitle() {
        return mTitle;
    }

    public ContentSearchRequest getSearchRequest() {
        return mRequest;
    }

    public boolean needsReload() {
        return mReload;
    }

    public void setReload(final boolean reload) {
        mReload = reload;
    }

    public void reload() {
        setReload(false);
        mLoaderId++;
        if (mContentList != null) {
            mContentList.clear();
        }
        startLoader();
    }

    @Override
    protected void updateState() {
        mBackProgressBar.setVisibility(View.GONE);
        mBackTextView.setVisibility(View.GONE);
        mBackView.setVisibility(View.GONE);
        if (mException != null) {
            // IoTError checking
            return;
        }

        if (mContentList == null) {
            // IoTError checking
            return;
        }

        DataList dataList = mContentList.getDataList();

        mDragSortListView.setVisibility(dataList.isEmpty() ? View.GONE : View.VISIBLE);
        if (dataList.isEmpty()) {
            if (mPlayAllBarLayout != null) {
                mPlayAllBarLayout.setVisibility(View.GONE);
            }
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyDetailTextView.setText(mContentList.getEmptyString());
            }
        }
        else if (mPlayAllBarLayout != null) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.GONE);
            }
            mPlayAllBarLayout.setVisibility((mContentList.isPlayAll() && (mContentList.getPlayableCount() > 0)) ?
                                                    View.VISIBLE : View.GONE);

            if (mContentList.isPlayAll()) {
                mPlayAllText.setText(getString(R.string.play_all_number, mContentList.getPlayableCount()));
            }

            mDragSortListView.setDragEnabled(mContentList.isEditable());
            mAdapter.updateItems(mContentList.getDataList().getItems());
        }
    }

    public void startLoader() {
        if (mRequest == null) {
            return;
        }

        if (mBackView != null) {
            mBackView.setVisibility(View.VISIBLE);
            mBackTextView.setText(getString(R.string.loading));
            mBackTextView.setVisibility(View.VISIBLE);
            mBackProgressBar.setVisibility(View.VISIBLE);
        }
        if (mDragSortListView != null) {
            mDragSortListView.setVisibility(View.GONE);
        }
        LoaderManager.getInstance(this).restartLoader(mLoaderId, null, this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.browse_play_all_action:
                playAll(PlayAllType.PLAY_ALL_NOW);
                break;
            case R.id.browse_more_options:
                showMoreOptions(view, null);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ContentItem contentItem = (ContentItem) mAdapter.getItem(position);
        switch (contentItem.getContentType()) {
            case MEDIA:
                playItem(PlayItemType.PLAY_ITEM_NOW, (MediaContentItem) contentItem);
                break;
            case GROUP:
                if (mOnBrowseControl.get() != null) {
                    mOnBrowseControl.get().clickBrowseListGroup((ContentGroup) contentItem);
                }
                break;
            default:
                break;
        }
    }

    private static class DataLoader extends AsyncTaskLoader<LoadDataResult> {

        private LoadDataResult mLoadDataResult;

        private final ContentSearchRequest mRequest;
        private final ContentList mContentList;

        public DataLoader(final Context context, final ContentSearchRequest request, final ContentList contentList) {
            super(context);
            mRequest = request;
            mContentList = contentList;
        }

        @Override
        public LoadDataResult loadInBackground() {
            try {
                final ContentList result = mRequest.getContent(getContext(), mContentList);
                mLoadDataResult = new LoadDataResult(result);
            }
            catch (Exception e) {
                mLoadDataResult = new LoadDataResult(e);
            }

            return mLoadDataResult;
        }

        @Override
        public void deliverResult(final LoadDataResult data) {
            if (isStarted()) {
                super.deliverResult(data);
            }
        }

        @Override
        public void onCanceled(final LoadDataResult data) {
            super.onCanceled(data);
            mLoadDataResult = null;
        }

        @Override
        protected void onReset() {
            super.onReset();
            onStopLoading();
        }

        @Override
        protected void onStartLoading() {
            if (mLoadDataResult != null) {
                deliverResult(mLoadDataResult);
                return;
            }
            if (takeContentChanged() || (mLoadDataResult == null)) {
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
            mLoadDataResult = null;
        }
    }

    private void showMoreOptions(final View view, final MediaContentItem mediaContentItem) {
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        if (zone == null) {
            return;
        }
        List<MoreOptionType> moreOptionTypes = new ArrayList<>();
        moreOptionTypes.add(MoreOptionsPopupWindow.MoreOptionType.PLAY_NOW);
        if ((zone.getPlayerState() == PlayState.kPlaying) &&
                (zone.getShuffleMode() == ShuffleMode.kLinear)) {
            moreOptionTypes.add(MoreOptionsPopupWindow.MoreOptionType.PLAY_NEXT);
        }
        moreOptionTypes.add(MoreOptionsPopupWindow.MoreOptionType.ADD_TO_QUEUE);

        if (mediaContentItem != null) {
            moreOptionTypes.add(MoreOptionsPopupWindow.MoreOptionType.ADD_TO_PLAYLIST);
        }

        if (mContentList.isEditable()) {
            if (mediaContentItem == null) {
                moreOptionTypes.add(MoreOptionsPopupWindow.MoreOptionType.EDIT_PLAYLIST_NAME);
                moreOptionTypes.add(MoreOptionsPopupWindow.MoreOptionType.CLEAR_PLAYLIST);
                moreOptionTypes.add(MoreOptionsPopupWindow.MoreOptionType.DELETE_PLAYLIST);
            }
            else {
                moreOptionTypes.add(MoreOptionsPopupWindow.MoreOptionType.REMOVE_FROM_PLAYLIST);
            }
        }

        MoreOptionsPopupWindow moreOptionsPopupWindow = new MoreOptionsPopupWindow(getContext(),
                                                                                   view, moreOptionTypes);
        moreOptionsPopupWindow.setOnMoreOptionSelectedListener(new MoreOptionsPopupWindow.OnMoreOptionSelectedListener() {
            @Override
            public void onMoreOptionSelected(MoreOptionsPopupWindow.MoreOptionType moreOptionType) {
                switch (moreOptionType) {
                    case PLAY_NOW:
                        if (mediaContentItem == null) {
                            playAll(PlayAllType.PLAY_ALL_NOW);
                        }
                        else {
                            playItem(PlayItemType.PLAY_ITEM_NOW, mediaContentItem);
                        }
                        break;
                    case PLAY_NEXT:
                        if (mediaContentItem == null) {
                            playAll(PlayAllType.PLAY_ALL_NEXT);
                        }
                        else {
                            playItem(PlayItemType.PLAY_ITEM_NEXT, mediaContentItem);
                        }
                        break;
                    case ADD_TO_QUEUE:
                        if (mediaContentItem == null) {
                            playAll(PlayAllType.ADD_ALL_TO_QUEUE);
                        }
                        else {
                            playItem(PlayItemType.ADD_ITEM_TO_QUEUE, mediaContentItem);
                        }
                        break;
                    case ADD_TO_PLAYLIST:
                        break;
                    case EDIT_PLAYLIST_NAME:
                        showEditPlaylistName();
                        break;
                    case CLEAR_PLAYLIST:
                        deletePlaylist(mTitle, true);
                        break;
                    case DELETE_PLAYLIST:
                        deletePlaylist(mTitle, false);
                        break;
                    case REMOVE_FROM_PLAYLIST:
                        removeFromPlaylist(mTitle, mDragSortListView.getPositionForView(view));
                        break;
                    default:
                        break;
                }
            }
        });
        moreOptionsPopupWindow.show();
    }

    private void showEditPlaylistName() {
        final String title = getString(R.string.edit);
        final String hint = getString(R.string.edit_playlist_name_hint);
        final String positiveText = getString(R.string.edit);
        final String negativeText = getString(R.string.cancel);
        final EditTextDialogFragment editTextDialogFragment = EditTextDialogFragment
                .newEditTextDialog(DIALOG_EDIT_PLAYLIST_NAME_TAG, title, mTitle, hint, positiveText, negativeText);
        editTextDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(String tag) {
                EditText editText = editTextDialogFragment.getEditText();
                if (editText != null) {
                    editPlaylistName(editText.getText().toString());
                }
            }

            @Override
            public void onNegativeButtonClicked(String tag) {
            }
        });
        editTextDialogFragment.setEditTextKeyClickedListener(new EditTextDialogFragment.OnEditTextKeyClickedListener() {
            @Override
            public void onDoneClicked(String tag, EditText editText) {
                editPlaylistName(editText.getText().toString());
            }
        });
        mBaseActivity.showDialog(editTextDialogFragment, DIALOG_EDIT_PLAYLIST_NAME_TAG);
    }

    private void editPlaylistName(final String newName) {
        final String oldName = mTitle;
        mTitle = newName;
        updateState();
        mBaseActivity.addTaskToQueue(new EditPlaylistNameAsyncTask(oldName, newName));
    }

    private void removeFromPlaylist(final String playlistName, final int row) {
        mAdapter.removeIndex(row);
        updateState();
        mBaseActivity.addTaskToQueue(new RemoveRowFromPlaylistAsyncTask(playlistName, row));
    }

    private void deletePlaylist(final String playlistName, final boolean clear) {
        mAdapter.clear();
        updateState();
        mBaseActivity.addTaskToQueue(new DeletePlaylistAsyncTask(playlistName, clear));
    }

    private void playItem(final PlayItemType playItemType, final MediaContentItem mediaContentItem) {
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        if (zone == null) {
            return;
        }
        mBaseActivity.addTaskToQueue(new PlayItemAsyncTask(zone, playItemType, mediaContentItem));
    }

    private void playAll(final PlayAllType playAllType) {
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        if (zone == null) {
            return;
        }
        mBaseActivity.addTaskToQueue(new PlayAllAsyncTask(zone, playAllType,
                                                          mContentList.getDataList().getItems()));
    }

    private boolean playItem(final IoTGroup zone, final PlayItemType type, final MediaContentItem mediaContentItem) {
        if ((zone == null) || (mediaContentItem == null)) {
            return false;
        }

        List<MediaContentItem> mediaContentItems = new ArrayList<>();
        mediaContentItems.add(mediaContentItem);

        PlayAllType playAllType = PlayAllType.PLAY_ALL_NOW;
        switch (type) {
            case PLAY_ITEM_NEXT:
                playAllType = PlayAllType.PLAY_ALL_NEXT;
                break;
            case ADD_ITEM_TO_QUEUE:
                playAllType = PlayAllType.ADD_ALL_TO_QUEUE;
                break;
            default:
                break;
        }

        return playAll(zone, playAllType, mediaContentItems);
    }

    private boolean playAll(final IoTGroup zone, final PlayAllType type,
                            final List<MediaContentItem> mediaContentItems) {
        if ((zone == null) || (mediaContentItems == null) || mediaContentItems.isEmpty()) {
            return false;
        }

        if (!mApp.startHttpService()) {
            return false;
        }

        List<MediaItem> mediaItems = getMediaItems(mediaContentItems);
        updateUrls(mediaItems);

        IoTError error = IoTError.NONE;
        switch (type) {
            case PLAY_ALL_NOW:
                int size = zone.getPlaylistSize();
                error = zone.addMediaItemList(size, mediaItems, true, null);
                break;
            case PLAY_ALL_NEXT:
                error = zone.addMediaItemList((zone.getPlaylistSize() == 0) ? 0 : zone.getIndexPlaying() + 1,
                                              mediaItems, false, null);
                break;
            case ADD_ALL_TO_QUEUE:
                error = zone.addMediaItemList(zone.getPlaylistSize(), mediaItems, false, null);
                break;
        }

        return (error == IoTError.NONE);
    }

    private List<MediaItem> getMediaItems(final List<MediaContentItem> contentList) {
        List<MediaItem> mediaItems = new ArrayList<>();
        for (MediaContentItem contentItem : contentList) {
            mediaItems.add(contentItem.getMediaItem());
        }
        return mediaItems;
    }

    private void updateUrls(List<MediaItem> mediaItems) {
        String ipAddress = mApp.getConnectivityReceiver().getIPAddress();
        if (ipAddress == null) {
            return;
        }
        for (MediaItem mediaItem : mediaItems) {
            if ((mediaItem.getThumbnailUrl() != null) &&
                    mediaItem.getThumbnailUrl().startsWith(LocalProvider.LOCAL_SCHEME)) {
                Uri uri = Uri.parse(mediaItem.getThumbnailUrl());
                String albumArtID = uri.getLastPathSegment();
                mediaItem.setThumbnailUrl(
                        HttpServer.buildHttpUrl(ipAddress, HttpServer.HTTP_ALBUM_ART_TYPE, albumArtID));
            }
            if ((mediaItem.getStreamUrl() != null) &&
                    mediaItem.getStreamUrl().startsWith(LocalProvider.LOCAL_SCHEME)) {
                Uri uri = Uri.parse(mediaItem.getStreamUrl());
                String audioID = uri.getLastPathSegment();
                mediaItem.setStreamUrl(HttpServer.buildHttpUrl(ipAddress, HttpServer.HTTP_AUDIO_TYPE, audioID));
            }
        }
    }

    private DragSortListView.DropListener mOnDropListener = (from, to) -> {
        if ((from != to) && (mAdapter != null)) {
            mAdapter.moveItem(from, to);
            mBaseActivity.addTaskToQueue(new MoveItemInPlaylistAsyncTask(mTitle, from, to));
        }
    };

    class BrowseAdapter extends BaseAdapter {

        protected List<ContentItem> mItems;

        public BrowseAdapter() {
            mItems = Collections.synchronizedList(new ArrayList<>());
        }

        public void updateItems(final List<ContentItem> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final ContentItem contentItem = (ContentItem) getItem(position);
            String title = contentItem.getTitle();

            switch (contentItem.getContentType()) {
                case MEDIA:
                case GROUP: {
                    if ((convertView == null) ||
                            !convertView.getTag().equals(contentItem.getContentType().toString())) {
                        LayoutInflater inflater = (LayoutInflater) getActivity().
                                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = inflater.inflate(R.layout.list_item_media_item, viewGroup, false);
                    }
                    ImageView dragImage = (ImageView) convertView.findViewById(R.id.media_item_drag);
                    ImageView imageView = (ImageView) convertView.findViewById(R.id.media_item_image);
                    TextView titleView = (TextView) convertView.findViewById(R.id.media_item_title);
                    TextView subtitleView = (TextView) convertView.findViewById(R.id.media_item_subtitle);
                    ImageButton moreOptionButton = (ImageButton) convertView.findViewById(R.id.media_item_more_option);

                    if (contentItem.getContentType() == ContentItem.ContentType.GROUP) {
                        moreOptionButton.setVisibility(View.GONE);
                        dragImage.setVisibility(View.GONE);
                    }
                    else {
                        moreOptionButton.setVisibility(View.VISIBLE);
                        dragImage.setVisibility((mContentList.isEditable()) ? View.VISIBLE : View.GONE);

                        moreOptionButton.setFocusable(false);
                        moreOptionButton.setContentDescription(getString(R.string.cont_desc_item_more_options, title));
                        moreOptionButton.setOnClickListener(
                                view -> showMoreOptions(view, (MediaContentItem) contentItem));
                    }
                    Picasso.get()
                            .load(contentItem.getThumbnailUrl())
                            .placeholder(R.drawable.ic_album_art_default_list)
                            .error(R.drawable.ic_album_art_default_list)
                            .into(imageView);

                    titleView.setText(contentItem.getTitle().toUpperCase());
                    subtitleView.setText(contentItem.getSubtitle());
                    convertView.setContentDescription(getString(R.string.cont_desc_browse_item, title));
                }
                break;
                case SECTION: {
                    if ((convertView == null) || !convertView.getTag().equals(contentItem.getContentType().toString())) {
                        LayoutInflater inflater = (LayoutInflater) getActivity().
                                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = inflater.inflate(R.layout.list_item_browse_section, viewGroup, false);
                    }

                    TextView titleView = convertView.findViewById(R.id.browse_section_title);
                    titleView.setText(title);
                    convertView.setContentDescription(getString(R.string.cont_desc_browse_section, title));
                }
                break;
                default:
                    break;
            }
            convertView.setTag(contentItem.getContentType().toString());

            return convertView;
        }

        public void moveItem(final int from, final int to) {
            if (mItems.size() == 0) {
                return;
            }
            ContentItem item = mItems.remove(from);
            mItems.add(to, item);
            notifyDataSetChanged();
        }

        public void removeIndex(final int index) {
            if ((index >= 0) && (index < mItems.size())) {
                mItems.remove(index);
            }
            notifyDataSetChanged();
        }

        public void clear() {
            mItems.clear();
            notifyDataSetChanged();
        }
    }

    public class EditPlaylistNameAsyncTask extends BaseAsyncTask {

        final private String mOldName;
        final private String mNewName;

        public EditPlaylistNameAsyncTask(final String oldName, final String newName) {
            super(mBaseActivity, null);
            mOldName = oldName;
            mNewName = newName;
            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                    mOnBrowseControl.get().modifyChildBrowseFragment();
                }

                @Override
                public void onRequestFailed() {
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

    public class MoveItemInPlaylistAsyncTask extends BaseAsyncTask {

        final private String mPlaylistName;
        final private int mFrom;
        final private int mTo;

        public MoveItemInPlaylistAsyncTask(final String playlistName, final int from, final int to) {
            super(mBaseActivity, null);
            mPlaylistName = playlistName;
            mFrom = from;
            mTo = to;
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

    private class DeletePlaylistAsyncTask extends BaseAsyncTask {

        final private String mPlaylistName;
        final private boolean mClear;

        public DeletePlaylistAsyncTask(final String playlistName, final boolean clear) {
            super(mBaseActivity, null);
            mPlaylistName = playlistName;
            mClear = clear;
            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                    if (mClear) {
                        mOnBrowseControl.get().modifyChildBrowseFragment();
                    }
                    else {
                        mOnBrowseControl.get().obsoleteChildBrowseFragment();
                    }
                }

                @Override
                public void onRequestFailed() {
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

    private class RemoveRowFromPlaylistAsyncTask extends BaseAsyncTask {

        final private String mPlaylistName;
        final private int mRow;

        public RemoveRowFromPlaylistAsyncTask(final String playlistName, final int row) {
            super(mBaseActivity, null);
            mPlaylistName = playlistName;
            mRow = row;
            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                    mOnBrowseControl.get().modifyChildBrowseFragment();
                }

                @Override
                public void onRequestFailed() {
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

    private class PlayItemAsyncTask extends PleaseWaitAsyncTask {

        final private IoTGroup mZone;
        final private PlayItemType mType;
        final private MediaContentItem mMediaContentItem;

        public PlayItemAsyncTask(final IoTGroup zone, final PlayItemType type,
                                 final MediaContentItem mediaContentItem) {
            super(mBaseActivity, null);
            mZone = zone;
            mType = type;
            mMediaContentItem = mediaContentItem;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mResult = playItem(mZone, mType, mMediaContentItem);
            return null;
        }
    }

    private class PlayAllAsyncTask extends PleaseWaitAsyncTask {

        final private IoTGroup mZone;
        final private PlayAllType mType;
        final private List<ContentItem> mContentItems;

        public PlayAllAsyncTask(final IoTGroup zone, final PlayAllType type, final List<ContentItem> contentItems) {
            super(mBaseActivity, null);
            mZone = zone;
            mType = type;
            mContentItems = contentItems;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final BaseActivity baseActivity = getActiveActivity();
            if (baseActivity == null) {
                return null;
            }

            List<MediaContentItem> mediaContentItems = new ArrayList<>();

            for (ContentItem contentItem : mContentItems) {
                if (contentItem == null) {
                    continue;
                }
                switch (contentItem.getContentType()) {
                    case MEDIA:
                        mediaContentItems.add((MediaContentItem) contentItem);
                        break;
                    case GROUP:
                        ContentSearchRequest contentSearchRequest = ((ContentGroup) contentItem).getRequest();
                        if (contentSearchRequest != null) {
                            try {
                                ContentList contentList = new MediaContentList();
                                traverseToEnd(baseActivity, contentSearchRequest, contentList);
                                mediaContentItems.addAll(getMediaContentItems(contentList.getDataList().getItems()));
                            }
                            catch (ContentProviderException e) {
                                e.printStackTrace();
                                mResult = false;
                            }
                        }
                        break;
                    default:
                        continue;
                }
            }
            if (mResult) {
                mResult = playAll(mZone, mType, mediaContentItems);
            }
            return null;
        }
    }

    protected List<MediaContentItem> getMediaContentItems(final List<ContentItem> contentItems) {
        List<MediaContentItem> mediaContentItems = new ArrayList<>();
        if (contentItems != null) {
            for (ContentItem contentItem : contentItems) {
                if ((contentItem != null) && (contentItem.getContentType() == ContentItem.ContentType.MEDIA)) {
                    mediaContentItems.add((MediaContentItem) contentItem);
                }
            }
        }
        return mediaContentItems;
    }

    protected void traverseToEnd(Context context, ContentSearchRequest contentSearchRequest, ContentList contentList)
            throws ContentProviderException {
        ContentList contents = contentSearchRequest.getContent(context, null);
        for (ContentItem contentItem : contents.getDataList().getItems()) {
            switch (contentItem.getContentType()) {
                case MEDIA:
                    contentList.getDataList().addItem(contentItem);
                    break;
                case GROUP:
                    ContentGroup contentGroup = (ContentGroup) contentItem;
                    ContentList newContentList = new MediaContentList();
                    traverseToEnd(context, contentGroup.getRequest(), newContentList);
                    contentList.getDataList().addItems(newContentList.getDataList().getItems());
                    break;
                default:
                    break;
            }
        }
    }

}
