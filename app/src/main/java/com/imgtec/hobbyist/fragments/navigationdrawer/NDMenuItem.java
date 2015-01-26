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

package com.imgtec.hobbyist.fragments.navigationdrawer;

import com.imgtec.hobbyist.R;

/**
 * Enumeration which encapsulates menu items for NavigationDrawer.
 * Order is important - it's order of menu items in UI.
 */
public enum NDMenuItem {

  UserName(),
  MyDevice(),
  InteractiveMode(R.string.interactive_mode),
  ConfigureWifi(R.string.configure_wifi),
  ActivityLogs(R.string.activity_logs),
  Separator(),
  ConnectedDevices(R.string.connected_devices),
  SetupDevice(R.string.setup_device),
  ApplicationLogs(R.string.application_logs),
  Help(R.string.help),
  About(R.string.about),
  SignOut(R.string.log_out);

  private final int textId;
  private static NDMenuItem checkedItem = NDMenuItem.ConnectedDevices;

  NDMenuItem(int textId) {
    this.textId = textId;
  }

  NDMenuItem() {
    textId = -1;
  }

  public int getTextId() {
    return textId;
  }

  public boolean isMyDevice() {
    return (this == MyDevice);
  }

  public boolean isUserName() {
    return (this == UserName);
  }

  public boolean isSeparator() {
    return (this == Separator);
  }

  public static NDMenuItem getCheckedItem() {
    return NDMenuItem.checkedItem;
  }

  public static void setCheckedItem(NDMenuItem checkedItem) {
    NDMenuItem.checkedItem = checkedItem;
  }

  public static boolean isChecked(NDMenuItem menuItem) {
    return menuItem == checkedItem;
  }

  public static NDMenuItem[] initialValues() {
    return new NDMenuItem[]{UserName, ConnectedDevices, SetupDevice, ApplicationLogs,
        Help, About, SignOut};
  }

  public static NDMenuItem[] wifiNetworkModeValues() {
    return new NDMenuItem[]{UserName, MyDevice, ConfigureWifi, ActivityLogs, Separator,
        ConnectedDevices, SetupDevice, ApplicationLogs, Help, About, SignOut};
  }

  public static NDMenuItem[] interactiveModeValues() {
    return new NDMenuItem[]{UserName, MyDevice, InteractiveMode, Separator,
        ConnectedDevices, SetupDevice, ApplicationLogs, Help, About, SignOut};
  }

}
