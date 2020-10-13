/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EnabledControlsAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTRepository;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.activity.MainActivity;
import com.qualcomm.qti.smartaudio.adapter.SpeakerAdapter;
import com.qualcomm.qti.smartaudio.interfaces.IFragmentControl;
import com.qualcomm.qti.smartaudio.interfaces.IFragmentObserver;
import com.qualcomm.qti.smartaudio.interfaces.IMainViewController;
import com.qualcomm.qti.smartaudio.interfaces.OnGroupSelectedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupPlaylistChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupStateChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnDeviceRediscoveredListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupInfoStateChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupListChangedListener;
import com.qualcomm.qti.smartaudio.util.PleaseWaitAsyncTask;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;

import org.iotivity.base.OcException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SpeakersListFragment extends BaseFragment implements
        OnGroupListChangedListener,
        OnGroupInfoStateChangedListener,
        OnCurrentGroupStateChangedListener,
        OnCurrentGroupPlaylistChangedListener,
        View.OnClickListener,
        OnDeviceRediscoveredListener,
        IFragmentControl,
        SpeakerAdapter.SpeakerAdapterListener {

    public static final String TAG = "SpeakersListFragment";

    // When a group is selected
    private OnGroupSelectedListener mOnGroupSelectedListener = null;

    private ExpandableListView mListView = null;
    private SpeakerAdapter mAdapter = null;

    private View mEmptyView = null;

    private LinearLayout mPauseAllButtonLinearLayout = null;
    private PauseAllAsyncTask mPauseAllAsyncTask = null;

    private WeakReference<IFragmentObserver> mControlObserver;
    private WeakReference<IMainViewController> mViewController;

    private SwipeRefreshLayout mSwipeContainerEmpty;

    public static SpeakersListFragment newInstance() {
        SpeakersListFragment fragment = new SpeakersListFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnGroupSelectedListener = (OnGroupSelectedListener) context;
        mControlObserver = new WeakReference<>((IFragmentObserver) context);
        mControlObserver.get().register(this);
        mViewController = new WeakReference<>((IMainViewController) context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_speaker, container, false);

        TextView titleView = view.findViewById(R.id.app_bar_text);
        titleView.setText(R.string.main_view_bar_title);
        titleView.setContentDescription(getString(R.string.cont_desc_screen_speakers_list));

        setHasOptionsMenu(true);

        // Get the list view.
        mListView = view.findViewById(R.id.speaker_fragment_list_view);
        mAdapter = new SpeakerAdapter(this);
        mListView.setAdapter(mAdapter);

        mEmptyView = view.findViewById(R.id.empty_view_layout);

        ImageView emptyIcon = view.findViewById(R.id.empty_view_icon);
        if (emptyIcon != null) {
            emptyIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_speaker_list_empty,
                                                                   null));
        }
        TextView emptyTextView = view.findViewById(R.id.empty_view_text);
        if (emptyTextView != null) {
            emptyTextView.setText(R.string.empty_list_text);
        }
        TextView emptyDetailTextView = view.findViewById(R.id.empty_view_detail_text);
        if (emptyDetailTextView != null) {
            emptyDetailTextView.setText(R.string.empty_list_detail_text);
        }

        mPauseAllButtonLinearLayout = view.findViewById(R.id.speaker_fragment_pause_all_button_layout);
        mPauseAllButtonLinearLayout.setContentDescription(getString(R.string.cont_desc_pause_all));
        mPauseAllButtonLinearLayout.setOnClickListener(this);

        if ((mApp != null) && mApp.isInit()) {
            // Add the listeners
            updateState();
        }

        SwipeRefreshLayout swipeContainer = view.findViewById(R.id.manual_scan);
        swipeContainer.setOnRefreshListener(() -> {
                                                IoTService.getInstance().forceScan();
                                                UiThreadExecutor.getInstance().executeAtDelayTime(() -> {
                                                    swipeContainer.setRefreshing(false);
                                                }, 500);
                                            }
        );

        mSwipeContainerEmpty = view.findViewById(R.id.manual_scan_empty);
        mSwipeContainerEmpty.setOnRefreshListener(() -> {
                                                      IoTService.getInstance().forceScan();
                                                      UiThreadExecutor.getInstance().executeAtDelayTime(() -> {
                                                          mSwipeContainerEmpty.setRefreshing(false);
                                                      }, 500);
                                                  }
        );
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mViewController.get() != null) {
            mViewController.get().setupToolbar(R.id.speaker_app_toolbar);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((mApp != null) && mApp.isInit()) {
            mAllPlayManager.addOnZoneListChangedListener(this);
            mAllPlayManager.addOnZoneStateChangedListener(this);
            mAllPlayManager.addOnCurrentZonePlaylistChangedListener(this);
            mAllPlayManager.addOnCurrentZoneStateChangedListener(this);
            mAllPlayManager.registerDeviceRediscoveredListener(this);
            updateState();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if ((mApp != null) && mApp.isInit()) {
            mAllPlayManager.removeOnZoneListChangedListener(this);
            mAllPlayManager.removeOnZoneStateChangedListener(this);
            mAllPlayManager.removeOnCurrentZonePlaylistChangedListener(this);
            mAllPlayManager.removeOnCurrentZoneStateChangedListener(this);
            mAllPlayManager.unRegisterDeviceRediscoveredListener(this);
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mControlObserver.get().unRegister(this);
        mControlObserver.clear();
        mViewController.clear();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) {
            return false;
        }

        switch (item.getItemId()) {
            case MainActivity.ActionBarActions.BOTTOM_MENU:
                activity.showBottomSheetDialogFragment();
                return true;
            case MainActivity.ActionBarActions.WIFI_ONBOARDING:
                activity.navigateToWifiOnboarding();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void updateState() {
        if ((mAllPlayManager == null)) {
            return;
        }

        UiThreadExecutor.getInstance().execute(() -> updateUI());
    }


    @Override
    protected void updateUI() {
        List<IoTGroup> groups = mAllPlayManager.getGroups();

        // Here we separate the idle and non-idle zones
        final List<IoTGroup> idleZones = new ArrayList<>();
        final List<IoTGroup> nowPlayingZones = new ArrayList<>();
        boolean zonePlaying = false;
        for (IoTGroup grp : groups) {
            if ((grp.getCurrentItem() != null)) {
                // If it has a current item, it is non idle
                nowPlayingZones.add(grp);
                if (grp.getPlayerState() == PlayState.kPlaying) {
                    zonePlaying = true;
                }
            }
            else {
                idleZones.add(grp);
            }
        }
        boolean isEmpty;
        isEmpty = groups.isEmpty();

        if (mAdapter != null) {
            mAdapter.updateSpeakers(nowPlayingZones, idleZones);
        }
        mListView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        mSwipeContainerEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        mPauseAllButtonLinearLayout.setEnabled(zonePlaying);
        float alpha = (zonePlaying == true) ? 1.0f : 0.5f;
        mPauseAllButtonLinearLayout.setAlpha(alpha);
        mPauseAllButtonLinearLayout.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.speaker_fragment_pause_all_button_layout:
                pauseAll();
                break;
            default:
                break;
        }
    }

    @Override // SpeakerAdapter.SpeakerAdapterListener
    public void onZoneSelected(IoTRepository zone) {
        if (zone != null) {
            mOnGroupSelectedListener.onZoneSelected(zone);
        }
    }

    @Override // SpeakerAdapter.SpeakerAdapterListener
    public void onZoneMenuSelected(IoTRepository zone) {
        mOnGroupSelectedListener.onZoneMenuSelected(zone);
    }

    @Override // SpeakerAdapter.SpeakerAdapterListener
    public BaseActivity getBaseActivity() {
        return mBaseActivity;
    }

    @Override // SpeakerAdapter.SpeakerAdapterListener
    public IoTDevice getDeviceByHostName(String host) {
        return mAllPlayManager.getDeviceByHostName(host);
    }

    @Override
    public void onZoneListChanged() {
        updateInUiThread();
    }

    @Override
    public void onGroupInfoStateChanged() {
        updateInUiThread();
    }

    @Override
    public void onGroupSpeakerFragmentDismiss() {
        mAdapter.setGroupFragmentZoneID(null);
    }

    private void pauseAll() {
        if (mPauseAllAsyncTask == null) {
            mPauseAllAsyncTask = new PauseAllAsyncTask();
            mBaseActivity.addTaskToQueue(mPauseAllAsyncTask);
        }
    }

    private class PauseAllAsyncTask extends PleaseWaitAsyncTask {

        final private Object mCondition = new Object();
        private Integer mCount;

        public PauseAllAsyncTask() {
            super(mBaseActivity, null);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<IoTGroup> groups = mAllPlayManager.getGroups();
            int count = groups.size();
            CountDownLatch latch = new CountDownLatch(count);
            for (IoTGroup group : groups) {
                try {
                    group.pause(status -> {
                        if (status) {
                            latch.countDown();
                        }

                        try {
                            latch.await(3000, TimeUnit.SECONDS);
                            UiThreadExecutor.getInstance().execute(() -> updateState());
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                }
                catch (OcException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void clean() {
            mPauseAllAsyncTask = null;
        }
    }

    @Override
    public void onCurrentZonePlaylistChanged() {
        updateState();
    }

    @Override
    public void onCurrentGroupEnabledControlsChanged(EnabledControlsAttr attr) {
        updateState();
    }

    @Override
    public void OnCurrentGroupStateChanged() {
        updateState();
    }

    @Override
    public void onDeviceRediscovered() {
        updateState();
    }
}
