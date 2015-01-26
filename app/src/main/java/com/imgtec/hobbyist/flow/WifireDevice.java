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

package com.imgtec.hobbyist.flow;

import com.imgtec.flow.client.users.Device;
import com.imgtec.flow.client.users.Devices;
import com.imgtec.hobbyist.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for Flow's {@link com.imgtec.flow.client.users.Device} class.
 */
public class WifireDevice implements Comparable<WifireDevice> {
  private Device device;
  private String networkSSID;
  private boolean networkState; //connected or not
  private String networkRSSIdBm;
  private String boardHealth;
  private String status;
  private String aor;

  private String uptime;

  public WifireDevice(Device device) {
    this.device = device;
    aor = device.getFlowMessagingAddress().getAddress();
  }

  public static List<WifireDevice> devicesAsWifireDeviceList(Devices devices) {
    List<WifireDevice> result = new ArrayList<>();
    for (Device device : devices) {
      if (Constants.DEVICE_TYPES.contains(device.getDeviceType())) {
        WifireDevice wifireDevice = new WifireDevice(device);
        insertDevice(wifireDevice, result);
      }
    }
    return result;
  }

  private static void insertDevice(WifireDevice device, List<WifireDevice> list) {
    if (!list.isEmpty()) {
      for (int i = 0; i < list.size(); ++i) {
        if (list.get(i).compareTo(device) < 0) {
          list.add(i, device);
          return;
        }
      }
    }
    list.add(device);
  }


  public Device getDevice() {
    return device;
  }

  @Override
  public String toString() {
    return device.getDeviceName();
  }

  public String getAor() {
    return aor;
  }

  public String getName() {
    return device.getDeviceName();
  }

  public void setName(String name) {
    device.setDeviceName(name);
  }

  public String getUptime() {
    return uptime;
  }

  public void setUptime(String uptime) {
    this.uptime = uptime;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getBoardHealth() {
    return boardHealth;
  }

  public void setBoardHealth(String boardHealth) {
    this.boardHealth = boardHealth;
  }

  public String getNetworkRSSIdBm() {
    return networkRSSIdBm;
  }

  public void setNetworkRSSIdBm(String networkRSSIdBm) {
    this.networkRSSIdBm = networkRSSIdBm;
  }

  public boolean isNetworkConnected() {
    return networkState;
  }

  public void setNetworkState(boolean networkState) {
    this.networkState = networkState;
  }

  public String getNetworkSSID() {
    return networkSSID;
  }

  public void setNetworkSSID(String networkSSID) {
    this.networkSSID = networkSSID;
  }

  @Override
  public int compareTo(WifireDevice another) {
    return this.getName().compareTo(another.getName());
  }

  @Override
  public boolean equals(Object rhs) {
    return rhs instanceof WifireDevice && this.compareTo((WifireDevice) rhs) == 0;
  }
}
