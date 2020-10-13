/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.utils;

public class PlayerNullPointerException extends NullPointerException {

  /**
    * Code for exception.
    */
    int code = 0;

   /**
    * Details for exception.
    */
    String message = null;

   /**
    * Constructs an PlayerNullPointerException without message and code.
    */
    public PlayerNullPointerException() {}

   /**
    * Constructs an PlayerNullPointerException with the specified detail message.
    *
    * @param code is exception code.
    * @param message is a detail message that describes this particular exception.
    */
    public PlayerNullPointerException(int code, String message) {
      this.code = code;
      this.message = message;
    }

   /**
    * Get code for exception.
    *
    * @return exception code.
    */
    public int getCode() {
      return code;
    }

   /**
    * Set code for exception.
    *
    * @param code is exception code.
    */
    public void setCode(int code) {
      this.code = code;
    }

   /**
    * Get code for exception.
    *
    * @return detail message that describes this particular exception.
    */
    public String getMessage() {
      return message;
    }

   /**
    * Set message for exception.
    *
    * @param message is the detail message that describes this particular exception.
    */
    public void setMessage(String message) {
      this.message = message;
    }
}

