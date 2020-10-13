/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.LoopMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.ShuffleMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTBluetoothDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothAdapterState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothDiscoverableState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothError;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTRepository;
import com.qualcomm.qti.iotcontrollersdk.utils.TaskMonitor;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.activity.MainActivity;
import com.qualcomm.qti.smartaudio.adapter.NetworkTreeAdapter;
import com.qualcomm.qti.smartaudio.adapter.NetworkTreeAdapter.NetworkTreeAdapterListener;
import com.qualcomm.qti.smartaudio.interfaces.IMainViewController;
import com.qualcomm.qti.smartaudio.interfaces.OnGroupSelectedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupPlaylistChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupStateChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupInfoStateChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupListChangedListener;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager.OnBluetoothListener;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;

import org.iotivity.base.OcException;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class NetworkTreeFragment extends BaseFragment implements OnGroupListChangedListener,
        OnGroupInfoStateChangedListener,
        OnCurrentGroupStateChangedListener,
        OnCurrentGroupPlaylistChangedListener,
        OnClickListener, NetworkTreeAdapterListener, OnBluetoothListener {

    private WeakReference<IMainViewController> mViewController;
    private RecyclerView mDeviceTree;
    private View mNoDevicesView;
    private NetworkTreeAdapter mAdapter;
    private LinearLayout mPauseAllButtonLinearLayout;

    private OnGroupSelectedListener mOnGroupSelectedListener = null;

    public static NetworkTreeFragment newInstance() {
        return new NetworkTreeFragment();
    }

    @Override
    public void updateState() {
        updateUI();
    }

    @Override
    public void updateUI() {
        updateTreeView(fetchDevices());
        updatePlayingState();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_network_tree_view, container, false);
        mDeviceTree = rootView.findViewById(R.id.devices_tree);

        TextView titleView = rootView.findViewById(R.id.app_bar_text);
        titleView.setText(R.string.main_view_bar_title);
        titleView.setContentDescription(getString(R.string.cont_desc_screen_network_tree));

        setHasOptionsMenu(true);
        mDeviceTree.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mDeviceTree.setLayoutManager(layoutManager);
        mAdapter = new NetworkTreeAdapter(this);
        mDeviceTree.setAdapter(mAdapter);

        mPauseAllButtonLinearLayout = rootView.findViewById(R.id.speaker_fragment_pause_all_button_layout);
        mPauseAllButtonLinearLayout.setContentDescription(getString(R.string.cont_desc_pause_all));
        mPauseAllButtonLinearLayout.setOnClickListener(this);

        SwipeRefreshLayout refreshLayout = rootView.findViewById(R.id.devices_tree_refresh_layout);
        refreshLayout.setOnRefreshListener(() -> onRefreshTree(refreshLayout));

        mNoDevicesView = initNoDeviceView(rootView);

        updateTreeView(fetchDevices());

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnGroupSelectedListener = (OnGroupSelectedListener) context;
        mViewController = new WeakReference<>((IMainViewController) context);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivity() == null) {
            return false;
        }

        switch (item.getItemId()) {
            case MainActivity.ActionBarActions.BOTTOM_MENU:
                ((MainActivity) getActivity()).showBottomSheetDialogFragment();
                return true;
            case MainActivity.ActionBarActions.WIFI_ONBOARDING:
                ((MainActivity) getActivity()).navigateToWifiOnboarding();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mViewController.get() != null) {
            mViewController.get().setupToolbar(R.id.network_app_toolbar);
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
            mIoTSysManager.addOnBluetoothListener(this);
            updateState();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if ((mApp != null) && mApp.isInit()) {
            // Remove listeners
            mAllPlayManager.removeOnZoneListChangedListener(this);
            mAllPlayManager.removeOnZoneStateChangedListener(this);
            mAllPlayManager.removeOnCurrentZonePlaylistChangedListener(this);
            mAllPlayManager.removeOnCurrentZoneStateChangedListener(this);
            mIoTSysManager.removeOnBluetoothListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.speaker_fragment_pause_all_button_layout) {
            pauseAll();
        }
    }

    private void pauseAll() {
        List<IoTGroup> groups = mAllPlayManager.getGroups();
        int count = groups.size();
        TaskMonitor monitor = new TaskMonitor(count);
        for (IoTGroup group : groups) {
            try {
                group.pause(status -> {
                    if (status) {
                        monitor.increment(status);
                    }

                    if (monitor.isDone() && monitor.getResult()) {
                        UiThreadExecutor.getInstance().execute(this::updateState);
                    }
                });
            }
            catch (OcException e) {
                e.printStackTrace();
            }
        }
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
    public void onCurrentZonePlaylistChanged() {
        updateInUiThread();
    }

    @Override
    public void onCurrentGroupLoopModeChanged(LoopMode loopMode) {
        updateInUiThread();
    }

    @Override
    public void onCurrentGroupShuffleModeChanged(ShuffleMode shuffleMode) {
        updateInUiThread();
    }

    @Override
    public void OnCurrentGroupStateChanged() {
        updateInUiThread();
    }

    @Override // NetworkTreeAdapterListener
    public IoTDevice getIoTDeviceFromPlayer(IoTPlayer player) {
        if (player == null || player.getHostName() == null || mAllPlayManager == null) {
            return null;
        }
        return mAllPlayManager.getDeviceByHostName(player.getPlayerHost());
    }

    @Override // NetworkTreeAdapterListener
    public void onIoTElementSelected(IoTRepository element) {
        if (mOnGroupSelectedListener != null) {
            mOnGroupSelectedListener.onZoneSelected(element);
        }
    }

    @Override
    public void onIoTElementMenuSelected(IoTRepository element) {
        if (mOnGroupSelectedListener == null) {
            return;
        }
        mOnGroupSelectedListener.onZoneMenuSelected(element);
    }

    @Override // NetworkTreeAdapterListener
    public BaseActivity getBaseActivity() {
        return mBaseActivity;
    }

    @Override // OnBluetoothListener
    public void onBluetoothAdapterStateChanged(IoTDevice device, IoTBluetoothAdapterState state) {
        // Bluetooth devices to display have changed
        updateInUiThread();
    }

    @Override // OnBluetoothListener
    public void onBluetoothDiscoverableStateChanged(IoTDevice device,
                                                    IoTBluetoothDiscoverableState state) {
        // no changes for the Bluetooth devices to display
    }

    @Override // OnBluetoothListener
    public void onPairedDevicesUpdated(IoTDevice device, List<IoTBluetoothDevice> devices) {
        // Bluetooth devices to display have changed
        updateInUiThread();
    }

    @Override // OnBluetoothListener
    public void onBluetoothError(IoTDevice device, IoTBluetoothError error) {
        // Bluetooth devices to display **might** have changed
        updateInUiThread();
    }

    @Override // OnBluetoothListener
    public void onConnectedBluetoothDeviceChanged(IoTDevice device,
                                                  IoTBluetoothDevice bluetoothDevice) {
        // Bluetooth devices to display have changed
        updateInUiThread();
    }

    @Override // OnBluetoothListener
    public void onBluetoothScanStateChanged(IoTDevice device, boolean scanning) {
        // no changes for the Bluetooth devices to display
    }

    @Override // OnBluetoothListener
    public void onBluetoothDeviceDiscovered(IoTDevice device, IoTBluetoothDevice scanned) {
        // no changes for the Bluetooth devices to display
    }

    @Override // OnBluetoothListener
    public void onBluetoothPairStateUpdated(IoTDevice device, IoTBluetoothDevice pairDevice,
                                            boolean paired) {
        // Bluetooth devices to display have changed
        updateInUiThread();
    }

    /**
     * <p>This method fetches the latest list of groups and devices from the IoTService.</p>
     */
    private List<? extends IoTRepository> fetchDevices() {
        return IoTService.getInstance().getAvailableGroups();
    }

    /**
     * <p>This method updates the UI with the given list.</p>
     */
    private void updateTreeView(List<? extends IoTRepository> devices) {
        boolean isEmpty = devices == null || devices.isEmpty();

        // display the tree or a message depending on the list being empty or not.
        mDeviceTree.setVisibility(!isEmpty ? View.VISIBLE : View.GONE);
        mNoDevicesView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        // update the adapter content
        if (mAdapter != null) {
            if (!isEmpty) {
                Collections.sort(devices);
            }
            mAdapter.setList(devices);
        }
    }

    /**
     * <p>Called when the user manually request the tree list to be refreshed using the Swipe Refresh Layout. This
     * method empties the tree, forces the discovery of devices and ends the discovery progress after 500ms.</p>
     *
     * @param refreshLayout
     *         the layout to
     */
    private void onRefreshTree(SwipeRefreshLayout refreshLayout) {
        // emptying UI
        updateTreeView(null);
        // forcing a scan
        IoTService.getInstance().forceScan();
        // stopping the layout progress after 500ms
        UiThreadExecutor.getInstance().executeAtDelayTime(() -> refreshLayout.setRefreshing(false), 500);
    }

    /**
     * <p>This method updates the pause all button depending on the current zone playing states.</p>
     */
    private void updatePlayingState() {
        List<IoTGroup> groups = mAllPlayManager.getGroups();
        boolean zonePlaying = false;

        for (IoTGroup zone : groups) {
            if ((zone.getCurrentItem() != null)) {
                if (zone.getPlayerState() == PlayState.kPlaying) {
                    zonePlaying = true;
                }
            }
        }

        mPauseAllButtonLinearLayout.setEnabled(zonePlaying);
        float alpha = (zonePlaying) ? 1.0f : 0.5f;
        mPauseAllButtonLinearLayout.setAlpha(alpha);
        mPauseAllButtonLinearLayout.setVisibility(groups.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * <p>This method initialises the view which informs the user that no speaker has been found. That view
     * corresponds to the layout {@link R.layout#empty_view empty_view}.</p>
     *
     * @param rootView
     *         the parent view which contains the empty view.
     *
     * @return the "empty" view once initialised.
     */
    private View initNoDeviceView(View rootView) {
        View emptyView = rootView.findViewById(R.id.empty_view_layout);

        // set image
        ImageView emptyIcon = emptyView.findViewById(R.id.empty_view_icon);
        if (emptyIcon != null) {
            emptyIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_speaker_list_empty,
                                                                   null));
        }

        // set text
        TextView emptyTextView = emptyView.findViewById(R.id.empty_view_text);
        if (emptyTextView != null) {
            emptyTextView.setText(R.string.empty_list_text);
        }

        // set subtext
        TextView emptyDetailTextView = emptyView.findViewById(R.id.empty_view_detail_text);
        if (emptyDetailTextView != null) {
            emptyDetailTextView.setText(R.string.empty_list_detail_text);
        }

        return emptyView;
    }

}
