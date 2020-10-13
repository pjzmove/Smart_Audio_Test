/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.interfaces.IFragmentControl;
import com.qualcomm.qti.iotcontrollersdk.utils.VoiceUINameHelper;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.PlayerInfo;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupInfoStateChangedListener;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GroupFragment extends BaseFragment implements OnGroupInfoStateChangedListener {

    private final static String TAG = "GroupFragment";
    private final static boolean TEST = false;
    private final static String EXTRA_KEYS_GROUP_ID = "GROUP_ID";
    private PlayerListAdapter mAdapter;
    private String mGroupId;
    private ArrayList<PlayerInfo> mCheckedPlayers;
    private ArrayList<PlayerInfo> mUncheckedPlayers;
    private LinearLayout mErrorMessageContainerView;
    private TextView mErrorMessageTextView;
    private WeakReference<IFragmentControl> mController;

    private TextView mGroupNameTextView;
    private TextView mGroupNameDetailTextView;
    private Button mSaveButton;
    private ProgressBar mProgressBar;
    private LinearLayout mDeleteButton;


    private boolean mWaitForComplete;

    public static GroupFragment newInstance(String groupId) {
        Bundle bundle = new Bundle();
        GroupFragment fragment = new GroupFragment();
        bundle.putString(EXTRA_KEYS_GROUP_ID, groupId);
        Log.d(TAG, "groupId:" + groupId);
        fragment.setArguments(bundle);
        fragment.mCheckedPlayers = new ArrayList<>();
        fragment.mUncheckedPlayers = new ArrayList<>();
        return fragment;
    }

    public void setController(IFragmentControl controller) {
        mController = new WeakReference<>(controller);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        List<PlayerInfo> playerList = getPlayersList(true);
        mAdapter = new PlayerListAdapter(playerList);

        RecyclerView recyclerView = view.findViewById(R.id.player_list);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(llm);
        DividerItemDecoration decoration = new DividerItemDecoration(recyclerView.getContext(),
                                                                     llm.getOrientation());
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);

        mGroupNameTextView = view.findViewById(R.id.group_name);
        mGroupNameDetailTextView = view.findViewById(R.id.group_name_detail);

        mErrorMessageContainerView = view.findViewById(R.id.error_container_view);
        mErrorMessageTextView = view.findViewById(R.id.input_error_message);

        View nameGrpButton = view.findViewById(R.id.name_group_button);
        nameGrpButton.setOnClickListener(v -> {
            EditGroupNameFragment fragment = EditGroupNameFragment.newInstance();
            if (mController.get() != null) {
                fragment.setController(mController.get());
                mController.get().onShowEditGroupNameFragment(fragment);
            }
        });

        Bundle bundle = getArguments();
        mGroupId = bundle.getString(EXTRA_KEYS_GROUP_ID);
        mSaveButton = getActivity().findViewById(R.id.action_button);
        mProgressBar = getActivity().findViewById(R.id.progress_status_bar);
        if (mSaveButton != null) {
            mSaveButton.setVisibility(View.VISIBLE);
            mSaveButton.setOnClickListener(v -> {
                if (saveChange()) {
                    v.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                else {
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        }

        mDeleteButton = view.findViewById(R.id.delete_group_layout);
        mDeleteButton.setOnClickListener(v -> showConfirmationDialog(OperationType.DELETE, ""));

        if (TEST) {
            TextView groupId = view.findViewById(R.id.group_id);
            groupId.setText(mGroupId);
        }
        return view;
    }

    public void updateUI(String name) {

        if (name != null && !name.isEmpty()) {
            mGroupNameTextView.setText(name);
            mGroupNameDetailTextView.setVisibility(View.GONE);
        }
        else if (name != null && name.isEmpty()) {
            mGroupNameTextView.setText(null);
            mGroupNameDetailTextView.setVisibility(View.VISIBLE);
        }

        clearInputErrorMessage();
        updateCheckbox(name);

        IoTGroup group = mAllPlayManager.getGroupById(mGroupId);
        if (group != null && !group.isSinglePlayer()) {
            mDeleteButton.setVisibility(View.VISIBLE);
        }
        else {
            mDeleteButton.setVisibility(View.GONE);
        }
    }

    private List<PlayerInfo> getPlayersList(boolean withoutEmptyGroup) {

        List<PlayerInfo> retList = IoTService.getInstance().getPlayerInfoList()
                .stream().filter(playerInfo -> {
                    if (withoutEmptyGroup) {
                        return playerInfo.isAvailable && !playerInfo.isEmptyGroup;
                    }
                    else {
                        return playerInfo.isAvailable;
                    }
                }).collect(Collectors.toList());

        for (PlayerInfo info : retList) {
            if (info.groupNames != null) {
                List<String> nameList = new ArrayList<>();
                for (String name : info.groupNames) {
                    nameList.add(VoiceUINameHelper.convert2GroupUIName(name));
                }
                info.groupNames = nameList;
            }
        }
        return retList;
    }

    private void updateCheckbox(String name) {
        if (name == null || name.isEmpty()) {
            mAdapter.updateGroupId("");
        }
        else {
            List<PlayerInfo> infoList = getPlayersList(true);
            Log.d(TAG, "infoList:" + infoList.size());
            for (PlayerInfo info : infoList) {
                if (info.groupIds == null) {
                    continue;
                }
                int idx = info.groupNames.indexOf(name);
                if (idx >= 0) {
                    mGroupId = info.groupIds.get(idx);
                    Log.d(TAG, "Group id:" + mGroupId);
                    mAdapter.updateGroupId(mGroupId);
                    break;
                }
            }
        }
    }

    private void showInputErrorMessage(String message) {
        mErrorMessageContainerView.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setText(message);
    }

    private void clearInputErrorMessage() {
        mErrorMessageContainerView.setVisibility(View.GONE);
        mErrorMessageTextView.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
        IoTService.getInstance().enableDeviceDiscovery(false);
        mSaveButton.setVisibility(View.VISIBLE);
        IoTGroup group = mAllPlayManager.getGroupById(mGroupId);
        String name = null;
        if (group != null && !group.isSinglePlayer()) {
            name = group.getName();
        }
        updateUI(name);
    }

    @Override
    public void onPause() {
        mSaveButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        IoTService.getInstance().enableDeviceDiscovery(true);
        super.onPause();
    }

    private void popBackStack() {
        UiThreadExecutor.getInstance().execute(() -> {
            if (mBaseActivity != null && mBaseActivity.getSupportFragmentManager() != null) {
                mBaseActivity.getSupportFragmentManager().popBackStackImmediate(0,
                                                                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private void updateCheckedPlayerList() {
        mCheckedPlayers.clear();
        mUncheckedPlayers.clear();

        int totalNodes = mAdapter.getItemCount();
        for (int i = 0; i < totalNodes; i++) {
            if (!mAdapter.isEnabled(i)) {
                continue;
            }

            if (mAdapter.isChecked(i)) {
                mCheckedPlayers.add(mAdapter.getItem(i));
            }
            else {
                mUncheckedPlayers.add(mAdapter.getItem(i));
            }
        }
    }

    private boolean saveChange() {
        String name = mGroupNameTextView.getText().toString();
        Log.d(TAG, "Save change:" + name);
        IoTGroup group = mAllPlayManager.getGroupById(mGroupId);
        if (name == null || name.isEmpty() || name.equalsIgnoreCase(getString(R.string.name_group_default_name))) {

            if (group != null && group.isSinglePlayer()) {
                IoTPlayer player = group.getLeadPlayer();
                if (player != null) {
                    name = player.getName();
                }
            }
            else if (group != null) {
                name = "";
            }
            else {
                IoTPlayer player = mAllPlayManager.getPlayer(mGroupId);
                if (player != null) {
                    name = player.getName();
                }
                else {
                    showInputErrorMessage("No groups or players are existed!");
                    return false;
                }
            }
        }
        else {
            name = name.trim();
        }

        if (name.isEmpty()) {
            showInputErrorMessage("Please choose a group name");
            return false;
        }

        boolean isGroupCreated = false;

        mWaitForComplete = false;

        boolean isNewGroup = true;
        String groupId = null;

        //Check if it is a new group or not
        List<PlayerInfo> playerList = getPlayersList(false);
        playerList.forEach(playerInfo -> {
            String groupNames = "";
            if (playerInfo.groupNames != null) {
                for (String n : playerInfo.groupNames) {
                    groupNames += "," + n;
                }
            }
            Log.d(TAG, String.format("[id:%s][name:%s][host:%s][group name:%s]",
                                     playerInfo.id, playerInfo.name, playerInfo.host, groupNames));
        });

        if (group != null && !group.isSinglePlayer()) {
            isNewGroup = false;
            groupId = mGroupId;
        }
        else {
            for (PlayerInfo info : playerList) {
                if (info.groupNames != null) {
                    int idx = info.groupNames.indexOf(name);
                    if (idx >= 0) {
                        groupId = info.groupIds.get(idx);
                        isNewGroup = false;
                        break;
                    }
                }
            }
        }

        updateCheckedPlayerList();

        List<String> addedPlayerIds = null;
        if (mCheckedPlayers.size() > 0) {
            addedPlayerIds = mCheckedPlayers.stream()
                    .filter(info -> (info.groupIds == null || !info.groupIds.contains(mGroupId)))
                    .map(info -> info.id)
                    .collect(Collectors.toList());
        }

        boolean isAddOperation = (addedPlayerIds != null && addedPlayerIds.size() > 0);

        List<String> removedPlayerIds = null;
        if (mUncheckedPlayers.size() > 0) {
            removedPlayerIds = mUncheckedPlayers.stream()
                    .filter(info -> (info.groupIds != null && info.groupIds.contains(mGroupId)))
                    .map(info -> info.id)
                    .collect(Collectors.toList());
        }

        boolean isRemoveOperation = (removedPlayerIds != null && removedPlayerIds.size() > 0);

        if (!isNewGroup
                && !isAddOperation && !isRemoveOperation
                && mCheckedPlayers.size() > 0 && !name.equalsIgnoreCase(group.getName())) {
            showConfirmationDialog(OperationType.RENAME, name);
            return false;
        }

        if (isNewGroup) {
            if (mCheckedPlayers.size() > 0) {
                List<String> ids = mCheckedPlayers.stream().map(info -> info.id).collect(Collectors.toList());

                String debugMsg = "Create group for player ids:";
                for (String id : ids) {
                    debugMsg += id + ";";
                }
                Log.d(TAG, debugMsg);

                if (ids.size() > 0) {

                    isGroupCreated = true;
                    name = VoiceUINameHelper.convert2DutName(name);
                    IoTService.getInstance().addPlayerInNewGroup(ids, name, success -> {
                        if (success) {
                            popBackStack();
                        }
                        else {
                            Log.d(TAG, "Add player ids in the group failed!");
                            showInputErrorMessage("Failed to add players!");
                        }
                    });
                }
                else {
                    Log.e(TAG, "player id list is empty!");
                }
            }
        }
        else {

            if (isAddOperation) {
                IoTService.getInstance().addPlayers(addedPlayerIds, groupId, status -> {
                    if (mWaitForComplete) {
                        mWaitForComplete = false;
                    }
                    else {
                        popBackStack();
                    }
                });
            }

            if (isRemoveOperation) {
                IoTService.getInstance().removePlayers(removedPlayerIds, groupId, status -> {
                    if (mWaitForComplete) {
                        mWaitForComplete = false;
                    }
                    else {
                        popBackStack();
                    }
                });
            }
            else {
                Log.d(TAG, "ID list is empty!");
            }
        }

        if (!isGroupCreated && !isAddOperation && !isRemoveOperation) {
            showInputErrorMessage("No players has been chosen!");
            return false;
        }

        if (isAddOperation && isRemoveOperation) {
            mWaitForComplete = true;
        }
        else {
            mWaitForComplete = false;
            if (!isAddOperation && !isRemoveOperation) {
                Log.w(TAG, "Empty group is created!");
            }
        }

        return true;
    }

    private class PlayerListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<PlayerInfo> mPlayerList;
        private List<Boolean> mCheckedStatus;
        private String mId;

        public PlayerListAdapter(List<PlayerInfo> data) {
            mPlayerList = data;
            mCheckedStatus = new ArrayList<>(Collections.nCopies(mPlayerList.size(), false));
            mId = mGroupId;
        }

        public void setData(List<PlayerInfo> nodes) {
            mPlayerList = nodes;
            mCheckedStatus.clear();
            mCheckedStatus = null;
            mCheckedStatus = new ArrayList<>(Collections.nCopies(mPlayerList.size(), false));
            notifyDataSetChanged();
        }

        public void updateGroupId(String id) {
            mId = id;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View view = inflater.inflate(R.layout.player_info_item, viewGroup, false);
            return new PlayerInfoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            PlayerInfoViewHolder vh = (PlayerInfoViewHolder) viewHolder;
            PlayerInfo playerInfo = mPlayerList.get(position);
            vh.playerNameView.setText(playerInfo.name);
            String host = "";
            if (playerInfo.id != null) {
                IoTPlayer player = mAllPlayManager.getPlayer(playerInfo.id);
                if (player != null) {
                    host = ControllerSdkUtils.stripHostName(player.getHostName());
                }
            }
            vh.playerIdView.setText(host);
            boolean isInGroup =
                    (mId != null && !mId.isEmpty()) && ((playerInfo.groupIds != null) && playerInfo.groupIds.contains(mId));

            vh.checkBox.setChecked(isInGroup);

            vh.checkBox.setContentDescription(getString(isInGroup ? R.string.cont_desc_group_remove_device :
                                                                R.string.cont_desc_group_add_device, playerInfo.name));

            vh.updateContentDescription(isInGroup, playerInfo.name);

            vh.checkBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        mCheckedStatus.set(position, isChecked);
                        vh.updateContentDescription(isInGroup, playerInfo.name);

                    });

        }

        @Override
        public int getItemCount() {
            if (mPlayerList == null) {
                return 0;
            }
            return mPlayerList.size();
        }

        public boolean isChecked(int position) {
            if (mCheckedStatus == null) {
                return false;
            }
            return mCheckedStatus.get(position);
        }

        public boolean isEnabled(int position) {
            return mPlayerList.get(position).isAvailable;
        }

        public PlayerInfo getItem(int position) {
            return mPlayerList.get(position);
        }
    }

    private class PlayerInfoViewHolder extends RecyclerView.ViewHolder {

        TextView playerNameView;
        TextView playerIdView;
        LinearLayout playerItemView;
        CheckBox checkBox;

        PlayerInfoViewHolder(View view) {
            super(view);
            playerItemView = view.findViewById(R.id.player_info_container);
            playerNameView = view.findViewById(R.id.player_name);
            playerIdView = view.findViewById(R.id.player_id);
            checkBox = view.findViewById(R.id.player_checkbox);

            playerItemView.setOnClickListener(subview -> {
                CheckBox checkBox = subview.findViewById(R.id.player_checkbox);
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                }
                else {
                    checkBox.setChecked(true);
                }
                clearInputErrorMessage();
            });
        }

        void updateContentDescription(boolean isSelected, String name) {
            itemView.setContentDescription(getString(R.string.cont_desc_group_device, name,
                                                     getString(isSelected ? R.string.cont_desc_selected :
                                                                       R.string.cont_desc_not_selected)));
            checkBox.setContentDescription(getString(isSelected ? R.string.cont_desc_group_remove_device :
                                                             R.string.cont_desc_group_add_device, name));
        }
    }

    @Override
    public void onGroupInfoStateChanged() {
        Log.d(TAG, "onGroupInfoStateChanged");
    }

    private final static String DIALOG_CONFIRMATION_TAG = "CONFIRMATION_DIALOG";

    private enum OperationType {
        DELETE,
        RENAME
    }

    private void showConfirmationDialog(OperationType action, String param) {
        final String title = (action == OperationType.DELETE) ? getString(R.string.delete_group) :
                getString(R.string.rename_group_dialog_title);
        final String message = (action == OperationType.DELETE) ? getString(R.string.delete_group_message) :
                getString(R.string.rename_group_dialog_message);
        final String positiveText = (action == OperationType.DELETE) ? getString(R.string.delete_button_text) :
                getString(R.string.rename_button_text);
        final String negativeText = getString(R.string.cancel_button_text);
        CustomDialogFragment customDialogFragment = CustomDialogFragment.newDialog(DIALOG_CONFIRMATION_TAG,
                                                                                   title, message, positiveText,
                                                                                   negativeText);
        customDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(String tag) {
                switch (action) {
                    case DELETE:
                        IoTService.getInstance().deleteGroup(mGroupId, success ->
                                UiThreadExecutor.getInstance().execute(() -> {
                                                                           if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                                                                               getActivity().getSupportFragmentManager()
                                                                                       .popBackStackImmediate(0,
                                                                                                              FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                                                           }
                                                                       }
                                ));
                        break;
                    case RENAME:
                        String name = VoiceUINameHelper.convert2DutName(param);
                        IoTService.getInstance().renameGroup(mGroupId, name, success ->
                                UiThreadExecutor.getInstance().execute(() -> {
                                                                           if (getActivity() != null) {
                                                                               Log.d(TAG,
                                                                                     "rename group status:" + success);
                                                                               FragmentManager manager =
                                                                                       getActivity().getSupportFragmentManager();
                                                                               manager.popBackStackImmediate(0,
                                                                                                             FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                                                           }
                                                                       }
                                ));
                        break;
                }
            }

            @Override
            public void onNegativeButtonClicked(String tag) {

            }
        });
        mBaseActivity.showDialog(customDialogFragment, DIALOG_CONFIRMATION_TAG);
    }
}
