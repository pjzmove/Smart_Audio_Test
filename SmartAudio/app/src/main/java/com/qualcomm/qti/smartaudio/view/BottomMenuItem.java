/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */
package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;

public class BottomMenuItem extends ConstraintLayout {

    private ConstraintLayout mLayout;
    private ImageView mIcon;
    private TextView mTextView;

    public BottomMenuItem(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public BottomMenuItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public BottomMenuItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public BottomMenuItem(Context context, @StringRes int stringId, @DrawableRes int iconId, View.OnClickListener l) {
       super(context);
       init(context, null, 0, 0);
       this.setText(stringId);
       this.setIcon(iconId);
       this.setOnClickListener(l);
    }

    public BottomMenuItem(Context context, String text, @DrawableRes  int iconId, View.OnClickListener l) {
        super(context);
        init(context, null, 0, 0);
        this.setText(text);
        this.setIcon(iconId);
        this.setOnClickListener(l);
    }

    private void init(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View mainView = layoutInflater.inflate(R.layout.bottom_menu_item, this, true);

        mIcon = mainView.findViewById(R.id.bottom_menu_icon_image_view);
        mTextView = mainView.findViewById(R.id.bottom_menu_text_view);
        mLayout = mainView.findViewById(R.id.bottom_menu_layout);

        if (attrs != null) {
            TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BottomMenuItem, defStyleAttr, defStyleRes);
            try {
                String text = array.getString(R.styleable.BottomMenuItem_itemText);
                int iconId = array.getResourceId(R.styleable.BottomMenuItem_itemIcon, R.drawable.ic_networkmap_23dp);
                mTextView.setText(text);
                mIcon.setImageResource(iconId);
            } finally {
                array.recycle();
            }
        }
    }

    public void setText(String text) {
        if (mTextView == null) return;
        mTextView.setText(text);
    }

    public void setText(int stringId) {
        if (mTextView == null) return;
        mTextView.setText(stringId);
    }

    public void setIcon(int drawableId) {
        if (mIcon == null) return;
        mIcon.setImageResource(drawableId);
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener l) {
        if (mLayout == null) return;
        mLayout.setOnClickListener(l);
    }
}