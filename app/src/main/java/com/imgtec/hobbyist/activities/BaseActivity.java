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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;
import android.widget.TextView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.spice.requests.RobospiceRequestsHandler;
import com.imgtec.hobbyist.utils.Constants;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.XmlSpringAndroidSpiceService;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

/**
 * Main activity class.
 *
 * Has got access to:
 * - Robospice requests
 * - ActionBar title view
 *
 * Includes:
 * - HockeyApp setup
 * - fade in and fade out animations for start and finish the activity
 *
 */

public class BaseActivity extends ActionBarActivity {

  private String HOCKEY_APP_ID;

  protected TextView actionBarTitleView;
  protected RobospiceRequestsHandler requestsHandler = new RobospiceRequestsHandler(this, new SpiceManager(XmlSpringAndroidSpiceService.class));

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    HOCKEY_APP_ID = getString(R.string.hockey_id);
    initActionBar();
    checkForUpdates();
  }

  protected void initActionBar() {
    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.wdgt_action_bar);
    getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
    actionBarTitleView = (TextView) findViewById(R.id.actionBarTitle);
  }

  private void checkForUpdates() {
    if (Constants.HOCKEY_APP_ENABLED) {
      UpdateManager.register(this, HOCKEY_APP_ID);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    requestsHandler.startSpiceManager();
  }

  @Override
  protected void onResume() {
    super.onResume();
    checkForCrashes();
  }

  private void checkForCrashes() {
    if (Constants.HOCKEY_APP_ENABLED) {
      CrashManager.register(this, HOCKEY_APP_ID);
    }
  }

  @Override
  protected void onStop() {
    requestsHandler.stopSpiceManager();
    super.onStop();
  }

  @Override
  public void startActivity(Intent intent) {
    super.startActivity(intent);
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
  }

  public RobospiceRequestsHandler getRequestsHandler() {
    return requestsHandler;
  }

}
