/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.adapter.SettingsAdapter;
import com.qualcomm.qti.smartaudio.model.SettingsGroup;
import com.qualcomm.qti.smartaudio.model.SettingsItem;
import com.qualcomm.qti.smartaudio.view.SettingsItemView;

import static com.qualcomm.qti.smartaudio.adapter.SettingsAdapter.SettingsAdapterListener;
import static com.qualcomm.qti.smartaudio.model.SettingsGroup.GroupStates;
import static com.qualcomm.qti.smartaudio.model.SettingsItem.ItemStates;
import static com.qualcomm.qti.smartaudio.view.SettingsItemView.Types;

/**
 * <p>An abstract fragment to manage a list of settings.</p>
 * <p>This fragment encapsulates the layout setup for its children classes.</p>
 */
public abstract class BaseSettingsFragment extends BaseFragment implements SettingsAdapterListener {

    // ====== PRIVATE FIELDS ========================================================================

    /**
     * The adapter to manage the settings and their display.
     */
    private SettingsAdapter mSettingsAdapter = new SettingsAdapter(this);
    /**
     * The text view to display the title of the fragment.
     */
    private TextView mViewTitle;

    // ====== OVERRIDE METHODS ========================================================================

    @Nullable
    @Override // Fragment
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle
                                     savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mViewTitle = view.findViewById(R.id.settings_app_bar_text_view);

        ExpandableListView expandableListView = view
                .findViewById(R.id.settings_activity_expand_listview);
        expandableListView.setAdapter(mSettingsAdapter);

        onViewCreated();

        return view;
    }

    // ====== ABSTRACT METHODS ===========================================================

    /**
     * <p>This method is </p>
     */
    protected abstract void onViewCreated();

    @Override // SettingsAdapterListener
    public abstract void initItemViewDisplay(SettingsItemView view, SettingsGroup groupData,
                                             SettingsItem data);

    @Override // SettingsAdapterListener
    public abstract boolean onSettingsItemEvent(int event, int groupId, int itemId, Object data);

    // ====== MANAGE FRAGMENT UI ===========================================================

    /**
     * <p>To set the title displayed within this fragment.</p>
     *
     * @param title
     *         The String value of the title.
     */
    protected void setViewTitle(String title) {
        mViewTitle.setText(title);
    }

    /**
     * <p>To set the title displayed within this fragment.</p>
     *
     * @param title
     *         The resource value of the title.
     * @param contentDescription
     *         The resource value of the content description for the title.
     */
    protected void setViewTitle(int title, int contentDescription) {
        mViewTitle.setText(title);
        mViewTitle.setContentDescription(getString(contentDescription));
    }

    @Override // BaseFragment
    protected void updateUI() {
        mSettingsAdapter.notifyDataSetChanged();
    }

    // ====== MANAGE SETTINGS ========================================================================

    /**
     * <p>To add a group to the data structure without a title.</p>
     * <p>This method does NOT update the UI with the new group. To update the UI, call
     * {@link #updateUI() updateUI()}.</p>
     *
     * @param groupId
     *         The id of the group.
     */
    protected void addSettingsGroup(int groupId) {
        mSettingsAdapter.addGroup(groupId);
    }

    /**
     * <p>To add a group to the data structure with a title.</p>
     * <p>This method does NOT update the UI with the new group. To update the UI, call
     * {@link #updateUI() updateUI()}.</p>
     *
     * @param groupId
     *         The id of the group.
     * @param title
     *         The title of the group to display.
     */
    protected void addSettingsGroup(int groupId, String title) {
        mSettingsAdapter.addGroup(groupId, title);
    }

    /**
     * <p>To add a settings item to the structure.</p>
     * <p>This method does NOT update the UI with the new item. To update the UI, call
     * {@link #updateUI() updateUI()}.</p>
     *
     * @param groupId
     *         The id of the group the item is attached to.
     * @param itemId
     *         The id of the item.
     * @param title
     *         The title of the item to display.
     * @param description
     *         The description of the item to display, can be null or empty.
     * @param type
     *         The type of view to display for the item, one of {@link Types Types}.
     * @param contentDescription
     *         The content description of the item.
     */
    protected void addSettingsItem(int groupId, int itemId, String title, String description,
                                   @Types int type, String contentDescription) {
        mSettingsAdapter.addItem(groupId, itemId, title, description, type, contentDescription);
    }

    /**
     * <p>To add a settings item to the structure.</p>
     * <p>This method does NOT update the UI with the new item. To update the UI, call
     * {@link #updateUI() updateUI()}.</p>
     *
     * @param groupId
     *         The id of the group the item is attached to.
     * @param itemId
     *         The id of the item.
     * @param title
     *         The title of the item to display.
     * @param description
     *         The description of the item to display, can be null or empty.
     * @param type
     *         The type of view to display for the item, one of {@link Types Types}.
     * @param value
     *         The value to display for the settings, see the {@link Types Types} to see the
     *         expected values.
     * @param contentDescription
     *         The content description of the item.
     */
    protected void addSettingsItem(int groupId, int itemId, String title, String description,
                                   @Types int type, Object value, String contentDescription) {
        mSettingsAdapter.addItem(groupId, itemId, title, description, type, value, contentDescription);
    }

    /**
     * <p>To set up the display state of an item in order to hide, disable, etc. the item.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #updateUI() updateUI()}.</p>
     *
     * @param groupId
     *         The id of the group the item belongs to.
     * @param itemId
     *         The id of the item to change the state of.
     * @param state
     *         The state to set up, one of {@link ItemStates ItemStates}.
     * @param value
     *         The value the state should be.
     */
    protected void setSettingItemState(int groupId, int itemId, @ItemStates int state,
                                       boolean value) {
        mSettingsAdapter.setItemState(groupId, itemId, state, value);
    }

    /**
     * <p>To set up the given state of the given group, see {@link GroupStates GroupStates}.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #updateUI() updateUI()}.</p>
     *
     * @param groupId
     *         The id of the group to set up the state for.
     * @param state
     *         The state to set up, one of {@link GroupStates GroupStates}.
     * @param value
     *         <code>True</code> to enable the state, <code>False</code> to disable it.
     */
    protected void setSettingsGroupState(int groupId, @GroupStates int state, boolean value) {
        mSettingsAdapter.setGroupState(groupId, state, value);
    }

    /**
     * <p>To update the value of the settings.</p>
     * <p>This method does NOT update the UI with the new value. To update the UI, call
     * {@link #updateUI() updateUI()}.</p>
     *
     * @param groupId
     *         The id of the group the item is attached to.
     * @param itemId
     *         The id if the item to update.
     * @param value
     *         The value of the item. See {@link SettingsItem#setValue(Object)
     *         setSettingsItemValue(Object)} for more information.
     */
    protected void setSettingsItemValue(int groupId, int itemId, Object value) {
        mSettingsAdapter.setItemValue(groupId, itemId, value);
    }

    /**
     * <p>To update the description of an item.</p>
     * <p>This method does NOT update the UI with the new description. To update the UI, call
     * {@link #updateUI() updateUI()}.</p>
     *
     * @param groupId
     *         The id of the group the item is attached to.
     * @param itemId
     *         The id if the item to update.
     * @param description
     *         The new description of an item.
     */
    protected void setSettingsItemDescription(int groupId, int itemId, String description) {
        mSettingsAdapter.setItemDescription(groupId, itemId, description);
    }

    /**
     * <p>To remove all items of a group.</p>
     * <p>This method does NOT update the UI with the new group state. To update the UI, call
     * {@link #updateUI() updateUI()}.</p>
     *
     * @param groupId
     *         The id of the group to delete the items from.
     */
    protected void removeAllSettingsFromGroup(int groupId) {
        mSettingsAdapter.removeItemsFromGroup(groupId);
    }
}