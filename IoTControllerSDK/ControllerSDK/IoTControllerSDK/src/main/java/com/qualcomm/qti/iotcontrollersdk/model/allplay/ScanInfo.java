/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;

/**
 * Represents a network information 
 */
public class ScanInfo {	
	/**
	 * Enumeration of network authentication types supported
	 */
	public enum AuthType {
		/**
		 * Default value
		 */
		ANY(-1),
		/**
		 * OPEN authentication
		 */
		OPEN(0),
		/**
		 * WEP authentication
		 */
		WEP(1),
		/**
		 * WPA authentication
		 */
		WPA(2),
		/**
		 * WPA2 authentication
		 */
		WPA2(3),
		/**
		 * WPS authentication
		 */
		WPS(4);
		
		public final int authTypeValue;
		
		AuthType(int value) {
			this.authTypeValue = value;
		}   
	}
	
	/**
	 * Name of the network
	 */
	public String SSID = null;
	
	/**
	 * The authentication type
	 */
	public AuthType authType;

	/**
	 * The wifi signal quality.  The value is between 0 to 5, 0 being no almost no signal, 5 being strongest signal.
	 * This parameter is only available on device that called the scan have firmware v2.4.x and above.
	 */
	public int wifiQuality = 0;

	/**
	 * This parameter is only available if it is the soft AP of an unconfigured speaker, and the unconfigured speaker have firmware v2.4.x and above.
	 * For other APs, it is empty, as it may be a regular network, or older unconfigured speaker firmware.
	 */
	public String deviceID = null;

	/**
	 * The BSSID of the network.
	 */
	public String BSSID = null;
	
	/**
	 * Is a hidden network
	 */
	public boolean isHiddenNetwork = false;
	
	/**
	 * Initialize a new ScanInfo
	 */
	public ScanInfo() {
		authType = AuthType.ANY;
	}
	
	/**
	 * Initialize a new ScanInfo
	 * @param ssid
	 * 			Name of the network
	 * @param type
	 * 			The authentication type
	 */
	public ScanInfo(final String ssid, final AuthType type) {
		SSID = ssid;
		authType = type;
	}
	
	/**
	 * Initialize a new ScanInfo
	 * @param ssid
	 * 			Name of the network
	 * @param type
	 * 			The authentication type
	 * @param quality
	 * 			The wifi quality
	 * @param isHidden
	 * 			A hidden network
	 */
	public ScanInfo(final String ssid, final AuthType type, final int quality, final boolean isHidden) {
		SSID = ssid;
		authType = type;
		wifiQuality = quality;
		isHiddenNetwork = isHidden;
	}

	private ScanInfo(final String ssid, final AuthType type, final String bssid, final int quality, final String id) {
		SSID = ssid;
		authType = type;
		BSSID = bssid;
		wifiQuality = quality;
		deviceID = id;
	}

	public static AuthType capabilitiesToAuthType(String capabilities) {
		if (capabilities.contains("WPA2")) {
			return AuthType.WPA2;
		} else if (capabilities.contains("WPA")) {
			return AuthType.WPA;
		} else if (capabilities.contains("WEP")) {
			return AuthType.WEP;
		}
		return AuthType.OPEN;
	}

	@Override
	public boolean equals(Object other) {
    if ((other == null) || !(other instanceof ScanInfo)) {
      return false;
    }

    ScanInfo otherInfo = (ScanInfo) other;
    if(otherInfo.SSID == null) return false;
    return otherInfo.SSID.equalsIgnoreCase(SSID);
  }
}
