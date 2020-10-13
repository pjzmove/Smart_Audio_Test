/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.MainActivity;
import com.qualcomm.qti.smartaudio.interfaces.BottomDialogResultListener;
import com.qualcomm.qti.smartaudio.view.BottomMenuToggleItem;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.qualcomm.qti.smartaudio.activity.MainActivity.ExtraKeys;

public class SortingBottomMenuFragment extends BottomSheetDialogFragment {
    /**
     * IntDef of possible results of the BottomSheet.
     */
    @IntDef(value = {SortingBottomMenuSelection.NAVIGATE_ABOUT_PAGE, SortingBottomMenuSelection.VIEW_BY_TYPE,
            SortingBottomMenuSelection.VIEW_NETWORK_MAP})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SortingBottomMenuSelection {
        int NAVIGATE_ABOUT_PAGE = 200;
        int VIEW_BY_TYPE = 201;
        int VIEW_NETWORK_MAP = 202;
    }

    private int mSheetId;
    private int mCurrentView;
    private Context mContext;

    private LinearLayout mOptionsLayout;
    private BottomDialogResultListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    /**
     * Sets the listener for the result of the BottomSheet.
     *
     * @param listener The listener that subscribes to get the result.
     */
    public void setBottomDialogResultListener(BottomDialogResultListener listener) {
        this.mListener = listener;
    }

    private void initValues() {
        Bundle args = getArguments();
        if (args == null) return;
        mCurrentView = args.getInt(ExtraKeys.CURRENT_VIEW_EXTRA);
        mSheetId = args.getInt(ExtraKeys.SHEET_ID_EXTRA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bottom_navigation_home, container, false);

        initValues();

        mOptionsLayout = view.findViewById(R.id.bottom_menu_sorting_linear_layout);

        // region Menu Items
        BottomMenuToggleItem type = new BottomMenuToggleItem(mContext,
                R.string.view_by_type,
                R.drawable.ic_bytype_23dp,
                view1 -> returnDialogResult(SortingBottomMenuSelection.VIEW_BY_TYPE));
        BottomMenuToggleItem net = new BottomMenuToggleItem(mContext,
                R.string.view_network_map,
                R.drawable.ic_networkmap_23dp,
                view1 -> returnDialogResult(SortingBottomMenuSelection.VIEW_NETWORK_MAP));
        BottomMenuToggleItem about = new BottomMenuToggleItem(mContext,
                R.string.view_about_page,
                R.drawable.ic_about_23dp,
                view1 -> returnDialogResult(SortingBottomMenuSelection.NAVIGATE_ABOUT_PAGE));

        mOptionsLayout.addView(type);
        mOptionsLayout.addView(net);
        mOptionsLayout.addView(about);
        // endregion Menu Items

        // Enable the checkmark based on the currently selected view.
        switch (mCurrentView) {
            case MainActivity.ViewType.VIEW_BY_TYPE:
                type.setToggled(true);
                break;
            case MainActivity.ViewType.VIEW_NETWORK_MAP:
                net.setToggled(true);
                break;
        }
        return view;
    }

    /**
     * Notifies the listener of the result.
     *
     * @param resultId The id of the result (selected action).
     */
    private void returnDialogResult(int resultId) {
        if (mListener == null) return;

        mListener.onSortingBottomDialogResult(this.mSheetId, resultId);
        dismiss();
    }
}
