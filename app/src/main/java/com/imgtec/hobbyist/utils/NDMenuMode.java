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

/**
 * Enumeration containing states of UI, governing options available in navigation drawer menu.
 * UI has three possible states:
 * - Setup - when connected to device's WiFi and setting it up
 * - Interactive - when connected to Internet and interacting with one of the devices through Flow
 * - Initial - when neither of the above two are happening
 */
public enum NDMenuMode {

  Initial, Setup, Interactive;

  private static NDMenuMode mode = NDMenuMode.Initial;

  public static NDMenuMode getMode() {
    return mode;
  }

  public static void setMode(NDMenuMode mode) {
    NDMenuMode.mode = mode;
  }
}