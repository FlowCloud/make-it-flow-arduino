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

import java.util.ArrayList;
import java.util.List;

/**
 * Constant values used across the app.
 */
public class Constants {

  public static boolean WIFIRE_BOARD_REQUESTS_MODE = true; //false for testing on apiary mocks
  public static boolean HOCKEY_APP_ENABLED = true; //false for store version!
  public static int DEFAULT_MAXIMUM_FIELD_CHARACTERS_COUNT = 32;
  public static int DEFAULT_KEY_CHARACTERS_COUNT = 16;
  public static int DEFAULT_MAXIMUM_FLOW_REST_URL_CHARACTERS_COUNT = 47;
  public static final int WEP_64_BIT_SECRET_KEY_HEXADECIMAL_LENGTH = 10; //Expected length of Wep key
  public static final int FLOW_ACCOUNT_MINIMUM_CHARACTERS_COUNT = 5;
  /**
   * All WiFire boards have MAC addresses beginning with this prefix.
   */
  public static final String BOARD_MAC_ADDRESS_PREFIX = "00:1e:c0"; //board's MAC address must begin with this number
  public static final List<String> DEVICE_TYPES = new ArrayList<>();
  static {
    DEVICE_TYPES.add("WiFire");
  }
  public static final int TWO_SECONDS_MILLIS = 2000;
  public static final int TEN_SECONDS_MILLIS = 10000;
  public static final int THIRTY_SECONDS_MILLIS = 30000;
  public static final int SIXTY_SECONDS_MILLIS = 60000;

  public static String FLOW_TROUBLESHOOTING_URL = "http://flow.imgtec.com/developers/help/wifire/trouble-shooting";
  public static String FLOW_FORUM_URL = "http://forum.imgtec.com/categories/flow-developers";
  public static String FLOW_USING_BOARD_URL = "http://flow.imgtec.com/wifire";
  public static String FLOW_ONLINE_HELP_URL = "http://flow.imgtec.com/developers/help";
  public static String FLOW_FORGOT_PASSWORD_URL = "http://flow.imgtec.com/developers/user/password";

  /**
   * Piece of HTML code used to display help screen
   */
  public static final String WIFIRE_BUTTON_COMMANDS_HTML_TABLE = "<table cellpadding=\"4\" cellspacing=\"0\" border=\"2\" style=\"color: #000000;\">\n" +
      "                            <thead>\n" +
      "                                <tr>\n" +
      "                                    <th style=\"width: 33%\"><strong>Required mode</strong></th>\n" +
      "                                    <th style=\"width: 33%\"><strong>When</strong></th>\n" +
      "                                    <th style=\"width: 33\\\"><strong>How</strong></th>\n" +
      "                                </tr>\n" +
      "                            </thead>\n" +
      "                            <tbody>\n" +
      "                                <tr>\n" +
      "                                    <td>Wi-Fi setup mode</td>\n" +
      "                                    <td>On power up.</td>\n" +
      "                                    <td>Hold both <strong>BTN1</strong> and <strong>BTN2</strong> down.</td>\n" +
      "                                </tr>\n" +
      "                                <tr>\n" +
      "                                    <td>Wi-Fi setup mode</td>\n" +
      "                                    <td>From Wi-Fi connected mode while running.</td>\n" +
      "                                    <td>Hold both <strong>BTN1</strong> and <strong>BTN2</strong> down and then press the Reset button.</td>\n" +
      "                                </tr>\n" +
      "                                <tr>\n" +
      "                                    <td>Wi-Fi connected mode</td>\n" +
      "                                    <td>From Setup mode and board has been configured.</td>\n" +
      "                                    <td>Press the <strong>Reset</strong> button.<br /><strong>Note</strong>: If you have not configured Wi&bull;Fire board, pressing the Reset button will not take effect and your Wi&bull;Fire board will remain in Wi-Fi setup mode (as indicated by LED1 flashing).</td>\n" +
      "                                </tr>\n" +
      "                            </tbody>\n" +
      "                            </table>";

  public static final String CLAIMED = "Claimed";
  public static final String FREE = "Free";
}
