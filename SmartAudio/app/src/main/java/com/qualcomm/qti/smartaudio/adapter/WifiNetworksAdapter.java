/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.model.WifiNetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WifiNetworksAdapter extends BaseExpandableListAdapter {

    private final WifiNetworkList[] mLists = new WifiNetworkList[LIST_COUNT];
    private static final int RECOMMENDED_OFFSET = 0;
    private static final int OTHERS_OFFSET = 1;
    private static final int LIST_COUNT = 2;

    private final WifiNetwork mAddNetwork;

    public WifiNetworksAdapter(WifiNetwork addNetworkItem) {
        mAddNetwork = addNetworkItem;
        mLists[RECOMMENDED_OFFSET] = new WifiNetworkList();
        mLists[OTHERS_OFFSET] = new WifiNetworkList();
        List<WifiNetwork> list = new ArrayList<>();
        list.add(mAddNetwork);
        mLists[OTHERS_OFFSET].setList(list);
    }

    public void updateList(List<WifiNetwork> recommended, List<WifiNetwork> others) {
        mLists[RECOMMENDED_OFFSET].setList(recommended);
        mLists[OTHERS_OFFSET].setList(others);
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return LIST_COUNT;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return -1 < groupPosition && groupPosition < LIST_COUNT ? mLists[groupPosition].getCount() : 0;
    }

    @Override
    public WifiNetworkList getGroup(int groupPosition) {
        return -1 < groupPosition && groupPosition < LIST_COUNT ? mLists[groupPosition] : null;
    }

    @Override
    public WifiNetwork getChild(int groupPosition, int childPosition) {
        return -1 < groupPosition && groupPosition < LIST_COUNT ? mLists[groupPosition].getItem(childPosition) : null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_settings_headers, parent, false);
        }
        TextView headerTextView = convertView.findViewById(R.id.settings_header_text_view);
        headerTextView.setText(groupPosition == RECOMMENDED_OFFSET ? R.string.recommended_wifi : R.string.other_wifi);

        ExpandableListView listView = (ExpandableListView) parent;
        listView.expandGroup(groupPosition);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        final WifiNetwork network = getChild(groupPosition, childPosition);
        ChildHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.list_item_setup_wifi_child, parent, false);
            holder = new ChildHolder(convertView);
        }
        else {
            holder = (ChildHolder) convertView.getTag();
        }

        holder.update(parent.getContext(), network);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private class WifiNetworkList {

        private final List<WifiNetwork> mNetworks;

        WifiNetworkList() {
            mNetworks = new ArrayList<>();
        }

        WifiNetwork getItem(int index) {
            return mNetworks.get(index);
        }

        void setList(List<WifiNetwork> networks) {
            mNetworks.clear();
            mNetworks.addAll(networks);
            Collections.sort(mNetworks);
        }

        int getCount() {
            return mNetworks.size();
        }
    }

    private class ChildHolder {

        private TextView mmSsidTextView;
        private ImageView mmLockImageView;
        private ImageView mmAddImageView;
        private View mmView;

        ChildHolder(View convertView) {
            mmView = convertView;
            mmSsidTextView = convertView.findViewById(R.id.setup_wifi_child_ssid_text_view);
            mmLockImageView = convertView.findViewById(R.id.setup_wifi_child_locked_image_view);
            mmAddImageView = convertView.findViewById(R.id.setup_wifi_child_add_image_view);
            convertView.setTag(this);
        }

        public void update(Context context, WifiNetwork network) {
            boolean isAddButton = mAddNetwork.equals(network);
            mmAddImageView.setVisibility(isAddButton ? View.VISIBLE : View.GONE);
            mmSsidTextView.setText(network.getSSID());
            mmLockImageView.setVisibility(network.isOpenNetwork() ? View.GONE : View.VISIBLE);
            String description = isAddButton ? context.getString(R.string.cont_desc_button_add_network) :
                    context.getString(R.string.cont_desc_button_select_network, network.getSSID());
            mmView.setContentDescription(description);
        }
    }
}
