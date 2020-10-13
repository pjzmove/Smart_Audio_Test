/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;

/**
 * <p>This view holder binds the data of an item with its view. It uses the layout
 * {@link R.layout#list_item_setup_list list_item_setup_list}.</p>
 */
public class SetupListItemViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener {

    /**
     * The text view to display the name of the item.
     */
    private final TextView mTextView;
    /**
     * <p>The image view to set up an image for the item.</p>
     */
    private final ImageView mImageView;
    /**
     * The listener to get events when the user interacts with the bound view.
     */
    private final SetupListItemViewHolderListener mListener;
    /**
     * The main view which contains all the views this holder use.
     */
    private final View mView;

    /**
     * <p>The constructor which will instantiate the views to use for this holder.</p>
     *
     * @param rowView
     *         The main view which contains all the views this holder use.
     * @param listener
     *         The instance of the parent to interact with as a listener.
     */
    public SetupListItemViewHolder(View rowView, SetupListItemViewHolderListener listener) {
        super(rowView);
        mView = rowView;
        mTextView = rowView.findViewById(R.id.setup_list_item_text);
        mImageView = rowView.findViewById(R.id.setup_list_item_image);
        itemView.setOnClickListener(this);
        mListener = listener;
    }

    @Override // View.OnClickListener
    public void onClick(View v) {
        mListener.onClickItem(this.getAdapterPosition());
    }

    /**
     * <p>This method is for refreshing all the values displayed in the corresponding view.</p>
     *
     * @param image
     *         The image to illustrate the item.
     * @param text
     *         The text to display.
     * @param contentDescription
     *         The context description to attach to the main view, the given resource ID must contain a string
     *         parameter: the given <code>text</code> will be added to it using
     *         {@link View View}.{@link View#getResources() getResources()}.
     *         {@link android.content.res.Resources#getString(int, Object...) getString(int, Object...)}.
     */
    public void refreshValues(int image, String text, int contentDescription) {
        // display image
        mImageView.setImageResource(image);
        mImageView.setVisibility(View.VISIBLE);
        // update text
        mTextView.setText(text);
        // set content description
        mView.setContentDescription(mView.getResources().getString(contentDescription, text));
    }

    /**
     * The interface to allow this class to interact with its parent.
     */
    public interface SetupListItemViewHolderListener {

        /**
         * This method is called when the user clicks on the main view of an item.
         *
         * @param position
         *         The position of the item in the list.
         */
        void onClickItem(int position);
    }
}