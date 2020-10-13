/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                     *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.model.ContentItem;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.model.input.InputOutputSourceItem;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.InputSelectorAttr;

public class InputBrowseFragment extends BrowseFragment implements AllPlayManager.OnPlayerInputSelectorChangedListener,
        AdapterView.OnItemClickListener {

    public static InputBrowseFragment newInstance(final String title, final ContentSearchRequest contentSearchRequest) {
        InputBrowseFragment fragment = new InputBrowseFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        bundle.putSerializable(KEY_REQUEST, contentSearchRequest);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public BrowseFragmentType getBrowseFragmentType() {
        return BrowseFragmentType.INPUT;
    }

    protected BrowseAdapter getBrowseAdapter() {
        mAdapter = new InputBrowseAdapter();
        return mAdapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((mApp != null) && mApp.isInit()) {
            mAllPlayManager.addOnPlayerInputSelectorChangedListener(this);
            updateState();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if ((mApp != null) && mApp.isInit()) {
            mAllPlayManager.removeOnPlayerInputSelectorChangedListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setInputSelector((InputOutputSourceItem) mAdapter.getItem(position));
    }

    @Override
    public void onPlayerInputSelectorChanged(IoTPlayer player, InputSelectorAttr input) {
        UiThreadExecutor.getInstance().execute(() -> {
                                                   mContentList.clear();
                                                   startLoader();
                                               }
        );
    }

    private void setInputSelector(final InputOutputSourceItem item) {
        final IoTGroup zone = mAllPlayManager.getCurrentGroup();
        if ((zone == null) || (item == null) || Utils.isStringEmpty(item.getID()) ||
                Utils.isStringEmpty(item.getSourceName())) {
            return;
        }
        for (IoTPlayer player : zone.getPlayers()) {
            if (player != null && item.getID().equals(player.getPlayerId())) {
                if (!item.getInput().equalsIgnoreCase(player.getActiveInputSource())) {
                    player.setInputSelector(item.getInput(), success -> {
                    });
                }
                break;
            }
        }
    }

    private class InputBrowseAdapter extends BrowseFragment.BrowseAdapter {

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final ContentItem contentItem = (ContentItem) getItem(position);

            if ((convertView == null) || !convertView.getTag().equals(contentItem.getContentType().toString())) {
                LayoutInflater inflater = (LayoutInflater) getActivity().
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_input_output_selection, viewGroup, false);
            }

            TextView titleView = convertView.findViewById(R.id.browse_other_category_name);
            titleView.setText(contentItem.getTitle());
            View check = convertView.findViewById(R.id.input_source_check);

            if (contentItem instanceof InputOutputSourceItem) {
                InputOutputSourceItem item = (InputOutputSourceItem) contentItem;
                check.setVisibility(View.VISIBLE);
            }
            else {
                check.setVisibility(View.GONE);
            }
            convertView.setTag(contentItem.getContentType().toString());
            return convertView;
        }
    }

}
