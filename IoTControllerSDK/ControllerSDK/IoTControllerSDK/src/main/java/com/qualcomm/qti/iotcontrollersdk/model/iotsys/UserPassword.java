/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.iotsys;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a user responds from entering password.
 */
public class UserPassword implements Parcelable {
	private String mPassword;
	private boolean mUserCancelAuth;

	public UserPassword() {
		mUserCancelAuth = true;
	}

	public UserPassword(final String password, final boolean userCancelAuth) {
		mPassword = password;
		mUserCancelAuth = userCancelAuth;
	}

	/**
	 * Get the password
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 * Get whether or not the user cancelled the authentication
	 * 
	 * @return true if cancelled, false otherwise
	 */
	public boolean userCancelAuth() {
		return mUserCancelAuth;
	}

	/**
	 * Set the password
	 * @param password
	 * 				The password entered by the user
	 * @return the current UserPassword
	 */
	public UserPassword setPassword(final String password) {
		mPassword = password;
		return this;
	}

	/**
	 * Set if the user has canceled the authentication
	 * @param userCancelAuth
	 * 				Whether or not the user canceled the authentication
	 * @return the current UserPassword
	 */
	public UserPassword setUserCancelAuth(final boolean userCancelAuth) {
		mUserCancelAuth = userCancelAuth;
		return this;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mPassword);
		dest.writeInt(mUserCancelAuth ? 0 : 1);
	}

	protected UserPassword(Parcel in) {
		mPassword = in.readString();
		mUserCancelAuth = in.readInt() == 0 ? true : false;
	}
}
