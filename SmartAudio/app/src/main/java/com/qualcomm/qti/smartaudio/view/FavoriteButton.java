/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.qualcomm.qti.smartaudio.R;

public class FavoriteButton extends AppCompatImageButton {
	public enum Favorite {
		NONE,
		FAVORITE
	}

	private Favorite mFavorite = Favorite.NONE;
	private OnFavoriteChangedListener mOnFavoriteChangedListener = null;

	public FavoriteButton(Context context) {
		super(context);
	}

	public FavoriteButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FavoriteButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setOnFavoriteChangedListener(final OnFavoriteChangedListener listener) {
		mOnFavoriteChangedListener = listener;
	}

	public void setFavorite(final Favorite favorite) {
		setFavorite(favorite, false);
	}

	private void setFavorite(final Favorite favorite, final boolean notify) {
		synchronized (this) {
			mFavorite = favorite;
		}
		update();
		if (notify && (mOnFavoriteChangedListener != null)) {
			mOnFavoriteChangedListener.onFavoriteChanged(this, favorite);
		}
	}

	private Favorite getFavorite() {
		synchronized (this) {
			return mFavorite;
		}
	}

	protected void update() {
		final Favorite favorite = getFavorite();
		switch (favorite) {
			case NONE:
				setImageResource(R.drawable.btn_favorite);
				break;
			case FAVORITE:
				setImageResource(R.drawable.btn_favorite_selected);
				break;
		}
	}

	@Override
	public boolean performClick() {
		switch (mFavorite) {
			case NONE:
				setFavorite(Favorite.FAVORITE, true);
				break;
			case FAVORITE:
				setFavorite(Favorite.NONE, true);
				break;
		}
		return super.performClick();
	}

	public interface OnFavoriteChangedListener {
		void onFavoriteChanged(FavoriteButton button, Favorite favorite);
	}
}
