/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;

import com.qualcomm.qti.smartaudio.R;

import java.util.ArrayList;
import java.util.List;

public class MoreOptionsPopupWindow {

    final private static String TAG = "MoreOptionsPopupWindow";

    private final ListPopupWindow mListPopupWindow;
    private final Context mContext;
    private final View mMoreOptionsView;

    private final List<MoreOptionType> mMoreOptionTypes;

    private OnMoreOptionSelectedListener mOnMoreOptionSelectedListener;

    public enum MoreOptionType {
        PLAY_NOW,
        PLAY_NEXT,
        ADD_TO_QUEUE,
        ADD_TO_FAVORITES,
        ADD_TO_PLAYLIST,
        CLEAR_QUEUE,
        REMOVE_FROM_QUEUE,
        EDIT_PLAYLIST_NAME,
        CLEAR_PLAYLIST,
        DELETE_PLAYLIST,
        REMOVE_FROM_PLAYLIST
    }

    public MoreOptionsPopupWindow(final Context context, final View moreOptionsView,
                                  final List<MoreOptionType> moreOptionTypes) {
        mContext = context;
        mListPopupWindow = new ListPopupWindow(mContext);
        mMoreOptionTypes = moreOptionTypes;
        mMoreOptionsView = moreOptionsView;
    }

    public void setOnMoreOptionSelectedListener(final OnMoreOptionSelectedListener listener) {
        mOnMoreOptionSelectedListener = listener;
    }

    public void show() {
        List<String> moreOptionStrings = moreOptionsTypesToStrings();
        List<String> contentDescriptions = getContentDescriptions(mContext, moreOptionStrings);

        mListPopupWindow.setAnchorView(mMoreOptionsView);
        mListPopupWindow.setAdapter(new OptionsAdapter(mContext,
                                                       R.layout.list_item_more_option, R.id.browse_more_option_text,
													   moreOptionStrings, contentDescriptions));
        mListPopupWindow.setWidth(mContext.getResources()
                                          .getDimensionPixelSize(R.dimen.list_popup_window_more_options_width));
        mListPopupWindow.setModal(true);
        mListPopupWindow.setHorizontalOffset(mContext.getResources()
                                                     .getDimensionPixelSize(R.dimen.list_popup_window_more_options_x_offset));

        mListPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            mListPopupWindow.dismiss();
            if (mOnMoreOptionSelectedListener != null) {
                mOnMoreOptionSelectedListener.onMoreOptionSelected(mMoreOptionTypes.get(position));
            }
        });
        mListPopupWindow.show();
    }

    private List<String> moreOptionsTypesToStrings() {
        final List<String> moreOptionStrings = new ArrayList<>();
        for (MoreOptionType moreOptionType : mMoreOptionTypes) {
            switch (moreOptionType) {
                case PLAY_NOW:
                    moreOptionStrings.add(mContext.getString(R.string.play_now));
                    break;
                case PLAY_NEXT:
                    moreOptionStrings.add(mContext.getString(R.string.play_next));
                    break;
                case ADD_TO_QUEUE:
                    moreOptionStrings.add(mContext.getString(R.string.add_to_queue));
                    break;
                case ADD_TO_PLAYLIST:
                    moreOptionStrings.add(mContext.getString(R.string.add_to_playlist));
                    break;
                case CLEAR_QUEUE:
                    moreOptionStrings.add(mContext.getString(R.string.clear_queue));
                    break;
                case REMOVE_FROM_QUEUE:
                    moreOptionStrings.add(mContext.getString(R.string.remove_from_queue));
                    break;
                case EDIT_PLAYLIST_NAME:
                    moreOptionStrings.add(mContext.getString(R.string.edit_playlist_name));
                    break;
                case CLEAR_PLAYLIST:
                    moreOptionStrings.add(mContext.getString(R.string.clear_playlist));
                    break;
                case DELETE_PLAYLIST:
                    moreOptionStrings.add(mContext.getString(R.string.delete_playlist));
                    break;
                case REMOVE_FROM_PLAYLIST:
                    moreOptionStrings.add(mContext.getString(R.string.remove_from_playlist));
                    break;
            }
        }

        return moreOptionStrings;
    }

    private List<String> getContentDescriptions(Context context, List<String> options) {
        final List<String> contentDescriptions = new ArrayList<>();
        for (String option : options) {
            contentDescriptions.add(context.getString(R.string.cont_desc_menu_option, option));
        }

        return contentDescriptions;
    }

    private class OptionsAdapter extends ArrayAdapter<String> {

        private List<String> mContentDescriptions;

        OptionsAdapter(@NonNull Context context, int resource, int textViewResourceId,
                       @NonNull List<String> objects, @Nullable List<String> descriptions) {
            super(context, resource, textViewResourceId, objects);
            mContentDescriptions = descriptions;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (mContentDescriptions != null && position < mContentDescriptions.size()) {
                view.setContentDescription(mContentDescriptions.get(position));
            }
            return view;
        }
    }

    public interface OnMoreOptionSelectedListener {
        void onMoreOptionSelected(final MoreOptionType moreOptionType);
    }
}
