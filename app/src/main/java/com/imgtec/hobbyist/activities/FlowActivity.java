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

package com.imgtec.hobbyist.activities;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.flow.DevicePresenceListener;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.fragments.menu.setupguide.BoardConnectedChoiceDialogFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuListener;
import com.imgtec.hobbyist.spice.listeners.RequestListenerForDeviceName;
import com.imgtec.hobbyist.spice.pojos.DeviceName;
import com.imgtec.hobbyist.spice.requests.gets.GetDeviceNameRequest;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;
import com.imgtec.hobbyist.utils.WifiUtil;
import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Activity seen when we are logged in to Flow.
 * --------------------------------|
 * Navigation|       Content       |
 * Drawer    |                     |
 *           |                     |
 *           |                     |
 *           |                     |
 *           |                     |
 *           |                     |
 *           |                     |
 *           |                     |
 *           |                     |
 *           |                     |
 *           |                     |
 *           |                     |
 *           |                     |
 * _________________________________
 *
 */

public class FlowActivity extends BaseActivity implements NDMenuListener, DevicePresenceListener {

  private NDMenuFragment leftNDMenuFragment;
  private DrawerLayout drawerLayout;
  /**
   * drawerToggle is an object on the ActionBar, which is used to change Navigation Drawer visibility.
   */
  private ActionBarDrawerToggle drawerToggle;
  private CharSequence actionBarTitle;
  private WifiUtil wifiUtil;
  private Handler handler = new Handler();
  private FlowHelper flowHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    wifiUtil = new WifiUtil(this);
    flowHelper = FlowHelper.getInstanceAndRestartAppIfRequired(this);
    initUI();
  }

  private void initUI() {
    setContentView(R.layout.actv_flow);
    initActionBar();
    leftNDMenuFragment = (NDMenuFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerLayout.setScrimColor(Color.TRANSPARENT);
    leftNDMenuFragment.setUp(R.id.navigation_drawer, drawerLayout);
    drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
      public void onDrawerClosed(View view) {
        actionBarTitleView.setText(actionBarTitle);
      }

      public void onDrawerOpened(View drawerView) {
        actionBarTitleView.setText(actionBarTitle);
      }
    };
    drawerLayout.setDrawerListener(drawerToggle);
  }

  @Override
  protected void initActionBar() {
    super.initActionBar();
    getSupportActionBar().setHomeButtonEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setIcon(null);
    getSupportActionBar().setDisplayUseLogoEnabled(true);
    if (actionBarTitle == null) {
      actionBarTitle = getApplicationContext().getString(R.string.connected_devices);
    }
    actionBarTitleView.setText(actionBarTitle);
  }

  @Override
  protected void onResume() {
    super.onResume();
    flowHelper.addDevicePresenceListener(this);
    if (wifiUtil.isBoardConnected()) {
      setSupportProgressBarIndeterminateVisibility(true);
      performGetDeviceNameRequest();
    } else if (wifiUtil.isInternetConnected()) {
      saveSSID();
    }
  }

  @Override
  public void onPause() {
    flowHelper.removeDevicePresenceListener(this);
    super.onPause();
  }

  private void performGetDeviceNameRequest() {
    requestsHandler.performRequest(new RequestListenerForDeviceName() {
      @Override
      public void onRequestFailure(SpiceException e) {
        setSupportProgressBarIndeterminateVisibility(false);
        ActivitiesAndFragmentsHelper.showToast(getApplicationContext(), R.string.check_connectivity, handler);
      }

      @Override
      public void onRequestSuccess(DeviceName deviceName) {
        setSupportProgressBarIndeterminateVisibility(false);
        SetupGuideInfoSingleton.getInstance().setDeviceName(deviceName.getName());
        setUIMode(NDMenuMode.Setup);
        showDialog();
      }
    }, new GetDeviceNameRequest(getApplicationContext()));
  }

  private void showDialog() {
    BoardConnectedChoiceDialogFragment.newInstance().show(getSupportFragmentManager(), BoardConnectedChoiceDialogFragment.TAG);
  }

  private void saveSSID() {
    String currentWifiSSID = wifiUtil.getCurrentWifiSSID();
    if (currentWifiSSID == null) {
      ActivitiesAndFragmentsHelper.showToast(this, R.string.no_connection, new Handler());
      ActivitiesAndFragmentsHelper.restartApplication(this, new Handler());
    } else {
      SetupGuideInfoSingleton.getInstance().setSsid(currentWifiSSID);
    }
  }

  /**
   * -------------------------------drawerToggle functions start-----------------------------------
   */
  @Override
  public boolean onOptionsItemSelected(android.view.MenuItem item) {
    // The action bar home/up action should open or close the drawer.
    // ActionBarDrawerToggle will take care of this.
    if (drawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    drawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    drawerToggle.onConfigurationChanged(newConfig);
  }
  /**-------------------------------drawerToggle functions end------------------------------------*/

  /**
   * Change ActionBar title for selected NDMenuItem
   *
   * @param menuItem chosen NavigationDrawer item
   */
  @Override
  public void onSelectionAndTitleChange(NDMenuItem menuItem) {
    leftNDMenuFragment.setSelectionState(menuItem);
    if (menuItem.getTextId() > 0) { //if text for menuItem exists
      onTitleChange(getApplicationContext().getString(menuItem.getTextId()));
    }
  }

  @Override
  public void onTitleChange(String title) {
    actionBarTitle = title;
    actionBarTitleView.setText(actionBarTitle);
  }

  /**
   * Replace shown fragment
   *
   * @param fragment - new fragment
   */
  @Override
  public void onFragmentChange(Fragment fragment) {
    ActivitiesAndFragmentsHelper.replaceFragment(this, fragment);
  }

  /**
   * Replace shown fragment with backstack clear
   *
   * @param fragment - new fragment
   */
  @Override
  public void onFragmentChangeWithBackstackClear(Fragment fragment) {
    ActivitiesAndFragmentsHelper.replaceFragmentWithBackStackClear(this, fragment);
  }

  /**
   * There are 3 options of NavigationDrawer MenuItems:
   * {@link com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem#initialValues()} ()}
   * {@link com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem#wifiNetworkModeValues()} ()}
   * {@link com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem#interactiveModeValues()} ()}
   * and this states change drawer only after calling this function:
   */
  @Override
  public void onMenuChange(NDMenuMode mode) {
    leftNDMenuFragment.restartNavigationDrawer(mode);
  }

  /**
   * Should be used carefully, because it change the NDMenuFragment menu items!
   */
  public void setUIMode(NDMenuMode mode) {
    onMenuChange(mode);
  }

  public void setInteractiveToInitialMode() {
    if (NDMenuMode.getMode() == NDMenuMode.Interactive) {
      setUIMode(NDMenuMode.Initial);
    }
  }


  @Override
  public void onDevicePresenceChangeListener(boolean isConnected) {
    if(!isConnected) {
      setInteractiveToInitialMode();
    }
  }
}