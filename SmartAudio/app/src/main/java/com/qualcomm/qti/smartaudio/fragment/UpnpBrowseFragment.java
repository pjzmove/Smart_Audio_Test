/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.model.ContentItem;
import com.qualcomm.qti.smartaudio.model.ContentList;
import com.qualcomm.qti.smartaudio.provider.ContentProviderException;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;

public class UpnpBrowseFragment extends BrowseFragment {

	public static UpnpBrowseFragment newInstance(final String title, final ContentSearchRequest contentSearchRequest) {
		UpnpBrowseFragment fragment = new UpnpBrowseFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_TITLE, title);
		bundle.putSerializable(KEY_REQUEST, contentSearchRequest);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected BrowseAdapter getBrowseAdapter() {
		mAdapter = new UpnpBrowseAdapter();
		return mAdapter;
	}

	@Override
	protected void traverseToEnd(Context context, ContentSearchRequest contentSearchRequest, ContentList contentList)
			throws ContentProviderException {}

	private class UpnpBrowseAdapter extends BrowseAdapter {
		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			View view = super.getView(position, convertView, viewGroup);

			ImageView imageView = (ImageView) view.findViewById(R.id.media_item_image);
			final ContentItem contentItem = (ContentItem) getItem(position);

			float side = getResources().getDimensionPixelSize(R.dimen.list_item_media_image_side);
			if (contentItem.getContentType() == ContentItem.ContentType.GROUP) {
				imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_categories_folder, null));
				side = getResources().getDimensionPixelSize(R.dimen.list_item_media_folder_side);
			}
			ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
			layoutParams.width = (int)side;
			layoutParams.height = (int)side;
			imageView.setLayoutParams(layoutParams);

			return view;
		}
	}
}
