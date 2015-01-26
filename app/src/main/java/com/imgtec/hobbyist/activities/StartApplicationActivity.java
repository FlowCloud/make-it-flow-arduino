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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.imgtec.flow.client.core.FlowException;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalRun;
import com.imgtec.hobbyist.utils.Preferences;
import com.imgtec.hobbyist.utils.WifiUtil;

/**
 * Activity without UI, that loads appropriate next Activity.
 * Activity choice depends on whether user is logged in to Flow and whether interactive mode was
 * already accessed previously.
 */
public class StartApplicationActivity extends FragmentActivity {

  private SharedPreferences sharedPreferences;
  private FlowHelper flowHelper;
  private Handler handler = new Handler();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sharedPreferences = getSharedPreferences(Preferences.SETTINGS, MODE_PRIVATE);
    ErrorHtmlLogger.setup(getApplicationContext());

    if (new WifiUtil(this).isInternetNotBoardConnected()) {
      init();
    } else {
      startAppropriateActivity(false);
    }
  }

  private void init() {
    flowHelper = FlowHelper.getInstance(this);
    flowInitialization();
  }

  private void flowInitialization() {
    BackgroundExecutor.execute(new ExternalRun() {
      @Override
      public void execute() {
        try {
          flowHelper.initFlowIfNotInitialized(StartApplicationActivity.this);
          if (FlowHelper.isFlowInitialized()) {
            startAppropriateActivity(FlowHelper.isUserLoggedInToFlow());
          }
        } catch (FlowException e) {
          DebugLogger.log(getClass().getSimpleName(), e);
          ActivitiesAndFragmentsHelper.showToast(getApplicationContext(),
              ErrorHtmlLogger.log(FlowEntities.getInstance(getApplicationContext()).getLastError()),
              handler);
          finish();
        }
      }

    });
  }

  private void startAppropriateActivity(final boolean userIsLoggedIn) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (userIsLoggedIn) {
          ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne(StartApplicationActivity.this,
              new Intent(StartApplicationActivity.this, FlowActivity.class));
        } else {
          ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne(StartApplicationActivity.this,
              new Intent(StartApplicationActivity.this,
                  sharedPreferences.getBoolean(Preferences.INTERACTIVE_MODE_HAS_STARTED_AT_LEAST_ONCE, false)
                      ? LogInToFlowActivity.class
                      : TourActivity.class
              )
          );
        }
      }
    });
  }
}
