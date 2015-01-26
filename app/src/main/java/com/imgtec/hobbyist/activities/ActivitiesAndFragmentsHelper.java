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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.imgtec.hobbyist.R;

/**
 * Class defining operations which often happen in Activities and Fragments throughout the app.
 */
public class ActivitiesAndFragmentsHelper {

  static AlertDialog.Builder dialogBuilder;
  /**
   * Start new activity and finish previous one.
   *
   * @param activity is current activity to finish
   * @param intent   of new activity to start
   */
  public static void startActivityAndFinishPreviousOne(Activity activity, Intent intent) {
    activity.startActivity(intent);
    activity.finish();
  }

  /**
   * Replaces current fragment with fragment from second parameter.
   *
   * @param activity current activity
   * @param fragment we want to display
   */
  public static void replaceFragment(BaseActivity activity, Fragment fragment) {
    FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.content, fragment);
    fragmentTransaction.addToBackStack(null);
    fragmentTransaction.commitAllowingStateLoss();
    activity.getSupportFragmentManager().executePendingTransactions();
  }

  /**
   * Replaces current fragment with fragment from second parameter with fragment's backstack clear.
   *
   * @param activity current activity
   * @param fragment we want to display
   */
  public static void replaceFragmentWithBackStackClear(BaseActivity activity, Fragment fragment) {
    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.replace(R.id.content, fragment, fragment.getClass().getSimpleName());
    clearBackStack(fragmentManager);
    fragmentTransaction.commitAllowingStateLoss();
  }

  /**
   * Used to clear the fragments back stack. Particularly helpful when we click another NDMenuItem
   */
  private static void clearBackStack(FragmentManager fragmentManager) {
    for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
      fragmentManager.popBackStack();
    }
  }

  /**
   * Function use through the app to show Toast information. Function is called so often from
   * background, that it's content is explicitly called from UIThread.
   *
   * @param appContext       - application context
   * @param stringResourceId of text we show
   * @param handler          to UIThread
   */
  public static void showToast(final Context appContext, final int stringResourceId, Handler handler) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(appContext, appContext.getString(stringResourceId), Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Function use through the app to show Toast information and finish current activity.
   * Function is called so often from background, that it's content is explicitly called from UIThread.
   *
   * @param activity         - is current activity to finish
   * @param stringResourceId of text we show
   * @param handler          to UIThread
   */
  public static void showToastAndFinishActivity(final Activity activity, final int stringResourceId, Handler handler) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(activity, activity.getString(stringResourceId), Toast.LENGTH_SHORT).show();
        activity.finish();
      }
    });
  }

  public static void restartApplication(Activity activity, Handler handler) {
    clearBackStack(((FragmentActivity)activity).getSupportFragmentManager());
    ActivitiesAndFragmentsHelper.showToast(activity.getApplicationContext(), R.string.application_restart, handler);
    ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne(activity, new Intent(activity.getApplicationContext(), StartApplicationActivity.class));
  }

  /**
   * To hide system's keyboard.
   */
  public static void hideSoftInput(Context appContext, View... views) {
    InputMethodManager inputManager = (InputMethodManager) appContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    for (View view : views) {
      inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  /**
   * Shows dialog window.
   * After dismissing, current fragment will be replaced with specified fragment,
   * and state of the navigation drawer will be reset to Initial.
   * Backstack is not preserved.
   *
   * @param titleTextID
   * @param messageTextID
   * @param activity
   * @param fragment - Fragment to appear after dismiss
   */
  public static void showFragmentChangeDialog(int titleTextID, int messageTextID, final Activity activity, final Fragment fragment) {
    if (activity != null) {
      if (dialogBuilder == null) {
        dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setPositiveButton(messageTextID, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            ((FlowActivity) activity).setInteractiveToInitialMode();
            ((FlowActivity) activity).onFragmentChangeWithBackstackClear(fragment);
            dialogBuilder = null;
          }
        }).setTitle(titleTextID).setCancelable(false).create().show();
      }
    }
  }

  public static void showDeviceInfoChangeDialog(int titleTextID, int messageTextID, final Activity activity) {
    if (activity != null) {
      if (dialogBuilder == null) {
        dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setPositiveButton(messageTextID, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialogBuilder = null;
          }
        }).setTitle(titleTextID).setCancelable(false).create().show();
      }
    }
  }

}
