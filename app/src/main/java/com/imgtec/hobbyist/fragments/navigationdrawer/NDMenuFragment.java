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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.imgtec.flow.client.core.FlowException;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.LogInToFlowActivity;
import com.imgtec.hobbyist.adapters.NDMenuAdapter;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.fragments.menu.AboutFragment;
import com.imgtec.hobbyist.fragments.menu.ActivityLogsFragment;
import com.imgtec.hobbyist.fragments.menu.ApplicationLogsFragment;
import com.imgtec.hobbyist.fragments.menu.ConnectedDevicesFragment;
import com.imgtec.hobbyist.fragments.menu.DeviceInfoFragment;
import com.imgtec.hobbyist.fragments.menu.FlowDeviceInfoFragment;
import com.imgtec.hobbyist.fragments.menu.InteractiveModeFragment;
import com.imgtec.hobbyist.fragments.menu.help.HelpFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.LogInToWifiFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.SetUpADeviceFragment;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalRun;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

/**
 * NavigationDrawer with 3 possible states. Adapter for list of them is created in
 * {@link #restartNavigationDrawer(NDMenuMode menu)}.
 * States are of type {@link com.imgtec.hobbyist.utils.NDMenuMode}.
 *
 * There is also item selection, as well as appropriate fragment selection provided.
 */
public class NDMenuFragment extends NDListeningFragment {

  public static final String TAG = "NDMenuFragment";

  private DrawerLayout drawerLayout;
  private View fragmentContainerView;
  private ListView drawerListView;
  private ProgressDialog loadingProgressDialog;
  private NDMenuAdapter ndMenuAdapter;
  private Handler handler = new Handler();
  private FlowHelper flowHelper;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.frag_navigation_drawer, container, false);
    drawerListView = (ListView) rootView.findViewById(R.id.drawerListView);
    drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectFragment((NDMenuItem) ndMenuAdapter.getItem(position));
      }
    });
    restartNavigationDrawer(NDMenuMode.Initial);
    return rootView;
  }

  public void restartNavigationDrawer(NDMenuMode mode) {
    NDMenuMode.setMode(mode);
    switch (mode) {
      case Initial:
        ndMenuAdapter = new NDMenuAdapter((Activity)activity, NDMenuItem.initialValues());
        break;
      case Setup:
        ndMenuAdapter = new NDMenuAdapter((Activity)activity, NDMenuItem.wifiNetworkModeValues());
        break;
      case Interactive:
        ndMenuAdapter = new NDMenuAdapter((Activity)activity, NDMenuItem.interactiveModeValues());
        break;
      default:
        ndMenuAdapter = new NDMenuAdapter((Activity)activity, NDMenuItem.initialValues());
    }

    drawerListView.setAdapter(ndMenuAdapter);
  }

  /**
   * Users of this fragment must call this method to set up the navigation drawer interactions.
   *
   * @param fragmentId   The android:id of this fragment in its activity's layout.
   * @param drawerLayout The DrawerLayout containing this fragment's UI.
   */
  public void setUp(int fragmentId, DrawerLayout drawerLayout) {
    fragmentContainerView = getActivity().findViewById(fragmentId);
    this.drawerLayout = drawerLayout;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    flowHelper = FlowHelper.getInstance((Activity) activity);
    setHasOptionsMenu(true); // Indicate that this fragment would like to influence the set of actions in the action bar.
    selectInitializationFragment(NDMenuItem.getCheckedItem());
  }

  private void selectInitializationFragment(NDMenuItem menuItem) {
    closeDrawer();
    if (activity != null) {
      showInitializationFragment(menuItem);
    }
  }

  private void showInitializationFragment(NDMenuItem menuItem) {
    String tag = getSelectedItemTag(menuItem);
    Fragment fragment = getFragmentManager().findFragmentByTag(tag);
    if (fragment == null) {
      fragment = SimpleFragmentFactory.createFragment(tag);
      activity.onFragmentChangeWithBackstackClear(fragment);
    }
    activity.onSelectionAndTitleChange(menuItem);
  }

  private void selectFragment(NDMenuItem menuItem) {
    closeDrawer();
    if (activity != null) {
      if (!menuItem.equals(NDMenuItem.SignOut)) {
        showFragment(menuItem);
      } else {
        logout();
      }
    }
  }

  private void showFragment(NDMenuItem menuItem) {
    String tag = getSelectedItemTag(menuItem);
    if (menuItem.equals(NDMenuItem.ActivityLogs) || menuItem.equals(NDMenuItem.MyDevice)
        || menuItem.equals(NDMenuItem.ApplicationLogs)) {
      activity.onFragmentChange(SimpleFragmentFactory.createFragment(tag));
    } else {
      activity.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(tag));
    }
    activity.onSelectionAndTitleChange(menuItem);
  }

  private String getSelectedItemTag(NDMenuItem menuItem) {
    String tag = "";
    switch (menuItem) {
      case MyDevice:
        if (NDMenuMode.getMode() == NDMenuMode.Setup) {
          tag = DeviceInfoFragment.TAG;
        } else if (NDMenuMode.getMode() == NDMenuMode.Interactive) {
          tag = FlowDeviceInfoFragment.TAG;
        }
        break;
      case InteractiveMode:
        tag = InteractiveModeFragment.TAG;
        break;
      case ConfigureWifi:
        tag = LogInToWifiFragment.TAG;
        break;
      case ActivityLogs:
        tag = ActivityLogsFragment.TAG;
        break;
      case ApplicationLogs:
        tag = ApplicationLogsFragment.TAG;
        break;
      case ConnectedDevices:
        tag = ConnectedDevicesFragment.TAG;
        break;
      case SetupDevice:
        tag = SetUpADeviceFragment.TAG;
        break;
      case Help:
        tag = HelpFragment.TAG;
        break;
      case About:
        tag = AboutFragment.TAG;
        break;
      case SignOut:
        break;
      default:
        tag = ConnectedDevicesFragment.TAG;
    }
    return tag;
  }

  private void logout() {
    loadingProgressDialog = ProgressDialog.show(getActivity(), appContext.getString(R.string.please_wait_with_dots), appContext.getString(R.string.logging_out), true);
    logoutUser();
  }

  private void logoutUser() {
    NDMenuMode.setMode(NDMenuMode.Initial);
    BackgroundExecutor.execute(new ExternalRun() {
      @Override
      public void execute() {
        try {
          flowHelper.userLogOut();
        } catch (FlowException e) {
          DebugLogger.log(getClass().getSimpleName(), e);
          ActivitiesAndFragmentsHelper.showToast(appContext,
              ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
              handler);
        }
        afterLogoutUser();
      }
    });
  }

  private void afterLogoutUser() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        loadingProgressDialog.dismiss();
        ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne((Activity)activity, new Intent(getActivity(), LogInToFlowActivity.class));
        setSelectionState(NDMenuItem.ConnectedDevices); //set drawer menu to have default selection when this activity is resumed
      }
    });
  }


  public void setSelectionState(NDMenuItem menuItem) {
    NDMenuItem.setCheckedItem(menuItem);
    if (ndMenuAdapter != null) {
      ndMenuAdapter.notifyDataSetChanged();
    }
  }

  private void closeDrawer() {
    if (drawerLayout != null) {
      drawerLayout.closeDrawer(fragmentContainerView);
    }
  }

  @Override
  public void onPause() {
    if (loadingProgressDialog != null) {
      loadingProgressDialog.dismiss();
    }
    super.onPause();
  }

}
