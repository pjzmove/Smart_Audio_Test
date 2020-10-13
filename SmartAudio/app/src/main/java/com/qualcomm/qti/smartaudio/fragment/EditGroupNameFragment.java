/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.interfaces.IFragmentControl;
import com.qualcomm.qti.iotcontrollersdk.utils.VoiceUINameHelper;

import java.lang.ref.WeakReference;
import java.util.List;

public class EditGroupNameFragment extends BaseFragment {

    private GroupNameItemAdapter mAdapter;
    private WeakReference<IFragmentControl> mController;
    private ImageView mSelectedNameCheckmark;


    public void setController(IFragmentControl controller) {
        mController = new WeakReference<>(controller);
    }

    public static EditGroupNameFragment newInstance() {
        EditGroupNameFragment fragment = new EditGroupNameFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_name_select, container, false);
        RecyclerView groupNameListView = view.findViewById(R.id.group_name_list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        groupNameListView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(groupNameListView.getContext(),
                                                                                layoutManager.getOrientation());
        groupNameListView.addItemDecoration(dividerItemDecoration);
        mAdapter = new GroupNameItemAdapter();
        groupNameListView.setAdapter(mAdapter);
        LinearLayout layout = view.findViewById(R.id.use_default_name);
        layout.setOnClickListener(v -> {
            if (mController != null) {
                mController.get().onGroupNameChosen("");
            }
        });
        layout = view.findViewById(R.id.use_custom_name);
        layout.setOnClickListener(v -> showCustomGroupNameDialog());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Button actionButton = getActivity().findViewById(R.id.action_button);
        ProgressBar progressBar = getActivity().findViewById(R.id.progress_status_bar);
        if (actionButton != null) {
            actionButton.setVisibility(View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Button actionButton = getActivity().findViewById(R.id.action_button);
        if (actionButton != null) {
            actionButton.setVisibility(View.VISIBLE);
        }
    }

    private class GroupNameViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mContainer;
        ImageView mGroupImage;
        TextView mTitle;
        ImageView mSelectedCheckmark;

        public GroupNameViewHolder(View v) {
            super(v);
            mContainer = v.findViewById(R.id.group_name_selection);
            mTitle = v.findViewById(R.id.group_name_item);
            mGroupImage = v.findViewById(R.id.group_image);
            mSelectedCheckmark = v.findViewById(R.id.group_selected_checkmark);
        }
    }

    private class GroupNameItemAdapter extends RecyclerView.Adapter<GroupNameViewHolder> {

        List<String> mGroupNameList;

        public GroupNameItemAdapter() {
            mGroupNameList = VoiceUINameHelper.getGroupNameList();
        }

        @NonNull
        @Override
        public GroupNameViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.group_name_item_view, viewGroup,
                                                                         false);
            GroupNameViewHolder vh = new GroupNameViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull GroupNameViewHolder viewHolder, int i) {
            String groupName = mGroupNameList.get(i);
            viewHolder.mTitle.setText(groupName);
            viewHolder.mContainer.setOnClickListener(v -> {
                if (mSelectedNameCheckmark != null) {
                    mSelectedNameCheckmark.setVisibility(View.INVISIBLE);
                }
                mSelectedNameCheckmark = viewHolder.mSelectedCheckmark;
                mSelectedNameCheckmark.setVisibility(View.VISIBLE);

                if (mController != null) {
                    mController.get().onGroupNameChosen(groupName);
                }
            });
            viewHolder.itemView.setContentDescription(getString(R.string.cont_desc_group_voice_name, groupName));
        }

        @Override
        public int getItemCount() {
            return mGroupNameList.size();
        }
    }


    /**
     * A dialog for users to input number only
     */
    private void showCustomGroupNameDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.custom_group_name_dialog_view);
        Button ok = dialog.findViewById(R.id.text_editor_ok);
        Button cancel = dialog.findViewById(R.id.text_editor_cancel);
        EditText editText = dialog.findViewById(R.id.editor_text_value);
        editText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        dialog.setCancelable(false);

        ok.setOnClickListener(v -> {
            try {
                String name = editText.getText().toString();
                if (name == null || name.isEmpty()) {
                    return;
                }
                dialog.dismiss();
                if (mController.get() != null) {
                    mController.get().onGroupNameChosen(name);
                }
            }
            catch (Exception e) {

            }
        });
        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
