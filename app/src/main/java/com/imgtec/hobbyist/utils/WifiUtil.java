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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Couple of Wifi utils.
 */
public class WifiUtil {

  private final WifiManager wifiManager;
  private final Context appContext;

  public WifiUtil(Context appContext) {
    this.appContext = appContext;
    wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
  }

  /**
   * @return current Wifi's SSID without embracing apostrophe character.
   */
  public String getCurrentWifiSSID() {
    WifiInfo wifiInfo = wifiManager.getConnectionInfo(); // WiFiInfo object always needs re-initialization
    return (wifiInfo.getSSID() == null) ? null : wifiInfo.getSSID().replace("\"", "");
  }

  public void connectToWepNetwork(String ssid, String password) {
    wifiManager.setWifiEnabled(true);
    WifiConfiguration wifiConfiguration = createWifiConfiguration(ssid, password);
    int networkId = wifiManager.addNetwork(wifiConfiguration);
    wifiManager.saveConfiguration();
    wifiManager.disconnect();
    wifiManager.enableNetwork(networkId, true);
    wifiManager.reconnect();
  }

  /**
   * Every board is based on WEP, therefore we use WEP over here. There is possibility to extend it in the future.
   */
  private WifiConfiguration createWifiConfiguration(String ssid, String password) {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.SSID = "\"" + ssid + "\""; //This should be in Quotes!!
    wifiConfiguration.wepKeys[0] = password; //This is the WEP Password
    wifiConfiguration.hiddenSSID = true;
    wifiConfiguration.status = WifiConfiguration.Status.DISABLED;
    wifiConfiguration.priority = 40;
    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
    wifiConfiguration.wepTxKeyIndex = 0;
    return wifiConfiguration;
  }

  /**
   * @return list of already seen WiFire networks
   */
  public List<String> getBoardWifiList() {
    startWifiScan();
    List<String> result = new ArrayList<>();
    for (ScanResult wifi : wifiManager.getScanResults()) {
      if (wifi.BSSID.toLowerCase().startsWith(Constants.BOARD_MAC_ADDRESS_PREFIX)) {
        result.add(wifi.SSID);
      }
    }
    return result;
  }

  /**
   * @return list of already seen WiFi networks
   */
  public List<String> getAvailableWifiList() {
    startWifiScan();
    List<String> result = new ArrayList<>();
    for (ScanResult wifi : wifiManager.getScanResults()) {
      result.add(wifi.SSID);
    }
    return result;
  }

  /**
   * Asynchronous scan start.
   */
  public void startWifiScan() {
    wifiManager.startScan();
  }

  /**
   * For checking WiFi connection. It is a matter if there is WiFi connection.
   */
  public boolean isWifiConnected() {
    NetworkInfo netInfo = getNetworkInfo();
    return netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
  }

  /**
   * For checking Internet connection. Doesn't matter if WiFi or if 3G.
   */
  public boolean isInternetConnected() {
    NetworkInfo netInfo = getNetworkInfo();
    return netInfo != null && netInfo.isConnected();
  }

  /**
   * For checking if connection is board's WiFi one.
   * May not work in case of normal network with ssid same as board's.
   */
  public boolean isBoardConnected() {
    if (isWifiConnected()) {
      List<String> boardWifiList = getBoardWifiList();
      for (String ssid : boardWifiList) {
        if (ssid.equals(getCurrentWifiSSID())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * For checking if current Internet connection is not board's WiFi one.
   */
  public boolean isInternetNotBoardConnected() {
    return isInternetConnected() && !isBoardConnected();
  }

  /**
   * For checking if current WiFi connection is not board's WiFi one.
   */
  public boolean isWifiNotBoardConnected() {
    return isWifiConnected() && !isBoardConnected();
  }

  /**
   * Enable chosen ssid's network.
   *
   * @param ssid chosen ssid
   * @return if success
   */
  public boolean enableChosenNetwork(String ssid) {
    for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
      if (ssid.equals(wifiConfiguration.SSID)) {
        int networkId = wifiManager.addNetwork(wifiConfiguration);
        return wifiManager.enableNetwork(networkId, true);
      }
    }
    return false;
  }

  public NetworkInfo getNetworkInfo() {
    ConnectivityManager conMan = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    return conMan.getActiveNetworkInfo();
  }

}
