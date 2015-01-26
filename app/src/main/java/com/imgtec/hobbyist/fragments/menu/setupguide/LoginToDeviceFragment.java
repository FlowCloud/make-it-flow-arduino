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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.fragments.menu.help.HelpDeviceSetupFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.utils.BroadcastReceiverWithRegistrationState;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.OnTextChangedListener;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;
import com.imgtec.hobbyist.utils.WifiUtil;

/**
 * Fragment representing one of the screens of device setup.
 * Connecting with device's WiFi takes place here.
 */
public class LoginToDeviceFragment extends NDListeningFragment {

  public static final String TAG = "LoginToDeviceFragment";

  private EditText passwordField;
  private TextView chosenSSIDField;
  private Button connect;
  private Button helpLink;
  private ProgressBar progressBar;
  private String boardSSID;
  private Handler handler = new Handler();
  private Runnable showWrongPasswordRunnable;
  private WifiUtil wifiUtil;

  public static LoginToDeviceFragment newInstance() {
    return new LoginToDeviceFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_login_to_device, container, false);
    connect = (Button) rootView.findViewById(R.id.connect);
    helpLink = (Button) rootView.findViewById(R.id.helpLink);
    passwordField = (EditText) rootView.findViewById(R.id.passwordField);
    chosenSSIDField = (TextView) rootView.findViewById(R.id.chosenSSIDField);
    progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    boardSSID = SetupGuideInfoSingleton.getInstance().getBoardSsid();
    chosenSSIDField.setText(boardSSID);
    wifiUtil = new WifiUtil(appContext);
    initListeners();
  }

  private void initListeners() {
    new OnTextChangedListener(passwordField) {
      @Override
      public void onTextChanged(CharSequence s) {
        if (s.length() < Constants.WEP_64_BIT_SECRET_KEY_HEXADECIMAL_LENGTH) {
          connect.setEnabled(false);
        } else {
          connect.setEnabled(true);
        }
      }
    };

    connect.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (Constants.WIFIRE_BOARD_REQUESTS_MODE) {
          if (wifiUtil.getBoardWifiList().contains(boardSSID)) {
            startWiFiConnectionTimeout();
            startWiFiBoardConnection();
          } else {
            showAlertDialog(R.string.board_wifi_not_available);
          }
        } else {
          startFragmentAfterDeviceConnection();
        }
      }
    });

    helpLink.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        activity.onFragmentChange(SimpleFragmentFactory.createFragment(HelpDeviceSetupFragment.TAG));
      }
    });
  }

  private void showAlertDialog(int messageResId) {
    final AlertDialog.Builder builder = new AlertDialog.Builder((Activity)activity);
    builder
        .setMessage(appContext.getString(messageResId) + boardSSID)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            connect.setEnabled(false);
            progressBar.setVisibility(View.GONE);
            dialog.dismiss();
          }
        });
    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }

  private void startWiFiConnectionTimeout() {
    showWrongPasswordRunnable = new Runnable() {
      @Override
      public void run() {
        showAlertDialog(R.string.wrong_board_wifi_password);
        networkChangeReceiver.unregister(appContext);
      }
    };
    handler.postDelayed(showWrongPasswordRunnable, Constants.THIRTY_SECONDS_MILLIS);
  }

  private void startWiFiBoardConnection() {
    WifiUtil wifiUtil = new WifiUtil(appContext);
    wifiUtil.connectToWepNetwork(boardSSID, passwordField.getText().toString());
    ActivitiesAndFragmentsHelper.hideSoftInput(appContext, passwordField);
    progressBar.setVisibility(View.VISIBLE);
    networkChangeReceiver.register(appContext);
  }

  private BroadcastReceiverWithRegistrationState networkChangeReceiver =
      new BroadcastReceiverWithRegistrationState(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)) {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (wifiUtil.isBoardConnected()) {
            handler.removeCallbacks(showWrongPasswordRunnable);
            networkChangeReceiver.unregister(appContext);
            startFragmentAfterDeviceConnection();
          }
        }
      };

  private void startFragmentAfterDeviceConnection() {
    progressBar.setVisibility(View.GONE);
    if (activity != null) {
      activity.onFragmentChange(SimpleFragmentFactory.createFragment(LogInToWifiFragment.TAG));
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    activity.onTitleChange(appContext.getString(R.string.log_in_to_device));
  }

  @Override
  public void onPause() {
    networkChangeReceiver.unregister(appContext);
    handler.removeCallbacks(showWrongPasswordRunnable);
    super.onPause();
  }
}
