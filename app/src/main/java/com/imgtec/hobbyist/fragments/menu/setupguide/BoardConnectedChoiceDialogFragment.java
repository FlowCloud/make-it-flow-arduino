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

package com.imgtec.hobbyist.fragments.menu.setupguide;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.fragments.menu.ActivityLogsFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

/**
 * Dialog that redirects to {@link com.imgtec.hobbyist.fragments.menu.setupguide.LogInToWifiFragment}
 * or {@link com.imgtec.hobbyist.fragments.menu.ActivityLogsFragment}
 */
public class BoardConnectedChoiceDialogFragment extends DialogFragment {

  public static final String TAG = "BoardConnectedChoiceDialogFragment";

  public static BoardConnectedChoiceDialogFragment newInstance() {
    return new BoardConnectedChoiceDialogFragment();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Use the Builder class for convenient dialog construction
    final Activity activity = getActivity();
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    Context appContext = activity.getApplicationContext();
    builder.setTitle(appContext.getString(R.string.you_are_connected_to_your_device));
    builder.setMessage(appContext.getString(R.string.board_connected_choice_message))
        .setPositiveButton(R.string.connect_button_text, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            ((FlowActivity) activity).onFragmentChange(SimpleFragmentFactory.createFragment(LogInToWifiFragment.TAG));
            ((FlowActivity) activity).onSelectionAndTitleChange(NDMenuItem.SetupDevice);
          }
        })
        .setNegativeButton(R.string.activity_logs, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            ((FlowActivity) activity).onFragmentChange(SimpleFragmentFactory.createFragment(ActivityLogsFragment.TAG));
            ((FlowActivity) activity).onSelectionAndTitleChange(NDMenuItem.ActivityLogs);
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }
}
