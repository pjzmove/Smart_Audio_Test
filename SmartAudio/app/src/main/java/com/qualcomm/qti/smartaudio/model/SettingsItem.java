/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model;

import android.support.annotation.IntDef;

import com.qualcomm.qti.smartaudio.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

import static com.qualcomm.qti.smartaudio.view.SettingsItemView.Types;

/**
 * <p>This class encapsulates the data of the settings items which can be displayed within the
 * UI.</p>
 * <p>Each item has an id, a title a description - which can be <code>null</code> or empty, a view
 * type (see
 * {@link Types Types} and UI related states (see {@link ItemStates ItemStates}).</p>
 * <p>All items are initialised to be displayed, enabled and not updating.</p>
 * <p>This class only keeps data and does not interact with the UI. After setting up new values for
 * an item, the
 * Ui update process is not done by this class.</p>
 */
public class SettingsItem {

    /**
     * An id to identify the item.
     */
    private int mId;
    /**
     * The title to display for the settings item.
     */
    private String mTitle;
    /**
     * An optional description to display for the item.
     */
    private String mDescription;
    /**
     * An optional content description for the item.
     */
    private String mContentDescription;
    /**
     * The time of view to display for this item.
     */
    private @Types
    int mType;
    /**
     * To keep the value
     */
    private Object mValue = null;
    /**
     * To keep all - display - states of this item.
     */
    private boolean[] mStates = new boolean[ItemStates.COUNT];

    /**
     * <p>All display states a child can have.</p>
     */
    @IntDef({ItemStates.DISPLAYED, ItemStates.ENABLED, ItemStates.UPDATING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ItemStates {

        /**
         * The user can see the item.
         */
        int DISPLAYED = 0;
        /**
         * The user can interact with the item.
         */
        int ENABLED = 1;
        /**
         * The item displays an indeterminate progress bar indicating an updating state.
         */
        int UPDATING = 2;
        /**
         * The number of states, this is not part of the enumeration.
         */
        int COUNT = 3;
    }

    /**
     * <p>To create a new settings item.</p>
     *
     * @param id
     *         The item id.
     * @param title
     *         The title to display for this settings item.
     * @param description
     *         An optional description.
     * @param type
     *         The type of view to display for this item, one of {@link Types Types}.
     * @param contentDescription
     *         The content description of the item.
     */
    public SettingsItem(int id, String title, String description, @Types int type, String contentDescription) {
        this(id, title, description, type, null, contentDescription);
    }

    /**
     * <p>To create a new settings item.</p>
     *
     * @param id
     *         The item id.
     * @param title
     *         The title to display for this settings item.
     * @param description
     *         An optional description.
     * @param type
     *         The type of view to display for this item, one of {@link Types Types}.
     * @param value
     *         The value to display for the settings. Depending on the {@link Types
     *         <code>type</code>} this value will be interpreted as follows:
     *         <ul>
     *         <li>{@link Types#DEFAULT DEFAULT}: as an Object.</li>
     *         <li>{@link Types#OPTIONS OPTIONS}: as an {@link Object Object}} of type {@link Integer
     *         Integer}
     *         corresponding to an ID referenced in {@link R.menu R.menu} for the options menu to
     *         display.</li>
     *         <li>{@link Types#SWITCH SWITCH}: as an {@link Object Object}} of type {@link Boolean Boolean}
     *         corresponding to the state of the switch.</li>
     *         </ul>
     * @param contentDescription
     *         The content description of the item.
     */
    public SettingsItem(int id, String title, String description, @Types int type, Object value,
                        String contentDescription) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mType = type;
        mContentDescription = contentDescription;
        initValue(value);
        initStates();
    }

    /**
     * <p>To set the description to display.</p>
     *
     * @param description
     *         The description to display, can be <code>null</code>.
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * <p>To set the content description of the item.</p>
     *
     * @param contentDescription
     *         The content description to set, can be <code>null</code>.
     */
    public void setContentDescription(String contentDescription) {
        this.mContentDescription = contentDescription;
    }

    /**
     * <p>To set the status of the given item state.</p>
     *
     * @param state
     *         The state to set up. One of {@link ItemStates ItemStates}.
     * @param value
     *         The status of the state.
     */
    public void setState(@ItemStates int state, boolean value) {
        mStates[state] = value;
    }

    /**
     * <p>To change the value of the item.</p>
     * <p>Depending on the type if the item, the value is interpreted as follows:
     * <ul>
     * <li>{@link Types#DEFAULT DEFAULT}: as an Object.</li>
     * <li>{@link Types#OPTIONS OPTIONS}: as an {@link Object Object}} of type {@link Integer
     * Integer}
     * corresponding to an ID referenced in {@link R.menu R.menu} for the options menu to display
     * .</li>
     * <li>{@link Types#SWITCH SWITCH}: as an {@link Object Object}} of type {@link Boolean Boolean}
     * corresponding to the state of the switch.</li>
     * </ul></p>
     *
     * @param value
     *         The new value of the item.
     */
    public void setValue(Object value) {
        initValue(value);
    }

    /**
     * <p>To get the settings item id.</p>
     *
     * @return The id as given when building the object.
     */
    public int getId() {
        return mId;
    }

    /**
     * <p>To get the title of the settings item.</p>
     *
     * @return The title as given when building the instance.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * <p>To get the description of the item.</p>
     *
     * @return The description as set when creating the instance or with {@link
     * #setDescription(String) setDescription(String)}. This can be null.
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * <p>To get the content description of the item.</p>
     *
     * @return The description as set when creating the instance or with {@link
     * #setContentDescription(String) setContentDescription(String)}. This can be null.
     */
    public String getContentDescription() {
        return mContentDescription;
    }

    /**
     * <p>To get the description of the item. If the current description of the item is
     * <code>null</code> this
     * returns the value.</p>
     *
     * @return The description as set when creating the instance or with {@link
     * #setDescription(String) setDescription(String)}, if the description is null, this method
     * returns the
     * <code>value</code> of the item as a {@link String String}. The <code>value</code> can be set
     * when creating the
     * instance by using {@link #SettingsItem(int, String, String, int, Object, String) SettingsItem(int,
     * String, String, int, Object, String)} or can be set using {@link #setValue(Object) setValue(Object)}.
     */
    public String getDescriptionOrValue() {
        return mDescription == null ? mValue.toString() : mDescription;
    }

    /**
     * <p>To get the view type this item item should be displayed in.</p>
     *
     * @return The type as one of {@link Types Types}.
     */
    public @Types
    int getType() {
        return mType;
    }

    /**
     * <p>To get the status of the given state.</p>
     *
     * @param state
     *         The state to get the status of, one of {@link ItemStates itemStates}.
     *
     * @return The display state as one of {@link ItemStates ItemStates}.
     */
    public boolean getState(@ItemStates int state) {
        return mStates[state];
    }

    /**
     * <p>To get the current value of the item.</p>
     *
     * @return The value of the item as set with {@link #SettingsItem(int, String, String, int,
     * Object, String) SettingsItem(int, String, String, int, Object, String)} or {@link #setValue(Object)
     * setValue(Object)}.
     */
    public Object getValue() {
        return mValue;
    }

    /**
     * <p>To get the current value of the item as an {@link Integer Integer}.</p>
     *
     * @return the cast value or <code>-1</code> if the value couldn't be cast.
     */
    public int getIntValue() {
        return mValue instanceof Integer ? (int) mValue : -1;
    }

    /**
     * <p>To get the current value of the item as an {@link Boolean Boolean}.</p>
     *
     * @return the cast value or <code>false</code> if the value couldn't be cast.
     */
    public boolean getBooleanValue() {
        return mValue instanceof Boolean && (boolean) mValue;
    }

    /**
     * <p>To get the current value of the item as an {@link String String}.</p>
     *
     * @return the cast value or <code>""</code> if the value couldn't be cast.
     */
    public String getStringValue() {
        return mValue instanceof String ? (String) mValue : "";
    }

    @Override // Object
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SettingsItem childData = (SettingsItem) o;
        return mId == childData.mId
                && Objects.equals(mTitle, childData.mTitle)
                && Objects.equals(mDescription, childData.mDescription);
    }

    /**
     * <p>To initialise the content of the item value depending on the type.</p>
     * <p>The given value will be interpreted as follows:
     * <ul>
     * <li>{@link Types#DEFAULT DEFAULT}: as an Object.</li>
     * <li>{@link Types#OPTIONS OPTIONS}: as an {@link Object Object}} of type {@link Integer
     * Integer}
     * corresponding to an ID referenced in {@link R.menu R.menu} for the options menu to
     * display.<br/>
     * <i>Default value:</i> {@link R.menu#settings_item_default_menu settings_item_default_menu}.</li>
     * <li>{@link Types#SWITCH SWITCH}: as an {@link Object Object}} of type {@link Boolean Boolean}
     * corresponding to the state of the switch.<br/>
     * <i>Default value:</i> <code>false</code>.</li>
     * </ul>
     * </p>
     *
     * @param value
     *         The value to display for the item.
     */
    private void initValue(Object value) {
        switch (mType) {
            case Types.DEFAULT:
                mValue = value;
            case Types.OPTIONS:
                mValue = value instanceof Integer ? value : R.menu.settings_item_default_menu;
                break;
            case Types.SWITCH:
                mValue = value instanceof Boolean ? value : false;
                break;
        }
    }

    /**
     * <p>This method initialises all the {@link ItemStates ItemStates} states of the item as
     * follows:
     * <ul>
     * <li>Item is displayed,</li>
     * <li>Item is enabled,</li>
     * <li>Item is not updating.</li>
     * </ul></p>
     */
    private void initStates() {
        mStates[ItemStates.DISPLAYED] = true;
        mStates[ItemStates.ENABLED] = true;
        mStates[ItemStates.UPDATING] = false;
    }

}