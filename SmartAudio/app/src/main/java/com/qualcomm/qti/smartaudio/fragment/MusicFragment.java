/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                     *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 *  ************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.fragment.BrowseFragment.BrowseFragmentType;
import com.qualcomm.qti.smartaudio.fragment.DeviceDetailsFragment.ActionType;
import com.qualcomm.qti.smartaudio.provider.output.OutputSourceSearchRequest;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.interfaces.IFragmentControl;
import com.qualcomm.qti.smartaudio.interfaces.IFragmentObserver;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupSelectedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupStateChangedListener;
import com.qualcomm.qti.smartaudio.model.ContentGroup;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.playlist.PlaylistSearchRequest;
import com.qualcomm.qti.smartaudio.provider.search.SearchRequest;
import com.qualcomm.qti.smartaudio.util.FragmentController;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.smartaudio.view.PlayPauseButton;
import com.qualcomm.qti.smartaudio.view.PlayingAnimationView;
import com.qualcomm.qti.smartaudio.view.TrackSeekBar;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

public class MusicFragment extends BaseFragment implements OnCurrentGroupStateChangedListener,
        OnCurrentGroupSelectedListener, View.OnClickListener, IFragmentControl {

    public static final String TAG = "MusicFragment";

    /*package*/ final static String EXTRA_KEY_IOT_OBJECT_ID = "IOT_OBJECT_ID";
    /*package*/ final static String EXTRA_KEY_IOT_OBJECT_TYPE = "IOT_OBJECT_TYPE";

    // Now Playing
    private SlidingUpPanelLayout mSlidingUpPanel = null;
    private RelativeLayout mMiniNowPlayingLayout = null;
    private ImageView mMiniAlbumArt = null;
    private PlayPauseButton mMiniPlayPauseButton = null;
    private PlayingAnimationView mMiniPlayingAnimation = null;
    private TextView mMiniTrackTextView = null;
    private TextView mMiniArtistTextView = null;
    private TrackSeekBar mMiniSeekBar = null;

    private ImageButton mDownButton = null;
    private TextView mSlideUpActionBarTitle = null;
    private ImageButton mQueueButton = null;
    private ImageButton mNowPlayingButton = null;

    private NowPlayingFragment mNowPlayingFragment = null;
    private QueueFragment mQueueFragment = null;

    // Browse
    private FragmentController mFragmentController = null;

    private ImageButton mActionBarBackButton = null;
    private ImageButton mActionBarSearchButton = null;
    private EditText mActionBarSearchEditText = null;
    private Button mActionBarButton = null;
    private TextView mActionBarTitleText = null;
    private View mActionBarSearchLayout = null;
    private ImageButton mActionBarSearchClearButton = null;
    private boolean mHaveSearch = false;
    private WeakReference<IFragmentObserver> mFragmentObserver;
    private String mObjectId;
    private IoTType mObjectType;

    private TextWatcher mSearchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            startSearch(s.toString());
        }
    };

    public static MusicFragment newInstance(String id, IoTType type) {
        MusicFragment musicFragment = new MusicFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_KEY_IOT_OBJECT_ID, id);
        bundle.putInt(EXTRA_KEY_IOT_OBJECT_TYPE, type.ordinal());
        musicFragment.setArguments(bundle);
        return musicFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFragmentObserver = new WeakReference<>((IFragmentObserver) context);
        mFragmentObserver.get().register(this);
    }

    @Override
    public void onDetach() {
        mFragmentObserver.get().unRegister(this);
        mFragmentObserver.clear();
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music, container, false);

        Bundle bundle = getArguments();
        mObjectId = bundle.getString(EXTRA_KEY_IOT_OBJECT_ID);
        mObjectType = IoTType.fromValue(bundle.getInt(EXTRA_KEY_IOT_OBJECT_TYPE));

        // This is for the mini player
        mSlidingUpPanel = view.findViewById(R.id.slider_panel_now_playing_layout);
        mSlidingUpPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if ((slideOffset >= 0.2f) && (mMiniNowPlayingLayout.getVisibility() == View.VISIBLE)) {
                    mMiniNowPlayingLayout.setVisibility(View.GONE);
                }
                else if ((slideOffset < 0.2f) && (mMiniNowPlayingLayout.getVisibility() == View.GONE)) {
                    final IoTGroup zone = mAllPlayManager.getCurrentGroup();
                    if (zone != null) {
                        final MediaItem mediaItem = zone.getCurrentItem();
                        if (mediaItem != null) {
                            mMiniNowPlayingLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                final IoTGroup group = mAllPlayManager.getCurrentGroup();
                if (group != null) {
                    final MediaItem mediaItem = group.getCurrentItem();

                    if (mediaItem == null) {
                        if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                            mSlidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        }
                    }
                    else {
                        updateMiniNowPlaying(group, mediaItem);
                        if (newState == SlidingUpPanelLayout.PanelState.HIDDEN) {
                            mSlidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
                        else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                            mNowPlayingFragment.updateInUiThread();
                        }
                    }
                }
            }
        });

        mMiniNowPlayingLayout = (RelativeLayout) view.findViewById(R.id.slider_panel_mini_now_playing_layout);

        mMiniAlbumArt = (ImageView) view.findViewById(R.id.slider_panel_mini_now_playing_album_art);
        mMiniPlayingAnimation = (PlayingAnimationView) view.findViewById(R.id.slider_panel_mini_playing_animation);
        mMiniTrackTextView = (TextView) view.findViewById(R.id.slider_panel_mini_track_text);
        mMiniArtistTextView = (TextView) view.findViewById(R.id.slider_panel_mini_artist_text);

        mMiniPlayPauseButton = (PlayPauseButton) view.findViewById(R.id.slider_panel_mini_play_pause_button);
        mMiniSeekBar = (TrackSeekBar) view.findViewById(R.id.slider_panel_mini_seekbar);
        mMiniSeekBar.enableTouch(false);

        mDownButton = (ImageButton) view.findViewById(R.id.app_bar_playing_down_button);
        mDownButton.setOnClickListener(this);

        mSlideUpActionBarTitle = view.findViewById(R.id.app_bar_playing_text);

        mQueueButton = (ImageButton) view.findViewById(R.id.app_bar_playing_queue_button);
        mQueueButton.setOnClickListener(this);

        mNowPlayingButton = (ImageButton) view.findViewById(R.id.app_bar_playing_now_playing_button);
        mNowPlayingButton.setOnClickListener(this);

        mNowPlayingFragment = NowPlayingFragment.newInstance();
        mQueueFragment = QueueFragment.newInstance();

        DeviceDetailsFragment deviceFragment = DeviceDetailsFragment.newInstance(mObjectId, mObjectType);
        deviceFragment.setController(this);

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.music_frame, mNowPlayingFragment);
        fragmentTransaction.add(R.id.music_frame, mQueueFragment);
        fragmentTransaction.hide(mQueueFragment);
        fragmentTransaction.commit();
        updateSlideUpTitleBar(getString(R.string.now_playing), getString(R.string.cont_desc_screen_playing));

        // This is for Browse
        mActionBarBackButton = (ImageButton) view.findViewById(R.id.app_bar_back_button);
        mActionBarBackButton.setVisibility((mBaseActivity.isTablet()) ? View.GONE : View.VISIBLE);
        if (!mBaseActivity.isTablet()) {
            mActionBarBackButton.setOnClickListener(this);
        }

        mActionBarTitleText = view.findViewById(R.id.app_bar_text);

        mActionBarSearchLayout = view.findViewById(R.id.app_bar_search_layout);
        mActionBarSearchButton = (ImageButton) view.findViewById(R.id.app_bar_search_button);
        mActionBarSearchButton.setOnClickListener(this);

        mActionBarSearchClearButton = (ImageButton) view.findViewById(R.id.app_bar_search_clear_button);
        mActionBarSearchClearButton.setOnClickListener(this);

        mActionBarSearchEditText = (EditText) view.findViewById(R.id.app_bar_search_text);

        mActionBarSearchEditText.setOnEditorActionListener((view1, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mActionBarButton.setText(getString(R.string.done));
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mActionBarSearchEditText.getWindowToken(), 0);
                startSearch(view1.getText().toString());
                return true;
            }
            return false;
        });

        mActionBarButton = view.findViewById(R.id.app_bar_button);
        mActionBarButton.setOnClickListener(this);

        mFragmentController = new FragmentController(getChildFragmentManager(), R.id.main_music_frame);
        mFragmentController.startFragment(deviceFragment, getNewBrowseTag(), false);

        return view;
    }

    private void startSearch(final String searchText) {
        BrowseFragment browseFragment = (BrowseFragment) mFragmentController.getCurrentFragment();
        if (browseFragment.getBrowseFragmentType() == BrowseFragment.BrowseFragmentType.SEARCH) {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setQuery(searchText);
            SearchFragment searchFragment = (SearchFragment) browseFragment;
            searchFragment.updateSearchRequest(searchRequest);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mApp.isInit()) {
            mAllPlayManager.addOnCurrentZoneStateChangedListener(this);
            mAllPlayManager.addOnCurrentZoneSelectedListener(this);
        }
        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mApp.isInit()) {
            mAllPlayManager.removeOnCurrentZoneStateChangedListener(this);
            mAllPlayManager.removeOnCurrentZoneSelectedListener(this);
        }
    }

    @Override
    protected void updateState() {
        updateUI();
    }

    @Override
    protected void updateUI() {

        updateBrowseFragment();

        if (mSlidingUpPanel == null) {
            return;
        }

        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        if (zone == null) {
            return;
        }

        final MediaItem mediaItem = zone.getCurrentItem();

        if (mediaItem == null) {
            if (mSlidingUpPanel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                mSlidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        }
        else {
            updateMiniNowPlaying(zone, mediaItem);
            if (mSlidingUpPanel.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                mSlidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
            if (mSlidingUpPanel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                mMiniNowPlayingLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void bringDownSlidePanel() {
        if ((mSlidingUpPanel.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) ||
                (mSlidingUpPanel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED)) {
            return;
        }
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        MediaItem mediaItem = null;
        if (zone != null) {
            mediaItem = zone.getCurrentItem();
        }
        if (mediaItem != null) {
            updateMiniNowPlaying(zone, mediaItem);
        }
        mSlidingUpPanel.setPanelState((mediaItem == null) ? SlidingUpPanelLayout.PanelState.HIDDEN :
                                              SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public boolean isMain() {
        return (!isSlidePanelUp() && (mFragmentController.getCount() == 1));
    }

    public void onBackPressed() {

        if (isSlidePanelUp()) {
            bringDownSlidePanel();
            return;
        }
        if (!isMain()) {
            Fragment fragment = mFragmentController.getCurrentFragment();
            if (fragment instanceof BrowseFragment) {
                BrowseFragment browseFragment = (BrowseFragment) mFragmentController.getCurrentFragment();
                if (browseFragment.getBrowseFragmentType() == BrowseFragment.BrowseFragmentType.SEARCH) {
                    enableSearch(false);
                }
                else {
                    mFragmentController.pop();
                    updateBrowseFragment();
                }
            }
            else {
                mFragmentController.pop();
                updateBrowseFragment();
            }
        }
        else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private boolean isSlidePanelUp() {
        return (mSlidingUpPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    private void updateMiniNowPlaying(final IoTGroup zone, final MediaItem mediaItem) {
        mMiniSeekBar.setZone(zone);

        if ((zone == null) || (mediaItem == null)) {
            return;
        }
        if (Utils.isStringEmpty(mediaItem.getThumbnailUrl())) {
            mMiniAlbumArt.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                                                       R.drawable.ic_album_art_default_small, null));
        }
        else {
            Picasso.get()
                    .load(mediaItem.getThumbnailUrl())
                    .placeholder(R.drawable.ic_album_art_default_small)
                    .error(R.drawable.ic_album_art_default_small)
                    .tag(getContext())
                    .into(mMiniAlbumArt);
        }
        PlayState playerState = zone.getPlayerState();
        String title = mediaItem.getTitle();
        if (Utils.isStringEmpty(title)) {
            title = getString(R.string.unknown_upper_case);
        }
        String artist = mediaItem.getArtist();
        if (Utils.isStringEmpty(artist)) {
            artist = getString(R.string.unknown_artist);
        }

        mMiniPlayPauseButton.setBaseActivity(mBaseActivity);
        mMiniPlayPauseButton.setGroup(zone);
        boolean enabled = zone.isInterruptible();
        boolean pauseEnabled = (enabled && zone.isPauseEnabled());
        double alpha = (pauseEnabled == true) ? 1.0 : 0.5;
        mMiniPlayPauseButton.setEnabled(pauseEnabled);
        mMiniPlayPauseButton.setAlpha((float) alpha);

        mMiniTrackTextView.setText(title.toUpperCase());
        mMiniArtistTextView.setText(artist);

        mMiniPlayingAnimation.setPlayerState(playerState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.app_bar_playing_down_button:
                bringDownSlidePanel();
                break;
            case R.id.app_bar_playing_now_playing_button:
                mQueueButton.setVisibility(View.VISIBLE);
                mNowPlayingButton.setVisibility(View.GONE);
            {
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                fragmentTransaction.show(mNowPlayingFragment);
                fragmentTransaction.hide(mQueueFragment);
                fragmentTransaction.commit();
                updateSlideUpTitleBar(getString(R.string.now_playing), getString(R.string.cont_desc_screen_playing));
            }
            break;
            case R.id.app_bar_playing_queue_button:
                mQueueButton.setVisibility(View.GONE);
                mNowPlayingButton.setVisibility(View.VISIBLE);

            {
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                fragmentTransaction.show(mQueueFragment);
                fragmentTransaction.hide(mNowPlayingFragment);
                fragmentTransaction.commit();
                updateSlideUpTitleBar(getString(R.string.queue_title), getString(R.string.cont_desc_screen_queue));
            }
            break;
            case R.id.app_bar_back_button:
                onBackPressed();
                break;
            case R.id.app_bar_search_button:
                enableSearch(true);
                break;
            case R.id.app_bar_button:
                enableSearch(false);
                break;
            case R.id.app_bar_search_clear_button:
                mActionBarSearchEditText.getText().clear();
                break;
            default:
                break;
        }
    }

    private void enableSearch(final boolean enable) {
        mHaveSearch = enable;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (enable) {
            mActionBarButton.setContentDescription(getString(R.string.cont_desc_cancel));
            mActionBarButton.setText(getString(R.string.cancel));
            mFragmentController.push(SearchFragment.newInstance(), getNewBrowseTag(), false);
            mActionBarSearchEditText.requestFocus();
            mActionBarSearchEditText.removeTextChangedListener(mSearchTextWatcher);
            mActionBarSearchEditText.setText("");
            mActionBarSearchEditText.addTextChangedListener(mSearchTextWatcher);
            imm.showSoftInput(mActionBarSearchEditText, InputMethodManager.SHOW_IMPLICIT);
        }
        else {
            imm.hideSoftInputFromWindow(mActionBarSearchEditText.getWindowToken(), 0);
            mFragmentController.pop(false);
        }
        updateBrowseFragment();
    }

    private void updateActionBarForSearch(final boolean searchEnabled) {
        mActionBarTitleText.setVisibility(searchEnabled ? View.GONE : View.VISIBLE);
        mActionBarSearchLayout.setVisibility(searchEnabled ? View.VISIBLE : View.GONE);
        mActionBarBackButton.setVisibility(searchEnabled ? View.GONE :
                                                   (!mBaseActivity.isTablet() ? View.VISIBLE : View.GONE));
        mActionBarButton.setVisibility(searchEnabled ? View.VISIBLE : View.GONE);
        mActionBarSearchButton.setVisibility((searchEnabled) ? View.GONE : ((mHaveSearch) ? View.GONE :
                View.VISIBLE));
    }

    private void hideSearchBar() {
        mActionBarTitleText.setVisibility(View.GONE);
        mActionBarSearchLayout.setVisibility(View.GONE);
        mActionBarBackButton.setVisibility(View.VISIBLE);
        mActionBarButton.setVisibility(View.GONE);
        mActionBarSearchButton.setVisibility(View.GONE);
    }

    private void showTitleBar() {
        mActionBarTitleText.setVisibility(View.VISIBLE);
        mActionBarSearchLayout.setVisibility(View.GONE);
        mActionBarBackButton.setVisibility(View.VISIBLE);
        mActionBarButton.setVisibility(View.GONE);
        mActionBarSearchButton.setVisibility(View.GONE);
    }

    private String getNewBrowseTag() {
        final int count = mFragmentController.getCount();
        return TAG + count;
    }

    private void updateBrowseFragment() {
        Fragment fragment = mFragmentController.getCurrentFragment();
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();

        if (fragment instanceof BrowseFragment) {
            BrowseFragment browseFragment = (BrowseFragment) fragment;
            String title = "";

            if (mFragmentController.getCount() == 1) {
                title = zone != null ? Utils.getZoneDisplayName(getContext(), zone) :
                        getString(R.string.main_browse_fragment_title);
            }
            else /*if (browseFragment != null)*/ {
                BrowseFragmentType type = browseFragment.getBrowseFragmentType();
                if (type == BrowseFragment.BrowseFragmentType.BROWSE) {
                    InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mActionBarSearchEditText.getWindowToken(), 0);
                    title = browseFragment.getTitle();
                    if (browseFragment.needsReload()) {
                        browseFragment.reload();
                    }
                }
                else if (type == BrowseFragmentType.MAIN) {
                    title = zone != null ? Utils.getZoneDisplayName(getContext(), zone) :
                            getString(R.string.main_browse_fragment_title);
                }
                else if (type == BrowseFragmentType.INPUT) {
                    title = getString(R.string.input_source_selection);
                }
                else if (type == BrowseFragmentType.ONLINE) {
                    title = getString(R.string.online_media_services_screen);
                }
            }

            updateActionBarForSearch((browseFragment.getBrowseFragmentType() == BrowseFragment.BrowseFragmentType.SEARCH));
            mActionBarSearchButton.setVisibility(browseFragment.isSearchable() ? View.VISIBLE : View.GONE);

            if (!Utils.isStringEmpty(title)) {
                updateTitleBar(title, getString(R.string.cont_desc_screen_browsing, title));
            }
        }
        else if (fragment instanceof DeviceDetailsFragment) {
            updateTitleBar(zone != null ? Utils.getZoneDisplayName(getContext(), zone) :
                                   getString(R.string.device_detail_fragment_title),
                           getString(R.string.cont_desc_screen_details));
            showTitleBar();
        }
        else if (fragment instanceof GroupFragment) {
            updateTitleBar(getString(R.string.group_fragment_title), getString(R.string.cont_desc_screen_group));
            showTitleBar();
        }
        else {
            hideSearchBar();
        }

    }

    @Override
    public void onCurrentZoneSelected(IoTGroup zone) {
        updateInUiThread();
    }

    @Override
    public void onCurrentZoneRemoved() {

    }

    @Override
    public void OnCurrentGroupStateChanged() {
        updateInUiThread();
    }

    @Override
    public void onBrowseListGroupClicked(ContentGroup contentGroup) {
        if (contentGroup == null) {
            return;
        }
        BrowseFragment browseFragment = (BrowseFragment) mFragmentController.getCurrentFragment();
        if (browseFragment.getBrowseFragmentType() == BrowseFragment.BrowseFragmentType.SEARCH) {
            mActionBarButton.setText(getString(R.string.done));
        }
        if (contentGroup.getRequest().getSearchType() == ContentSearchRequest.ContentSearchType.INPUT) {
            browseFragment = InputBrowseFragment.newInstance(contentGroup.getTitle(), contentGroup.getRequest());
        }
        else if (contentGroup.getRequest().getSearchType() == ContentSearchRequest.ContentSearchType.OUTPUT) {
            browseFragment = OutputDestinationsFragment.newInstance(contentGroup.getTitle(),
                                                                    contentGroup.getRequest());
        }
        else if (contentGroup.getRequest().getSearchType() == ContentSearchRequest.ContentSearchType.UPNP) {
            browseFragment = UpnpBrowseFragment.newInstance(contentGroup.getTitle(), contentGroup.getRequest());
        }
        else if (contentGroup.getRequest().getSearchType() == ContentSearchRequest.ContentSearchType.ONLINE) {
            browseFragment = OnlineBrowseFragment.newInstance(contentGroup.getTitle(), contentGroup.getRequest());
        }
        else {
            browseFragment = BrowseFragment.newInstance(contentGroup.getTitle(), contentGroup.getRequest());
        }
        mFragmentController.push(browseFragment, getNewBrowseTag());
        updateBrowseFragment();
    }

    @Override
    public void onChildBrowseFragmentObsolete() {
        setReloadOnParentBrowseFragment();
        mFragmentController.pop();
        updateBrowseFragment();
    }

    @Override
    public void onChildBrowseFragmentModified() {
        setReloadOnParentBrowseFragment();
        updateBrowseFragment();
    }

    public void onPlaylistSaved() {
        BrowseFragment browseFragment = (BrowseFragment) mFragmentController.getCurrentFragment();
        if (browseFragment == null) {
            return;
        }
        ContentSearchRequest contentSearchRequest = browseFragment.getSearchRequest();
        if ((contentSearchRequest != null) &&
                (contentSearchRequest.getSearchType() == ContentSearchRequest.ContentSearchType.PLAYLIST)) {
            PlaylistSearchRequest playlistSearchRequest = (PlaylistSearchRequest) contentSearchRequest;
            if (playlistSearchRequest.queryPlaylistName() == null) {
                browseFragment.reload();
            }
        }
    }

    public void onSongAddedToPlaylist(final String playlistName) {
        BrowseFragment browseFragment = (BrowseFragment) mFragmentController.getCurrentFragment();
        if (browseFragment == null) {
            return;
        }
        ContentSearchRequest contentSearchRequest = browseFragment.getSearchRequest();
        if ((contentSearchRequest != null) &&
                (contentSearchRequest.getSearchType() == ContentSearchRequest.ContentSearchType.PLAYLIST)) {
            PlaylistSearchRequest playlistSearchRequest = (PlaylistSearchRequest) contentSearchRequest;
            if (playlistName.equals(playlistSearchRequest.queryPlaylistName())) {
                browseFragment.reload();
            }
        }
    }

    public void onUpnpContentDirectoriesChanged() {
        Fragment fragment = mFragmentController.getCurrentFragment();

        if (fragment instanceof BrowseFragment) {
            BrowseFragment browseFragment = (BrowseFragment) fragment;
            if (browseFragment != null) {
                browseFragment.updateInUiThread();
            }
        }
    }

    private void setReloadOnParentBrowseFragment() {
        Fragment parentFragment = mFragmentController.getParentFragment();
        if (parentFragment != null) {
            BrowseFragment browseFragment = (BrowseFragment) parentFragment;
            browseFragment.setReload(true);
        }
    }

    @Override
    public void onDeviceDetailsItemClick(ActionType type) {

        switch (type) {
            case GROUP_DEVICE: {
                GroupFragment fragment = GroupFragment.newInstance(mObjectId);
                mFragmentController.push(fragment, getNewBrowseTag(), true);
                fragment.setController(this);
                updateBrowseFragment();
            }
            break;
            case AUDIO_SOURCE: {
                BrowseMainFragment fragment = BrowseMainFragment.newInstance();
                mFragmentController.push(fragment, getNewBrowseTag(), true);
                updateBrowseFragment();
            }
            break;
            case OUTPUT_DESTINATION: {
                OutputDestinationsFragment fragment =
                        OutputDestinationsFragment.newInstance(new OutputSourceSearchRequest());
                mFragmentController.push(fragment, getNewBrowseTag(), true);
                showTitleBar();
                updateTitleBar(getString(R.string.output_source_selection),
                               getString(R.string.cont_desc_screen_output));
            }
            break;
            case SURROUND_SYSTEM: {
                SetupSurroundsMainFragment fragment = SetupSurroundsMainFragment.newInstance(mObjectId);
                mFragmentController.push(fragment, getNewBrowseTag(), true);
                showTitleBar();
                updateTitleBar(getString(R.string.setup_surround_title), getString(R.string.cont_desc_screen_surround));
            }
            break;
        }

        mActionBarTitleText.setContentDescription(getString(R.string.cont_desc_screen,
                                                            mActionBarTitleText.getText()));
    }

    @Override
    public void onShowEditGroupNameFragment(Fragment fragment) {
        if (fragment != null) {
            mFragmentController.push(fragment, getNewBrowseTag(), true);
            showTitleBar();
            updateTitleBar(getString(R.string.group_tile), getString(R.string.cont_desc_screen_group_edit_name));
        }
    }

    @Override
    public void onGroupNameChosen(String name) {
        mFragmentController.pop(true);
        Fragment fragment = mFragmentController.getCurrentFragment();
        if (fragment instanceof GroupFragment) {
            GroupFragment frag = (GroupFragment) fragment;
            frag.updateUI(name);
        }
        updateBrowseFragment();
    }

    public interface OnMusicFragmentFinishedListener {

        void onMusicFragmentFinished();
    }

    private void updateTitleBar(String title, String contentDescription) {
        mActionBarTitleText.setText(title);
        mActionBarTitleText.setContentDescription(contentDescription);
    }

    private void updateSlideUpTitleBar(String title, String contentDescription) {
        mSlideUpActionBarTitle.setText(title);
        mSlideUpActionBarTitle.setContentDescription(contentDescription);
    }
}
