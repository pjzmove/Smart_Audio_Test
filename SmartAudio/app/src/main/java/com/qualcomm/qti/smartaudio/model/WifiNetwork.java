/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * A model to keep the attributes related to a Wifi network.
 */
public class WifiNetwork implements Comparable {

    // ========================================================================
    // PRIVATE FIELDS

    /**
     * This contains the SSID of the network.
     */
    @NonNull private String mSSID;
    /**
     * This contains the SSID of the network within doubles quotes.
     */
    private String mSSIDWithQuotes;
    /**
     * This contains the signal level of the network.
     */
    private int mLevel;
    /**
     * This is to know if the network is an open network.
     */
    private boolean mIsOpen;


    // ========================================================================
    // CONSTRUCTOR

    /**
     * <p>To build a new instance of wifiNetwork from a scan result.</p>
     *
     * @param ssid The SSID - without quotes - of the network also used as a display name.
     * @param level The WiFi level in dBm.
     * @param isOpen True if the network is an opened network, False otherwise.
     */
    public WifiNetwork(String ssid, int level, boolean isOpen) {
        this.mSSID = ssid == null ? "" : ssid; // network name
        this.mSSIDWithQuotes = formatWithQuotes(mSSID);
        this.mLevel = level; // dBm also known as RSSI
        this.mIsOpen = isOpen;
    }

    /**
     * <p>This method updates the given list of networks with the given network. If the network is already in the
     * list it updates the level of the existing one, otherwise it adds the network to the list.</p>
     *
     * @param networks
     *              The list of networks to update.
     * @param newNetwork
     *              The network to add to the list.
     */
    public static void addNetwork(List<WifiNetwork> networks, WifiNetwork newNetwork) {
        int index = networks.indexOf(newNetwork); // if not in the list, index == -1
        if (index == -1) {
            // not in the list yet
            networks.add(newNetwork);
        }
        else {
            WifiNetwork oldNetwork = networks.get(index);
            if (newNetwork.getLevel() < oldNetwork.getLevel()) {
                // the list contains a more accurate value of the level
                oldNetwork.setLevel(newNetwork.getLevel());
            }
        }
    }


    // ========================================================================
    // OVERRIDE METHODS

    @Override // Object
    public boolean equals(Object o) {
        return o instanceof WifiNetwork && Objects.equals(((WifiNetwork) o).mSSID, this.mSSID)
                && Objects.equals(((WifiNetwork) o).isOpenNetwork(), this.isOpenNetwork());
    }

    @Override // Comparable
    public int compareTo(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }

        if (!(o instanceof WifiNetwork)) {
            throw new IllegalArgumentException();
        }

        if (this.equals(o)) {
            return 0;
        }

        WifiNetwork other = (WifiNetwork) o;

        return Integer.compare(this.getLevel(), other.getLevel());
    }


    // ========================================================================
    // PUBLIC METHODS

    /**
     * <p>To get the SSID of the network without the double quotes.</p>
     *
     * @return the SSID of the network.
     */
    public String getSSID() {
        return mSSID;
    }

    /**
     * To get the SSID of the network within double quotes.
     *
     * @return the SSID of the network within double quotes.
     */
    public String getSSIDWithQuotes() {
        return mSSIDWithQuotes;
    }

    /**
     * To get the signal level of the network in dBm.
     *
     * @return the signal level in dBm.
     */
    public int getLevel() {
        return mLevel;
    }

    /**
     * To set the level of the signal.
     *
     * @param level the level in dBm.
     */
    public void setLevel(int level) {
        mLevel = level;
    }

    /**
     * To know if the network is an open network.
     *
     * @return True if the device is an open network, false otherwise.
     */
    public boolean isOpenNetwork() {
        return mIsOpen;
    }


    // ========================================================================
    // PRIVATE STATIC METHODS

    /**
     * <p>To add double quotes at the beginning and the end of a String.</p>
     *
     * @param text the text to add quotes to.
     *
     * @return The given string value formatted with double quotes.
     */
    private static String formatWithQuotes(String text) {
        return String.format("\"%s\"", text);
    }
}
