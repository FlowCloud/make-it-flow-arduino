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

import android.support.v4.app.Fragment;

import com.imgtec.hobbyist.fragments.loginsignup.LogInFragment;
import com.imgtec.hobbyist.fragments.loginsignup.LogInOrSignUpFragment;
import com.imgtec.hobbyist.fragments.loginsignup.SettingsFragment;
import com.imgtec.hobbyist.fragments.loginsignup.SignUpFragment;
import com.imgtec.hobbyist.fragments.menu.AboutFragment;
import com.imgtec.hobbyist.fragments.menu.ActivityLogsFragment;
import com.imgtec.hobbyist.fragments.menu.ApplicationLogsFragment;
import com.imgtec.hobbyist.fragments.menu.ConnectedDevicesFragment;
import com.imgtec.hobbyist.fragments.menu.DeviceInfoFragment;
import com.imgtec.hobbyist.fragments.menu.FlowDeviceInfoFragment;
import com.imgtec.hobbyist.fragments.menu.InteractiveModeFragment;
import com.imgtec.hobbyist.fragments.menu.help.HelpDeviceSetupFragment;
import com.imgtec.hobbyist.fragments.menu.help.HelpFragment;
import com.imgtec.hobbyist.fragments.menu.help.HelpIntroductionFragment;
import com.imgtec.hobbyist.fragments.menu.help.HelpUsingTheAppFragment;
import com.imgtec.hobbyist.fragments.menu.help.HelpUsingTheBoardFragment;
import com.imgtec.hobbyist.fragments.menu.help.HelpWifiNetworkFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.ConnectingFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.LogInToWifiFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.LoginToDeviceFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.NetworkChoiceFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.SetUpADeviceFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.SetupModeFragment;

/**
 * Simple factory creating fragments.
 */
public class SimpleFragmentFactory {

  public static Fragment createFragment(final String tag) {
    Fragment fragment;
    switch (tag) {
      case SetUpADeviceFragment.TAG:
        fragment = SetUpADeviceFragment.newInstance();
        break;
      case SetupModeFragment.TAG:
        fragment = SetupModeFragment.newInstance();
        break;
      case LoginToDeviceFragment.TAG:
        fragment = LoginToDeviceFragment.newInstance();
        break;
      case LogInToWifiFragment.TAG:
        fragment = LogInToWifiFragment.newInstance();
        break;
      case ConnectingFragment.TAG:
        fragment = ConnectingFragment.newInstance();
        break;
/**----------------------------------------------------------------------------------*/
      case AboutFragment.TAG:
        fragment = AboutFragment.newInstance();
        break;
      case ActivityLogsFragment.TAG:
        fragment = ActivityLogsFragment.newInstance();
        break;
      case ApplicationLogsFragment.TAG:
        fragment = ApplicationLogsFragment.newInstance();
        break;
      case DeviceInfoFragment.TAG:
        fragment = DeviceInfoFragment.newInstance();
        break;
      case FlowDeviceInfoFragment.TAG:
        fragment = FlowDeviceInfoFragment.newInstance();
        break;
      case HelpFragment.TAG:
        fragment = HelpFragment.newInstance();
        break;
      case HelpIntroductionFragment.TAG:
        fragment = HelpIntroductionFragment.newInstance();
        break;
      case HelpWifiNetworkFragment.TAG:
        fragment = HelpWifiNetworkFragment.newInstance();
        break;
      case HelpDeviceSetupFragment.TAG:
        fragment = HelpDeviceSetupFragment.newInstance();
        break;
      case HelpUsingTheAppFragment.TAG:
        fragment = HelpUsingTheAppFragment.newInstance();
        break;
      case HelpUsingTheBoardFragment.TAG:
        fragment = HelpUsingTheBoardFragment.newInstance();
        break;
      case InteractiveModeFragment.TAG:
        fragment = InteractiveModeFragment.newInstance();
        break;
      case LogInFragment.TAG:
        fragment = LogInFragment.newInstance();
        break;
      case LogInOrSignUpFragment.TAG:
        fragment = LogInOrSignUpFragment.newInstance();
        break;
      case ConnectedDevicesFragment.TAG:
        fragment = ConnectedDevicesFragment.newInstance();
        break;
      case SignUpFragment.TAG:
        fragment = SignUpFragment.newInstance();
        break;
      case SettingsFragment.TAG:
        fragment = SettingsFragment.newInstance();
        break;
      default:
        DebugLogger.log(SimpleFragmentFactory.class.getName(), "Wrong fragment instantiated");
        fragment = ConnectedDevicesFragment.newInstance();
    }
    return fragment;
  }

  public static Fragment createFragment(final String tag, boolean isSomething) {
    Fragment fragment;
    switch (tag) {
      case NetworkChoiceFragment.TAG:
        fragment = NetworkChoiceFragment.newInstance(isSomething);
        break;
      case ConnectedDevicesFragment.TAG:
        fragment = ConnectedDevicesFragment.newInstance(isSomething);
        break;
      default:
        DebugLogger.log(SimpleFragmentFactory.class.getName(), "Wrong fragment instantiated");
        fragment = ConnectedDevicesFragment.newInstance();
    }
    return fragment;
  }

}
