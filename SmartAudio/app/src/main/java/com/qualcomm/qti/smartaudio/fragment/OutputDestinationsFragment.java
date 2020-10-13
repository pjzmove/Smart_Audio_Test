/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                     *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 *  ************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.model.input.InputOutputSourceItem;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;

import java.util.ArrayList;
import java.util.List;

public class OutputDestinationsFragment extends BrowseFragment implements AllPlayManager.OnPlayerOutputSelectorChangedListener,
        AdapterView.OnItemClickListener {

    private List<InputOutputSourceItem> mSelectedList;

    public static OutputDestinationsFragment newInstance(ContentSearchRequest request) {
        OutputDestinationsFragment fragment = new OutputDestinationsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, "Output Source Selection");
        bundle.putSerializable(KEY_REQUEST, request);
        fragment.setArguments(bundle);
        fragment.mSelectedList = new ArrayList<>();
        return fragment;
    }

    @Override
    public BrowseFragmentType getBrowseFragmentType() {
        return BrowseFragmentType.OUTPUT;
    }

    protected BrowseAdapter getBrowseAdapter() {
        mAdapter = new OutputBrowseAdapter();
        return mAdapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((mApp != null) && mApp.isInit()) {
            mAllPlayManager.addOnPlayerOutputSelectorChangedListener(this);
            updateState();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if ((mApp != null) && mApp.isInit()) {
            mAllPlayManager.removeOnPlayerOutputSelectorChangedListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int size = mAdapter.getCount();

        mSelectedList.clear();
        for (int i = 0; i < size; i++) {
            InputOutputSourceItem item = (InputOutputSourceItem) mAdapter.getItem(i);

            if (position == i) {
                if (!item.isSelected()) {
                    mSelectedList.add(item);
                }
            }
            else {
                if (item.isSelected()) {
                    mSelectedList.add(item);
                }
            }
        }
        setOutputSelector(mSelectedList);
    }

    @Override
    public void onPlayerOutputSelectorChanged(IoTPlayer player) {
        UiThreadExecutor.getInstance().execute(() -> {
            mContentList.clear();
            startLoader();
        });
    }

    private void setOutputSelector(List<InputOutputSourceItem> activatedList) {
        final IoTGroup group = mAllPlayManager.getCurrentGroup();
        if (group == null) {
            return;
        }

        IoTPlayer player = group.getLeadPlayer();
        if (player != null) {
            List<String> outputList = new ArrayList<>();
            activatedList.forEach(output -> outputList.add(output.getSourceName()));
            player.setOutputSourceSelector(outputList, success ->
                    Log.d(TAG, "Set output source status:" + success)
            );
        }
    }

    public class OutputBrowseAdapter extends BrowseFragment.BrowseAdapter {

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final InputOutputSourceItem contentItem = (InputOutputSourceItem) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_input_output_selection, viewGroup, false);
            }

            String title = contentItem.getTitle();
            boolean isSelected = contentItem.isSelected();
            int description = isSelected ? R.string.cont_desc_list_item_selected : R.string.cont_desc_list_item;

            convertView.setContentDescription(getString(description, title));

            TextView titleView = convertView.findViewById(R.id.browse_other_category_name);
            titleView.setText(title);
            View check = convertView.findViewById(R.id.input_source_check);
            check.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            return convertView;
        }
    }

}
