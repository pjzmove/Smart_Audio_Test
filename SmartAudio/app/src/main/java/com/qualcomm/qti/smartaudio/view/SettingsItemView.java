/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class manages the view of a settings item by displaying or hiding components depending on
 * the
 * * {@link Types Types} which had been set up. The view always contains a title and a description.
 * The description is displayed only if it has been set up.</p>
 * <p>This view can be created programmatically using
 * {@link #SettingsItemView(Context, SettingsItemViewListener) SettingsItemView(ViewGroup,
 * SettingsItemViewListener)} where the groupId and settingsId will be given back to the listener
 * when the user interacts with this view.</p>
 * <p>This view inflates the following layout:
 * {@link R.layout#list_item_device_settings_child_view list_item_device_settings_child_view}.</p>
 */
public class SettingsItemView extends FrameLayout {

    // ========================================================================
    // FIELDS

    /**
     * <p>The listener to send events when the user interacts with the component.</p>
     */
    private SettingsItemViewListener mListener;
    /**
     * <p>An id to identify the group of settings this view belongs to.</p>
     */
    private int mGroupId;
    /**
     * <p>An id to identify the setting this view represents.</p>
     */
    private int mItemId;
    /**
     * <p>The view to display the title of the settings.</p>
     */
    private TextView mTitle;
    /**
     * <p>The view to display the description of the settings.</p>
     */
    private TextView mDescription;
    /**
     * <p>The button to display when this view has {@link Types#OPTIONS OPTIONS} for type.</p>
     */
    private ImageButton mOptionsButton;
    /**
     * <p>The menu to display when the user clicks on the button when this view has {@link
     * Types#OPTIONS OPTIONS} for
     * type.</p>
     */
    private int mMenuId = R.menu.settings_item_default_menu;
    /**
     * <p>The switch to display when this view has {@link Types#SWITCH SWITCH} for type.</p>
     */
    private Switch mSwitch;
    /**
     * <p>The options menu to show and hide and the user interacts with this view when it has
     * {@link Types#OPTIONS OPTIONS} for type.</p>
     */
    private PopupMenu mOptionsMenu;
    /**
     * <p>To differentiate if the call to the switch's listener
     * {@link CompoundButton.OnCheckedChangeListener#onCheckedChanged(CompoundButton, boolean)
     * onCheckedChanged} had been made by the application or the user.</p>
     */
    private boolean isProgrammaticallyChecked = false;
    /**
     * <p>The type of this view.</p>
     */
    private @Types
    int mType = Types.DEFAULT;
    /**
     * The indeterminate progress bar to show while validating a user's request.
     */
    private ProgressBar mProgressBar;


    // ========================================================================
    // ENUMERATIONS

    /**
     * <p>All user interactions events dispatched to the {@link SettingsItemViewListener
     * SettingsItemViewListener}
     * listener through {@link SettingsItemViewListener#onEvent(int, int, int, Object) onEvent(int,
     * int, int, Object)}.</p>
     */
    @IntDef({Events.ITEM_CLICK, Events.VALUE_CHANGED, Events.OPTION_CLICK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Events {

        /**
         * <p>Used when the user interacts with the item. When used with
         * {@link SettingsItemViewListener#onEvent(int, int, int, Object) onEvent(int, int, int,
         * Object)} the {@link Object Object} is <code>null</code>.</p>
         */
        int ITEM_CLICK = 0;
        /**
         * <p>Used when the user interacts with the switch. When used with
         * {@link SettingsItemViewListener#onEvent(int, int, int, Object) onEvent(int, int, int,
         * Object)) it also sends an {@link Object Object}} of type {@link Boolean Boolean} to give the
         * new state of the switch.</p>
         */
        int VALUE_CHANGED = 1;
        /**
         * <p>Used when the user interacts with the options menu. When used with
         * {@link SettingsItemViewListener#onEvent(int, int, int, Object) onEvent(int, int, int,
         * Object)) it also sends an {@link Object Object}} of type {@link Integer Integer} which
         * represents the id of the options menu entry referenced in {@link R.id R.id} the user
         * interacts with.</p>
         */
        int OPTION_CLICK = 2;
    }

    /**
     * <p>All types this view can be. The type defines the components to display and hide within the
     * UI.</p>
     */
    @IntDef({Types.DEFAULT, Types.OPTIONS, Types.SWITCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Types {

        /**
         * <p>The default settings item view which only displays a title and a description.</p>
         */
        int DEFAULT = 0;
        /**
         * On top of the {@link Types#DEFAULT DEFAULT} this type also displays a button which opens a
         * menu when the user interacts with it.
         */
        int OPTIONS = 1;
        /**
         * On top of the {@link Types#DEFAULT DEFAULT} this type also displays a switch.
         */
        int SWITCH = 2;
    }


    // ========================================================================
    // CONSTRUCTORS

    // ----------------------------------------
    // mandatory constructors

    public SettingsItemView(Context context) {
        super(context);
        init(context);
    }

    public SettingsItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SettingsItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressWarnings("unused")
    public SettingsItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    // ----------------------------------------
    // other constructors

    /**
     * <p>To build a new instance of this component view.</p>
     *
     * @param context
     *         <p>The context this view is attached to.</p>
     * @param listener
     *         <p>The listener to receive user interaction events.</p>
     */
    public SettingsItemView(Context context, SettingsItemViewListener listener) {
        super(context);
        // create the view/layout
        init(context);
        // init values
        this.mListener = listener;
    }


    // ========================================================================
    // PUBLIC METHODS

    @Override // FrameLayout
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mTitle.setEnabled(enabled);
        mDescription.setEnabled(enabled);
        mOptionsButton.setEnabled(enabled);
        mSwitch.setEnabled(enabled);
    }

    @Override
    public void setContentDescription(CharSequence contentDescription) {
        super.setContentDescription(getResources().getString(R.string.cont_desc_setting_item, contentDescription));
        mTitle.setContentDescription(getResources().getString(R.string.cont_desc_item_title, contentDescription));
        mDescription.setContentDescription(getResources().getString(R.string.cont_desc_item_description,
                                                                    contentDescription));
        mOptionsButton.setContentDescription(getResources().getString(R.string.cont_desc_item_more_options,
                                                                      contentDescription));
        mSwitch.setContentDescription(getResources().getString(R.string.cont_desc_item_control, contentDescription));
    }

    /**
     * <p>To set the group id of the item this view represents.</p>
     *
     * @param groupId
     *         <p>Settings might be attached to a group. This id will be sent back to the
     *         {@link SettingsItemViewListener SettingsItemViewListener} listener when called.</p>
     */
    public void setGroupId(int groupId) {
        this.mGroupId = groupId;
    }

    /**
     * <p>To set the id of the item this view represents.</p>
     *
     * @param itemId
     *         <p>Settings might have a specific id. This id will be sent back to the {@link
     *         SettingsItemViewListener SettingsItemViewListener} listener when called.</p>
     */
    public void setItemId(int itemId) {
        this.mItemId = itemId;
    }

    /**
     * <p>To update the title displayed in the view.</p>
     * <p>If the given argument is null or empty, this method hide the title view.</p>
     *
     * @param title
     *         The text to display as a title.
     */
    public void setTitle(String title) {
        if (title != null && title.length() > 0) {
            mTitle.setText(title);
            mTitle.setVisibility(VISIBLE);
        }
        else {
            mTitle.setText("");
            mTitle.setVisibility(GONE);
        }
    }

    /**
     * <p>To update the description displayed in the view.</p>
     * <p>If the given argument is null or empty, this method hide the description view.</p>
     *
     * @param description
     *         The text to display within the item view.
     */
    public void setDescription(String description) {
        if (description != null && description.length() > 0) {
            mDescription.setText(description);
            mDescription.setVisibility(VISIBLE);
        }
        else {
            mDescription.setText("");
            mDescription.setVisibility(GONE);
        }
    }

    /**
     * <p>To update the options menu displayed when the user presses the options button.</p>
     * <p>If the given argument is null or empty, this method hide the description view.</p>
     *
     * @param menuId
     *         The menu resource ID to use.
     */
    public void setOptionsMenu(@IdRes int menuId) {
        mMenuId = menuId;
        createPopUpMenu(mOptionsButton);
    }

    /**
     * <p>To update the state of the switch on this view.</p>
     *
     * @param checked
     *         True for the switch to be seen as checked/on, false otherwise.
     */
    public void setSwitch(boolean checked) {
        isProgrammaticallyChecked = true;
        mSwitch.setChecked(checked);
        isProgrammaticallyChecked = false;
    }

    /**
     * <p>To hide the control of this Setting view in order to display an indeterminate progress bar
     * instead.</p>
     *
     * @param show
     *         True to show the progress bar, false to display the control.
     */
    public void showProgress(boolean show) {
        mProgressBar.setVisibility(show ? VISIBLE : GONE);
        switch (mType) {
            case Types.OPTIONS:
                mOptionsButton.setVisibility(show ? GONE : VISIBLE);
                break;
            case Types.SWITCH:
                mSwitch.setVisibility(show ? GONE : VISIBLE);
                break;
            case Types.DEFAULT:
                break;
        }
    }

    /**
     * <p>To set the type of this view.</p>
     *
     * @param type
     *         The view type as one of {@link Types Types}.
     */
    public void setType(@Types int type) {
        mType = type;
        mOptionsButton.setVisibility(type == Types.OPTIONS ? VISIBLE : GONE);
        mSwitch.setVisibility(type == Types.SWITCH ? VISIBLE : GONE);
    }

    /**
     * <p>To get the type of this view, one of {@link Types Types}.</p>
     */
    public @Types
    int getType() {
        return mType;
    }


    // ========================================================================
    // PRIVATE METHODS

    /**
     * <p>Inflate the layout used for the {@link SettingsItemView} and initialises all the view
     * components.</p>
     *
     * @param context
     *         The context this view is related to.
     */
    private void init(Context context) {
        // inflate the layout
        inflate(context, R.layout.list_item_device_settings_child_view, this);

        // bind components
        mTitle = findViewById(R.id.settings_item_title_view);
        mDescription = findViewById(R.id.settings_item_description_view);
        mOptionsButton = findViewById(R.id.settings_item_options_button);
        mSwitch = findViewById(R.id.settings_item_switch);
        mProgressBar = findViewById(R.id.settings_item_progress_bar);

        // hide all
        mTitle.setVisibility(GONE);
        mDescription.setVisibility(GONE);
        mOptionsButton.setVisibility(GONE);
        mSwitch.setVisibility(GONE);

        // set listeners
        mOptionsButton.setOnClickListener(v -> showPopUpMenu());
        mSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (isProgrammaticallyChecked) {
                // no need to dispatch the event, setChecked has been called
                return;
            }
            sendEventToListener(Events.VALUE_CHANGED, checked);
        });
        setOnClickListener(v -> sendEventToListener(Events.ITEM_CLICK, null));
    }

    /**
     * <p>This method creates an option menu with the menu sets up with
     * {@link #setOptionsMenu(int) setOptionsMenu(int)} and attach it to the given view.</p>
     *
     * @param view
     *         The view to attach the menu to.
     */
    private void createPopUpMenu(View view) {
        mOptionsMenu = new PopupMenu(getContext(), view);
        MenuInflater inflater = mOptionsMenu.getMenuInflater();
        inflater.inflate(mMenuId, mOptionsMenu.getMenu());
        mOptionsMenu.setOnMenuItemClickListener(
                item -> sendEventToListener(Events.OPTION_CLICK, item.getItemId()));
    }

    /**
     * To show the options menu if it exists.
     */
    private void showPopUpMenu() {
        if (mOptionsMenu != null) {
            mOptionsMenu.show();
        }
    }

    /**
     * <p>To send a user event to an attached listener.</p>
     *
     * @param event
     *         The event to dispatch.
     * @param data
     *         Any complementary data.
     *
     * @return True if the event was consumed, false otherwise.
     */
    private boolean sendEventToListener(@Events int event, Object data) {
        return mListener != null && mListener.onEvent(event, mGroupId, mItemId, data);
    }


    // ========================================================================
    // INTERFACES

    /**
     * <p>The listener to implement to get user interaction events from {@link SettingsItemView
     * SettingsItemView}.</p>
     */
    public interface SettingsItemViewListener {

        /**
         * <p>Called when the user interacts with specific components views from a
         * {@link SettingsItemView SettingsItemView} object. The specific components are, for instance,
         * a {@link Switch Switch} when type is {@link Types#SWITCH SWITCH} or the options menu when the
         * type is {@link Types#OPTIONS OPTIONS}.</p>
         *
         * @param event
         *         The type of event which happened, one of {@link Events Events}.
         * @param groupId
         *         The group id sets when creating the {@link SettingsItemView
         *         SettingsItemView}.
         * @param settingsId
         *         The settings id sets when creating the {@link SettingsItemView
         *         SettingsItemView}.
         * @param data
         *         Any complementary data to the events. See the events {@link Events Events} to
         *         know what to expect.
         *
         * @return Must return <code>True</code> if the event has been consumed, <code>False</code>
         * otherwise.
         */
        boolean onEvent(@Events int event, int groupId, int settingsId, Object data);
    }

}