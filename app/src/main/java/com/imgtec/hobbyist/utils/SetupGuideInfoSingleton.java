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

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used during device setup process. It stores information about device and its setup progress.
 */
public class SetupGuideInfoSingleton {

  private static volatile SetupGuideInfoSingleton instance;

  public static SetupGuideInfoSingleton getInstance() {
    if (instance == null) {
      synchronized (SetupGuideInfoSingleton.class) {
        if (instance == null) {
          instance = new SetupGuideInfoSingleton();
        }
      }
    }
    return instance;
  }

  private SetupGuideInfoSingleton() {}

  /**
   * --------------------------------------------------------------------------------------------*
   */

  private String freeRegistrationKey;
  private List<Pair<String, String>> listOfKeyMACPairs = new ArrayList<>();
  private String lastOpenedLoggedInAppSSID;
  private String boardSSID;
  private String deviceName;
  private String deviceMacAddress;

  public String getFreeRegistrationKey() {
    return freeRegistrationKey;
  }

  public void setFreeRegistrationKey(String freeRegistrationKey) {
    this.freeRegistrationKey = freeRegistrationKey;
  }

  public List<Pair<String, String>> getListOfKeyMACPairs() {
    return listOfKeyMACPairs;
  }

  public void setListOfKeyMACPairs(List<Pair<String, String>> listOfKeyMACPairs) {
    this.listOfKeyMACPairs = listOfKeyMACPairs;
  }

  public String getSsid() {
    return lastOpenedLoggedInAppSSID;
  }

  public void setSsid(String ssid) {
    this.lastOpenedLoggedInAppSSID = ssid;
  }

  public String getBoardSsid() {
    return boardSSID;
  }

  public void setBoardSsid(String boardSSID) {
    this.boardSSID = boardSSID;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getDeviceMacAddress() {
    return deviceMacAddress;
  }

  public void setDeviceMacAddress(String deviceMacAddress) {
    this.deviceMacAddress = deviceMacAddress;
  }

}
