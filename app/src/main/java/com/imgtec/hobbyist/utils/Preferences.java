/*
 * <b>Copyright 2014 by Imagination Technologies Limited
 * and/or its affiliated group companies.</b>\n
 * All rights reserved.  No part of this software, either
 * material or conceptual may be copied or distributed,
 * transmitted, transcribed, stored in a retrieval system
 * or translated into any human or computer language in any
 * form by any means, electronic, mechanical, manual or
 * other-wise, or disclosed to the third parties without the
 * express written permission of Imagination Technologies
 * Limited, Home Park Estate, Kings Langley, Hertfordshire,
 * WD4 8LZ, U.K.
 */

package com.imgtec.hobbyist.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class that handles saving some persistent values to {@link android.content.SharedPreferences} object.
 */
public class Preferences {

  public static final String SETTINGS = "SETTINGS";

  public static final String EMAIL_CREDENTIAL = "EMAIL_CREDENTIAL";

  public static final String INTERACTIVE_MODE_HAS_STARTED_AT_LEAST_ONCE = "INTERACTIVE_MODE_HAS_STARTED_AT_LEAST_ONCE";
  public static final String ROOT_URL = "ROOT_URL";
  public static final String OAUTH_KEY = "OAUTH_KEY";
  public static final String OAUTH_SECRET = "OAUTH_SECRET";

  public static final String WIFIRE_URL = "WIFIRE_URL";

  public static final String ROOT_URL_DEFAULT_VALUE = "http://ws-uat.flowworld.com";
  public static final String OAUTH_KEY_DEFAULT_VALUE = "Ph3bY5kkU4P6vmtT";
  public static final String OAUTH_SECRET_DEFAULT_VALUE = "Sd1SVBfYtGfQvUCR";

  public static final String WIFIRE_URL_APIARY_WEBSERVICE_URL = "http://hobbyistandroid.apiary-mock.com";

  public static final String WIFIRE_URL_BOARD_WEBSERVICE_URL = "https://192.168.1.25";

  /**
   * Saves a preference saying that user has entered interactive mode with a device.
   * This causes app to skip tour screens on following launches.
   */
  public static void interactiveModeHasStartedAtLeastOnce(Context appContext) {
    SharedPreferences settings = appContext.getSharedPreferences(Preferences.SETTINGS, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putBoolean(Preferences.INTERACTIVE_MODE_HAS_STARTED_AT_LEAST_ONCE, true);
    editor.commit();
  }

  /**
   * Saves license details and WiFire board url to be used by the app.
   */
  public static void saveSettings(Context appContext, String rootUrl, String oKey, String oSecret, String wifireUrl) {
    SharedPreferences settings = appContext.getSharedPreferences(Preferences.SETTINGS, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(Preferences.ROOT_URL, rootUrl);
    editor.putString(Preferences.OAUTH_KEY, oKey);
    editor.putString(Preferences.OAUTH_SECRET, oSecret);
    editor.putString(Preferences.WIFIRE_URL, wifireUrl);
    editor.commit();
  }

  /**
   * Saves user's email.
   */
  public static void saveEmailCredential(Context appContext, String emailCredential) {
    SharedPreferences settings = appContext.getSharedPreferences(Preferences.SETTINGS, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(Preferences.EMAIL_CREDENTIAL, emailCredential);
    editor.commit();
  }

}
