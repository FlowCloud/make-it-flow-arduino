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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.BaseActivity;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.fragments.menu.ConnectedDevicesFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.ExternalCall;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;
import com.imgtec.hobbyist.utils.WifiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment representing one of the screens of device setup.
 * Gets some required data about user's devices before allowing to continue the process.
 */
public class SetUpADeviceFragment extends NDListeningFragment implements BackgroundExecutor.Callbacks<Pair<String, List<Pair<String, String>>>> {

  public static final String TAG = "SetUpADeviceFragment";
  public static final String TEXT_TO_REPLACE = "YOUR_WIFI_NETWORK";

  private static final int GET_INFO_ID = 1;

  private FlowHelper flowHelper;
  private ProgressBar progressBar;
  private TextView setupInfo;
  private LinearLayout setupADeviceWifi;
  private RelativeLayout setupADeviceNoWifi;
  private Button startSetup;
  private Button tryAgain;
  private Button cancel;
  private Button onlineHelpLink;
  private Handler handler = new Handler();

  private String freeKey;
  private boolean isWiFiNotBoardConnected;

  public static SetUpADeviceFragment newInstance() {
    return new SetUpADeviceFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_setup_a_device, container, false);
    progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
    setupInfo = (TextView) rootView.findViewById(R.id.setupInfo);
    setupADeviceWifi = (LinearLayout) rootView.findViewById(R.id.setupADeviceWifi);
    setupADeviceNoWifi = (RelativeLayout) rootView.findViewById(R.id.setupADeviceNoWifi);
    startSetup = (Button) rootView.findViewById(R.id.startSetup);
    tryAgain = (Button) rootView.findViewById(R.id.tryAgain);
    cancel = (Button) rootView.findViewById(R.id.cancel);
    onlineHelpLink = (Button) rootView.findViewById(R.id.onlineHelpLink);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initButtonListeners();
  }

  private void initButtonListeners() {
    startSetup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        progressBar.setVisibility(View.VISIBLE);
        flowHelper = FlowHelper.getInstance(getActivity());
        BackgroundExecutor.submit(new ExternalCall<Pair<String, List<Pair<String, String>>>>() {
          @Override
          public Pair<String, List<Pair<String, String>>> submit() {
            List<Pair<String, String>> listOfKeyMACPairs = new ArrayList<>();
            freeKey = flowHelper.getFirstFreeRegistrationKey();
            listOfKeyMACPairs.addAll(flowHelper.getClaimedKeyMACPair());
            return new Pair<>(freeKey, listOfKeyMACPairs);
          }
        }, SetUpADeviceFragment.this, GET_INFO_ID, appContext);
      }
    });

    onlineHelpLink.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FLOW_ONLINE_HELP_URL)));
      }
    });

    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (activity != null) {
          activity.onFragmentChange(SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
        }
      }
    });

    tryAgain.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        determineWiFiConnection();
        if (isWiFiNotBoardConnected) {
          initUI();
        } else {
          Toast.makeText(appContext, appContext.getString(R.string.app_not_connected_to_wifi), Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    activity.onSelectionAndTitleChange(NDMenuItem.SetupDevice);
    determineWiFiConnection();
    initUI();
  }

  private void determineWiFiConnection() {
    WifiUtil wifiUtil = new WifiUtil(appContext);
    isWiFiNotBoardConnected = wifiUtil.isWifiNotBoardConnected();
  }

  public void initUI() {
    clearUI();
    initWifiConnected();
    initWifiNoConnected();
  }

  private void clearUI() {
    setupADeviceWifi.setVisibility(View.GONE);
    setupADeviceNoWifi.setVisibility(View.GONE);
  }

  private void initWifiConnected() {
    if (isWiFiNotBoardConnected) {
      setupADeviceWifi.setVisibility(View.VISIBLE);
      initSetupInfo();
    }
  }

  private void initSetupInfo() {
    String currentNetworkSSID = SetupGuideInfoSingleton.getInstance().getSsid();
    String setupInfoText = setupInfo.getText().toString();
    setupInfo.setText(setupInfoText.replace(TEXT_TO_REPLACE, currentNetworkSSID));
  }

  private void initWifiNoConnected() {
    if (!isWiFiNotBoardConnected) {
      setupADeviceNoWifi.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onBackgroundExecutionResult(Pair<String, List<Pair<String, String>>> results, int taskCode) {
    if (results != null && taskCode == GET_INFO_ID) {
      SetupGuideInfoSingleton.getInstance().setFreeRegistrationKey(results.first);
      SetupGuideInfoSingleton.getInstance().setListOfKeyMACPairs(results.second);
      handler.post(new Runnable() {
        @Override
        public void run() {
          final BaseActivity baseActivity = (BaseActivity) getActivity();
          if (baseActivity != null) {
            progressBar.setVisibility(View.GONE);
            if(!freeKey.equals("")) {
              ActivitiesAndFragmentsHelper.replaceFragment(baseActivity,
                  SimpleFragmentFactory.createFragment(SetupModeFragment.TAG));
            } else {
              baseActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  ActivitiesAndFragmentsHelper.showFragmentChangeDialog(
                      R.string.check_registration_keys_header,
                      R.string.check_registration_keys_button,
                      baseActivity,
                      ConnectedDevicesFragment.newInstance());
                }
              });
            }
          }
        }
      });
    }
  }
}
