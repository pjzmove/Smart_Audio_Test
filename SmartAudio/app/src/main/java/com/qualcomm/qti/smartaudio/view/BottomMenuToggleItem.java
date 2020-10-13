/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */
package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
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


public class BottomMenuToggleItem extends ConstraintLayout {

    private ConstraintLayout mLayout;
    private ImageView mIcon;
    private TextView mTextView;
    private ImageView mToggleIcon;
    private boolean mToggled = false;

    public BottomMenuToggleItem(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public BottomMenuToggleItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public BottomMenuToggleItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public BottomMenuToggleItem(Context context, @StringRes int stringId, @DrawableRes int iconId,
                                View.OnClickListener l) {
        super(context);
        init(context, null, 0, 0);
        this.setText(stringId);
        this.setIcon(iconId);
        this.setOnClickListener(l);
        this.setContentDescription(getResources().getString(stringId));
    }

    public BottomMenuToggleItem(Context context, String text, int iconId, View.OnClickListener l) {
        super(context);
        init(context, null, 0, 0);
        this.setText(text);
        this.setIcon(iconId);
        this.setOnClickListener(l);
        this.setContentDescription(text);
    }

    private void init(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View mainView = layoutInflater.inflate(R.layout.bottom_menu_toggle_item, this, true);

        mIcon = mainView.findViewById(R.id.bottom_menu_icon_image_view);
        mTextView = mainView.findViewById(R.id.bottom_menu_text_view);
        mToggleIcon = mainView.findViewById(R.id.bottom_menu_toggle_image_view);
        mLayout = mainView.findViewById(R.id.bottom_menu_toggle_layout);

        if (attrs != null) {
            TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BottomMenuToggleItem,
                                                                         defStyleAttr, defStyleRes);
            try {
                String text = array.getString(R.styleable.BottomMenuToggleItem_toggleItemText);
                int iconId = array.getResourceId(R.styleable.BottomMenuToggleItem_toggleLeadingIcon,
                                                 R.drawable.ic_networkmap_23dp);
                mTextView.setText(text);
                mIcon.setImageResource(iconId);
                setToggledIcon();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                array.recycle();
            }
        }
    }

    public void setToggled(boolean toggled) {
        this.mToggled = toggled;
        setToggledIcon();
    }

    public void setText(String text) {
        if (mTextView == null) {
            return;
        }
        this.mTextView.setText(text);
    }

    public void setText(int stringId) {
        if (mTextView == null) {
            return;
        }
        this.mTextView.setText(stringId);
    }

    public void setIcon(int drawableId) {
        if (mIcon == null) {
            return;
        }
        mIcon.setImageResource(drawableId);
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener l) {
        if (mLayout == null) {
            return;
        }
        mLayout.setOnClickListener(l);
    }

    private void setToggledIcon() {
        mToggleIcon.setVisibility(mToggled ? View.VISIBLE : View.GONE);
    }
}
