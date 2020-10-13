/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import static com.qualcomm.qti.smartaudio.activity.WifiScanBaseActivity.ENABLE_NETWORK_TIMEOUT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;


import com.qualcomm.qti.smartaudio.activity.MultichannelSetupActivity.OnDeviceWifiScanListChangedListener;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.activity.MultichannelSetupActivity;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupListChangedListener;
import com.qualcomm.qti.smartaudio.util.RequestAsyncTask;
import com.qualcomm.qti.smartaudio.util.Utils;

import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.AddHomeTheaterChannelData;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap.MultiChannelInfo;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.ScanInfo;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.ScanInfo.AuthType;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ChooseChannelFragment extends SetupListFragment implements CustomDialogFragment.OnCustomDialogButtonClickedListener,
        OnGroupListChangedListener, OnDeviceWifiScanListChangedListener {

    private static final String TAG = ChooseChannelFragment.class.getSimpleName();

    private HomeTheaterChannel mChannel = HomeTheaterChannel.LEFT_SURROUND;

    private Handler mHandler = null;

    private String mSoundbarID = null;

    private ChooseChannelAdapter mAdapter = null;

    private SelectSpeakerObjectAsyncTask mSelectSpeakerObjectAsyncTask = null;

    private TextView mAddedText = null;
    private TextView mGreatText = null;
    private ImageView mImageView = null;
    private WeakReference<MultichannelSetupActivity> mParentActivityRef;

    public static ChooseChannelFragment newInstance(final String tag,
                                                    final HomeTheaterChannel channel,
                                                    final String soundbarID) {
        ChooseChannelFragment fragment = new ChooseChannelFragment();
        Bundle args = new Bundle();
        args.putString(SETUP_TAG_KEY, tag);
        args.putSerializable(MultichannelSetupActivity.SETUP_TYPE_KEY, channel);
        args.putString(MultichannelSetupActivity.SOUNDBAR_ID_KEY, soundbarID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mParentActivityRef = new WeakReference<>((MultichannelSetupActivity) context);
        mChannel = (HomeTheaterChannel) getArguments().get(MultichannelSetupActivity.SETUP_TYPE_KEY);
        mSoundbarID = getArguments().getString(MultichannelSetupActivity.SOUNDBAR_ID_KEY);
        mHandler = mParentActivityRef.get().getUiHandler();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mParentActivityRef.clear();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View frameView = inflater.inflate(R.layout.frame_setup_added, mEmptyFrameLayout, true);

        mImageView = frameView.findViewById(R.id.setup_added_image);
        mGreatText = frameView.findViewById(R.id.setup_great_text);
        mAddedText = frameView.findViewById(R.id.setup_added_text);

        mActionBarTitleTextView.setText(getActionBarTitle());

        mInstructionTextView.setText(getInstruction());

        mAdapter = new ChooseChannelAdapter();
        mExpandableListView.setAdapter(mAdapter);
        mExpandableListView.setOnChildClickListener(
                (expandableListView, view1, groupID, position, l) -> {
                    final Object speakerObject = mAdapter.getChild(groupID, position);
                    selectSpeakerObject(speakerObject);
                    return true;
                });

        setNoButtonSetup();

        mBottomButton.setText(getString(R.string.not_now));

        return view;
    }

    private String getActionBarTitle() {
        switch (mChannel) {
            case SUBWOOFER:
                return getString(R.string.new_allplay_subwoofer);
            case RIGHT_SURROUND:
                return getString(R.string.add_right_surround);
            case LEFT_SURROUND:
                return getString(R.string.add_left_surround);
            case LEFT_REAR_SURROUND:
                return getString(R.string.add_left_rear_surround);
            case RIGHT_REAR_SURROUND:
                return getString(R.string.add_right_rear_surround);
            case LEFT_UPFIRING_SURROUND:
                return getString(R.string.add_left_upfiring_surround);
            case RIGHT_UPFIRING_SURROUND:
                return getString(R.string.add_right_upfiring_surround);
            case LEFT_REARUPFIRING_SURROUND:
                return getString(R.string.add_left_rear_upfiring_surround);
            case RIGHT_REARUPFIRING_SURROUND:
                return getString(R.string.add_right_rear_upfiring_surround);
            default:
                return getString(R.string.add_left_surround);
        }
    }

    private String getInstruction() {
        switch (mChannel) {
            case SUBWOOFER:
                return getString(R.string.choose_subwoofer);
            case RIGHT_SURROUND:
                return getString(R.string.choose_right_surround);
            case LEFT_SURROUND:
                return getString(R.string.choose_left_surround);
            case LEFT_REAR_SURROUND:
                return getString(R.string.choose_left_rear_surround);
            case RIGHT_REAR_SURROUND:
                return getString(R.string.choose_right_rear_surround);
            case LEFT_UPFIRING_SURROUND:
                return getString(R.string.choose_left_upfiring_surround);
            case RIGHT_UPFIRING_SURROUND:
                return getString(R.string.choose_right_upfiring_surround);
            case LEFT_REARUPFIRING_SURROUND:
                return getString(R.string.choose_left_rear_upfiring_surround);
            case RIGHT_REARUPFIRING_SURROUND:
                return getString(R.string.choose_right_rear_upfiring_surround);
            default:
                return getString(R.string.choose_left_surround);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startSearch();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopSearch();
    }

    @Override
    protected void updateState() {
        updatePlayers();
        updateScanInfo();
    }

    @Override
    public void onPositiveButtonClicked(final String tag) {
        startSearch();
    }

    @Override
    public void onNegativeButtonClicked(final String tag) {
        if (mSetupFragmentListener != null) {
            mSetupFragmentListener.onBottomButtonClicked(mTag);
        }
    }

    @Override
    public void onZoneListChanged() {
        final List<IoTPlayer> homeTheaterChannelSupportedSpeakers = getHomeTheaterChannelSupportedSpeakers();
        if ((mAdapter.numberOfPlayers() == 0) && homeTheaterChannelSupportedSpeakers.isEmpty()) {
            return;
        }
        updatePlayers();
    }

    @Override
    public void onDeviceWifiScanListChanged(List<ScanInfo> wifiScanList) {
        final List<ScanInfo> newSpeakers = getNewSpeakers(wifiScanList);
        if ((mAdapter.numberOfScanInfos() != 0) || !newSpeakers.isEmpty()) {
            updateScanInfo();
        }
        updatePlayers();
        mParentActivityRef.get().dismissWifiScanDialog();
    }

    private void startSearch() {
        mHandler.post(() -> {
            MultichannelSetupActivity parentActivity = mParentActivityRef.get();
            if (parentActivity != null) {
                parentActivity.showWifiScanDialog(getString(R.string.searching_for_speakers), null);
                parentActivity.addOnDeviceWifiScanListChangedListener(ChooseChannelFragment.this);
                parentActivity.wifiScan();
            }
        });
    }

    private void stopSearch() {
        if (mChannel != HomeTheaterChannel.SUBWOOFER) {
            mAllPlayManager.removeOnZoneListChangedListener(this);
        }
        mParentActivityRef.get().removeOnDeviceWifiScanListChangedListener(this);
        mHandler.removeCallbacksAndMessages(null);
    }

    private void updateButtonSetup() {
        if ((mAdapter.numberOfScanInfos() == 0) && (mAdapter.numberOfPlayers() == 0)) {
            mInstructionTextView.setVisibility(View.GONE);
            // updateState ui with no speaker found text and updateState adapter data
            mImageView.setImageResource(R.drawable.ic_error_question);
            mGreatText.setText(getString((mChannel == HomeTheaterChannel.SUBWOOFER)
                                                 ? R.string.unable_to_find_allplay_subwoofers_header
                                                 : R.string.unable_to_find_allplay_speakers_header));
            mAddedText.setText(getString((mChannel == HomeTheaterChannel.SUBWOOFER)
                                                 ? R.string.unable_to_find_allplay_subwoofers_message
                                                 : R.string.unable_to_find_allplay_speakers_message));

            setOneButtonSetup();
        }
        else {
            setNoButtonSetup();
        }
    }

    private void updatePlayers() {
        final List<IoTPlayer> homeTheaterChannelSupportedSpeakers = getHomeTheaterChannelSupportedSpeakers();
        final BaseActivity activity = (BaseActivity) getActivity();
        if ((activity != null) && !activity.isSaveStateCalled() && !activity.isFinishing()) {
            activity.runOnUiThread(() -> {
                if ((activity != null) && !activity.isSaveStateCalled() && !activity.isFinishing()) {
                    mAdapter.updatePlayers(homeTheaterChannelSupportedSpeakers);
                    updateButtonSetup();
                }
            });
        }
    }

    private void updateScanInfo() {
        final List<ScanInfo> newSpeakers = getNewSpeakers(mParentActivityRef.get().getWifiScanList());
        final BaseActivity activity = (BaseActivity) getActivity();
        if ((activity != null) && !activity.isSaveStateCalled() && !activity.isFinishing()) {
            activity.runOnUiThread(() -> {
                if ((activity != null) && !activity.isSaveStateCalled() && !activity.isFinishing()) {
                    mAdapter.updateScanInfos(newSpeakers);
                    updateButtonSetup();
                }
            });
        }
    }

    private List<IoTPlayer> getHomeTheaterChannelSupportedSpeakers() {
        final List<IoTPlayer> homeTheaterChannelSupportedSpeakers =
                (mChannel == HomeTheaterChannel.SUBWOOFER) ?
                        new ArrayList<>() : mAllPlayManager.getHomeTheaterChannelSupportedSpeakers();
        homeTheaterChannelSupportedSpeakers.remove(mAllPlayManager.getPlayer(mSoundbarID));
        return homeTheaterChannelSupportedSpeakers;
    }

    private List<ScanInfo> getNewSpeakers(final List<ScanInfo> scanInfos) {
        final List<ScanInfo> newScanInfos = new ArrayList<>();
        if (scanInfos == null) {
            return newScanInfos;
        }

        for (ScanInfo scanInfo : scanInfos) {
            if (!newScanInfos.contains(scanInfo)) {
                newScanInfos.add(scanInfo);
            }
            else {
                int idx = newScanInfos.indexOf(scanInfo);
                newScanInfos.get(idx).wifiQuality = scanInfo.wifiQuality;
            }
        }
        Collections.sort(newScanInfos, (lhs, rhs) ->
                (rhs.wifiQuality < lhs.wifiQuality) ? -1
                        : (rhs.wifiQuality > lhs.wifiQuality) ? 1 : rhs.SSID.compareTo(lhs.SSID));
        return newScanInfos;

    }

    private void selectSpeakerObject(final Object speakerObject) {
        stopSearch();
        if (mSelectSpeakerObjectAsyncTask == null) {
            final String title;
            switch (mChannel) {
                case LEFT_SURROUND:
                    title = getString(R.string.adding_left_surround);
                    break;
                case RIGHT_SURROUND:
                    title = getString(R.string.adding_right_surround);
                    break;
                case LEFT_REAR_SURROUND:
                    title = getString(R.string.adding_left_rear_surround);
                    break;
                case RIGHT_REAR_SURROUND:
                    title = getString(R.string.adding_right_rear_surround);
                    break;
                case LEFT_UPFIRING_SURROUND:
                    title = getString(R.string.adding_right_upfiring_surround);
                    break;
                case RIGHT_UPFIRING_SURROUND:
                    title = getString(R.string.adding_right_upfiring_surround);
                    break;
                case LEFT_REARUPFIRING_SURROUND:
                    title = getString(R.string.adding_right_rear_upfiring_surround);
                    break;
                case RIGHT_REARUPFIRING_SURROUND:
                    title = getString(R.string.adding_right_rear_upfiring_surround);
                    break;
                default:
                    title = getString(R.string.adding_subwoofer);
                    break;
            }
            mSelectSpeakerObjectAsyncTask =
                    new SelectSpeakerObjectAsyncTask(speakerObject, title);
            mBaseActivity.addTaskToQueue(mSelectSpeakerObjectAsyncTask);
        }
    }

    private class ChooseChannelAdapter extends BaseExpandableListAdapter {

        private List<IoTPlayer> mPlayers = Collections.synchronizedList(new ArrayList<>());
        private List<ScanInfo> mScanInfos = Collections.synchronizedList(new ArrayList<>());

        public void update(final List<IoTPlayer> players, final List<ScanInfo> scanInfos) {
            mPlayers.clear();
            if (players != null) {
                mPlayers.addAll(players);
            }
            mScanInfos.clear();
            if (scanInfos != null) {
                mScanInfos.addAll(scanInfos);
            }
            notifyDataSetChanged();
        }

        public void updatePlayers(final List<IoTPlayer> players) {
            mPlayers.clear();
            if (players != null) {
                mPlayers.addAll(players);
            }
            notifyDataSetChanged();
        }

        public void updateScanInfos(final List<ScanInfo> scanInfos) {
            mScanInfos.clear();
            if (scanInfos != null) {
                mScanInfos.addAll(scanInfos);
            }
            notifyDataSetChanged();
        }

        public int numberOfPlayers() {
            return mPlayers.size();
        }

        public int numberOfScanInfos() {
            return mScanInfos.size();
        }

        @Override
        public int getGroupCount() {
            int count = 0;
            count += (mPlayers.size() > 0) ? 1 : 0;
            count += (mScanInfos.size() > 0) ? 1 : 0;
            return count;
        }

        @Override
        public int getChildrenCount(int groupId) {
            if (groupId == 0) {
                return (mPlayers.size() > 0) ? mPlayers.size() : mScanInfos.size();
            }
            return mScanInfos.size();
        }

        @Override
        public Object getGroup(int groupId) {
            if (mChannel == HomeTheaterChannel.SUBWOOFER) {
                return null;
            }
            if (groupId == 0) {
                return (mPlayers.size() > 0) ? getString(R.string.speakers_on_your_network)
                        : getString(R.string.new_speakers);
            }
            return getString(R.string.new_speakers);
        }

        @Override
        public Object getChild(int groupId, int position) {
            if (groupId == 0) {
                if (mPlayers.size() > 0) {
                    return (position < mPlayers.size()) ? mPlayers.get(position) : null;
                }
            }
            return (position < mScanInfos.size()) ? mScanInfos.get(position) : null;
        }

        @Override
        public long getGroupId(int position) {
            return position;
        }

        @Override
        public long getChildId(int groupID, int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupId, boolean b, View convertView, ViewGroup viewGroup) {
            String title = (String) getGroup(groupId);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate((mChannel == HomeTheaterChannel.SUBWOOFER) ?
                                                             R.layout.list_empty_layout :
                                                             R.layout.list_group_setup_list, viewGroup, false);
            }

            if (!Utils.isStringEmpty(title)) {
                TextView groupText = (TextView) convertView.findViewById(R.id.setup_list_group_text);
                groupText.setText(title);
            }

            ExpandableListView listView = (ExpandableListView) viewGroup;
            listView.expandGroup(groupId);
            return convertView;
        }

        @Override
        public View getChildView(int groupId, int childId, boolean b, View convertView,
                                 ViewGroup viewGroup) {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_setup_list, viewGroup, false);
            }

            TextView itemText = convertView.findViewById(R.id.setup_list_item_text);
            ImageView signalLevel = convertView.findViewById(R.id.setup_list_item_image);
            signalLevel.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.wifi_signal_drawable)));

            boolean setNewSpeaker = true;
            if (groupId == 0) {
                if (mPlayers.size() > 0) {
                    signalLevel.setVisibility(View.VISIBLE);
                    signalLevel.setImageResource(R.drawable.ic_speaker_23dp);
                    itemText.setText((childId < mPlayers.size()) ? mPlayers.get(childId).getName() : new String());
                    setNewSpeaker = false;
                }
            }
            if (setNewSpeaker) {
                ScanInfo scanInfo = mScanInfos.get(childId);
                itemText.setText((childId < mScanInfos.size()) ? scanInfo.SSID : new String());
                signalLevel.setVisibility(View.VISIBLE);
                signalLevel.setImageDrawable(convertView.getContext().getDrawable(Utils.getWifiSignalResource(scanInfo.wifiQuality, scanInfo.authType != AuthType.OPEN)));
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupId, int childId) {
            return true;
        }
    }

    private class SelectSpeakerObjectAsyncTask extends RequestAsyncTask implements
            AllPlayManager.OnHomeTheaterChannelChangedListener, OnGroupListChangedListener {

        private Object mSpeakerObject = null;
        private IoTPlayer mSoundBar = null;
        private int WAIT_FOR_SURROUND_TIME = 180;
        private AddHomeTheaterChannelData mAddHomeTheaterChannelData;
        private ReentrantLock mLock;
        private Condition mCondition;

        public SelectSpeakerObjectAsyncTask(final Object speakerObject, final String progressTitle) {
            super(progressTitle, null, mBaseActivity, null);
            mSpeakerObject = speakerObject;
            mLock = new ReentrantLock();
            mCondition = mLock.newCondition();
            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                    if (mSetupListFragmentListener != null) {
                        mSetupListFragmentListener.onItemClicked(mTag, mAddHomeTheaterChannelData);
                    }
                }

                @Override
                public void onRequestFailed() {
                    mBaseActivity.showCommunicationProblem(ChooseChannelFragment.this);
                    mLock.lock();
                    mCondition.signalAll();
                    mLock.unlock();
                }
            };
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAllPlayManager.addOnHomeTheaterChannelChangedListener(this);
            mAllPlayManager.addOnZoneListChangedListener(this);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mSoundBar = mAllPlayManager.getPlayer(mSoundbarID);
            if (mSoundBar == null || mSpeakerObject == null) {
                return null;
            }
            if (mSpeakerObject instanceof IoTPlayer) {
                mAddHomeTheaterChannelData = mSoundBar
                        .addHomeTheaterChannel(mChannel, (IoTPlayer) mSpeakerObject, success -> {
                        });
            }
            else {
                ScanInfo scanInfo = (ScanInfo) mSpeakerObject;
                if (scanInfo.deviceID == null) {

                    mAddHomeTheaterChannelData = new AddHomeTheaterChannelData();
                    mAddHomeTheaterChannelData.error = discoverSurroundDeviceId(scanInfo) ? IoTError.NONE :
                            IoTError.NOT_CONNECTED;
                    if (mAddHomeTheaterChannelData.error == IoTError.NONE) {
                        mAllPlayManager.enableDiscoverSurrounds(true);
                    }
                }
                else {
                    mAddHomeTheaterChannelData = mSoundBar.addHomeTheaterChannel(mChannel,
                                                                                 scanInfo.SSID, scanInfo.deviceID,
                                                                                 success -> {
                                                                                 });
                }

            }

            mResult = (mAddHomeTheaterChannelData.error == IoTError.NONE);

            if (mResult && !mSoundBar.isHomeTheaterChannelPlayerInfoAvailable(mChannel)) {
                mLock.lock();
                try {
                    mCondition.await(WAIT_FOR_SURROUND_TIME, TimeUnit.SECONDS);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    mLock.unlock();
                }
                mResult = mSoundBar.isHomeTheaterChannelPlayerInfoAvailable(mChannel);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void param) {
            super.onPostExecute(param);
            mSelectSpeakerObjectAsyncTask = null;
            mAllPlayManager.removeOnHomeTheaterChannelChangedListener(this);
            mAllPlayManager.removeOnZoneListChangedListener(this);
        }

        @Override
        public void onZoneListChanged() {
            if (mMultiChannelSetupState == MultiChannelSetupState.REJOIN_HOME_AP) {
                IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
                if (player != null) {
                    mSoundBar = null;
                    mSoundBar = player;
                    ScanInfo scanInfo = (ScanInfo) mSpeakerObject;
                    scanInfo.deviceID = mSurroundDeviceId;
                    Log.d(TAG, "Send add channel request with device id:" + mSurroundDeviceId);
                    mAddHomeTheaterChannelData = mSoundBar.addHomeTheaterChannel(mChannel,
                                                                                 scanInfo.SSID, scanInfo.deviceID,
                                                                                 success -> {
                                                                                     if (!success) {
                                                                                         mLock.lock();
                                                                                     }
                                                                                     try {
                                                                                         mCondition.signal();
                                                                                     }
                                                                                     finally {
                                                                                         mLock.unlock();
                                                                                     }
                                                                                 });
                }
            }
        }

        @Override
        public void onHomeTheaterChannelUpdate(IoTPlayer player, HomeTheaterChannelMap channelMap) {
            Log.d(TAG,
                  "onHomeTheaterChannelUpdate player id:+" + player.getPlayerId() + ",mSoundbarID:" + mSoundbarID +
                          ", current channel id:" + mChannel);
            if (player != null && player.getPlayerId().equalsIgnoreCase(mSoundbarID)) {
                channelMap.logChannelMap();
                MultiChannelInfo channelInfo = channelMap.getChannelInfo(mChannel);
                if (channelInfo != null && channelInfo.isAvailable()) {
                    Log.d(TAG, "HomeTheaterChannel update " + mChannel + " is added!");
                    mLock.lock();
                    try {
                        mCondition.signal();
                    }
                    finally {
                        mLock.unlock();
                    }
                }
            }
        }
    }

    /* The following method is to get device ID for unconfigured speakers */

    private WifiManager mWifiManager;

    private boolean discoverSurroundDeviceId(ScanInfo info) {
        if (mParentActivityRef.get() != null && mParentActivityRef.get().getWifiManager() != null) {
            mSurroundDeviceId = null;
            mSurroundId = null;
            mMultiChannelSetupState = MultiChannelSetupState.INIT;
            mAllPlayManager.clearAllPlayers((success) -> {

            });
            mWifiManager = mParentActivityRef.get().getWifiManager();
            mParentActivityRef.get().setSurroundDiscoveryListener(mSurroundDiscoveryListener);
            return connectWiFi(info);
        }
        return false;
    }

    private WifiConfiguration findConfiguration(String ssid) {
        if ((ssid == null) || (ssid.isEmpty())) {
            return null;
        }

        List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
        if (configurations == null) {
            return null;
        }
        for (WifiConfiguration configuration : configurations) {
            if (ssid.equals(Utils.stripSSIDQuotes(configuration.SSID))) {
                return configuration;
            }
        }

        return null;
    }

    private boolean connectWiFi(ScanInfo info) {
        try {

            String joiningSsid = info.SSID;

            if (info.authType != AuthType.OPEN) {
                Log.e(TAG, "Scanned speaker is not open Wi-Fi network");
                return false;
            }
            else {
                Log.v(TAG, "Item clicked, SSID " + joiningSsid);
                WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = "\"" + joiningSsid + "\"";
                conf.status = WifiConfiguration.Status.ENABLED;
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.clear();
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                WifiConfiguration wifiConfiguration = findConfiguration(joiningSsid);
                boolean shouldUpdate = (wifiConfiguration != null);
                if (shouldUpdate) {
                    int networkID = mWifiManager.updateNetwork(wifiConfiguration);
                    if ((networkID < 0) && (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                        networkID = wifiConfiguration.networkId;
                    }
                    Log.d(TAG,
                          "[connectScanInfo] update network SSID=" + conf.SSID + ", network id=" + networkID);
                }
                else {
                    int networkID = mWifiManager.addNetwork(conf);
                    Log.d(TAG,
                          "[connectScanInfo] add network SSID=" + conf.SSID + ", network id=" + networkID);
                }

                List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();

                boolean isConnecting = false;
                for (WifiConfiguration config : list) {
                    if (config.SSID != null && config.SSID.equals("\"" + joiningSsid + "\"")) {
                        Log.v(TAG, "WifiConfiguration SSID " + config.SSID);

                        mParentActivityRef.get().setOnboardingSpeakerSsid(joiningSsid);
                        mParentActivityRef.get().routeWifiTraffic(config.SSID);

                        boolean isDisconnected = mWifiManager.disconnect();
                        Log.v(TAG, "isDisconnected : " + isDisconnected);

                        mWifiManager.enableNetwork(config.networkId, true);

                        isConnecting = true;

                        mHandler.postDelayed(() -> mWifiManager.enableNetwork(config.networkId, true), 1000);
                        mHandler.postDelayed(mParentActivityRef.get().getRunnable(), ENABLE_NETWORK_TIMEOUT);
                        //Don't need to call reconnect() method here
                        break;
                    }
                }
                return isConnecting;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface SurroundDiscoveryListener {

        void onWifiNetworkChanged(String ssid, boolean connected);

        void onSurroundSpeakerDiscovered(String playerId, String deviceId);
    }

    enum MultiChannelSetupState {
        INIT,
        ONBOARDED_SURROUND_SOFT_AP,
        DEVICE_ID_DISCOVERED,
        REJOIN_HOME_AP
    }

    private String mSurroundId;
    private String mSurroundDeviceId;
    private MultiChannelSetupState mMultiChannelSetupState = MultiChannelSetupState.INIT;


    private SurroundDiscoveryListener mSurroundDiscoveryListener = new SurroundDiscoveryListener() {

        @Override
        public synchronized void onWifiNetworkChanged(String ssid, boolean connected) {
            boolean onboardedSoftAp =
                    (connected && (ssid != null && ssid.equalsIgnoreCase(mParentActivityRef.get().getOnBoardingSpeakerSsid())));

            if (mMultiChannelSetupState == MultiChannelSetupState.INIT && onboardedSoftAp) {
                mMultiChannelSetupState = MultiChannelSetupState.ONBOARDED_SURROUND_SOFT_AP;
                Log.d(TAG, "Connecting to " + mParentActivityRef.get().getOnBoardingSpeakerSsid());
            }
            else if (mMultiChannelSetupState == MultiChannelSetupState.DEVICE_ID_DISCOVERED && ssid.equalsIgnoreCase(mParentActivityRef.get().getHomeApSsid())) {
                Log.d(TAG, "Connecting to Home-AP" + mParentActivityRef.get().getHomeApSsid());
                mMultiChannelSetupState = MultiChannelSetupState.REJOIN_HOME_AP;
            }
        }

        @Override
        public void onSurroundSpeakerDiscovered(String playerId, String deviceId) {
            mSurroundDeviceId = playerId;
            mSurroundDeviceId = deviceId;
            mMultiChannelSetupState = MultiChannelSetupState.DEVICE_ID_DISCOVERED;
            mParentActivityRef.get().disconnectWifiNetwork();
        }
    };


}
