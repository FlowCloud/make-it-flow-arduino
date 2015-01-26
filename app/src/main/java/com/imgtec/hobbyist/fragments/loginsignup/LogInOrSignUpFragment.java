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

package com.imgtec.hobbyist.fragments.loginsignup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.imgtec.flow.client.core.FlowException;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalRun;
import com.imgtec.hobbyist.utils.WifiUtil;

/**
 * Represents a screen that allows proceeding to {@link com.imgtec.hobbyist.fragments.loginsignup.LogInFragment}
 * or {@link com.imgtec.hobbyist.fragments.loginsignup.SignUpFragment}.
 * User can also enter {@link com.imgtec.hobbyist.fragments.loginsignup.SettingsFragment} from here.
 */
public class LogInOrSignUpFragment extends FragmentWithTitle {

  public static final String TAG = "LogInOrSignUpFragment";
  private OnFragmentInteractionListener onFragmentInteractionListener;
  private Button logIn;
  private Button signUp;
  private TextView noConnectionTv;
  private ConnectivityReceiver connectionReceiver;
  private FlowHelper flowHelper;
  private Handler handler = new Handler();
  private WifiUtil wifiUtil;

  public static Fragment newInstance() {
    return new LogInOrSignUpFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    return inflater.inflate(R.layout.frag_log_in_or_sign_up, container, false);
  }

  @Override
  protected void setActionBarTitleText() {
    actionBarTitle.setText(appContext.getString(R.string.welcome));
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    logIn = (Button) view.findViewById(R.id.logIn);
    signUp = (Button) view.findViewById(R.id.signUp);
    noConnectionTv = (TextView) view.findViewById(R.id.noConnectionTv);
    logIn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onFragmentInteractionListener != null) {
          onFragmentInteractionListener.logInButtonListener();
        }
      }
    });
    signUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onFragmentInteractionListener != null) {
          onFragmentInteractionListener.signUpButtonListener();
        }
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    connectionReceiver = new ConnectivityReceiver();
    wifiUtil = new WifiUtil(getActivity());
    getActivity().registerReceiver(connectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    reinitialization();
  }

  @Override
  public void onPause() {
    getActivity().unregisterReceiver(connectionReceiver);
    super.onPause();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    if (activity instanceof OnFragmentInteractionListener) {
      onFragmentInteractionListener = (OnFragmentInteractionListener) activity;
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    onFragmentInteractionListener = null;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_welcome, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.server_settings:
        onFragmentInteractionListener.serverSettingsListener();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void reinitialization() {
    flowReinitialization();
    updateUIAccordingToConnection();
  }

  private void flowReinitialization() {
    final Activity activity = getActivity();
    if (activity != null) {
      flowHelper = FlowHelper.getInstance(activity);
      if (wifiUtil.isInternetNotBoardConnected()) {
        flowInitialization(activity);
      }
    }
  }

  private void flowInitialization(final Activity activity) {
    BackgroundExecutor.execute(new ExternalRun() {
      @Override
      public void execute() {
        try {
          flowHelper.initFlowIfNotInitialized(activity);
        } catch (FlowException e) {
          DebugLogger.log(getClass().getSimpleName(), e);
          ActivitiesAndFragmentsHelper.showToast(appContext,
              ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
              handler);
          activity.finish();
        }
      }
    });
  }

  private void updateUIAccordingToConnection() {
    boolean isInternetNotBoardConnected = wifiUtil.isWifiConnected();
    logIn.setEnabled(isInternetNotBoardConnected);
    signUp.setEnabled(isInternetNotBoardConnected);
    noConnectionTv.setVisibility(isInternetNotBoardConnected ? View.GONE : View.VISIBLE);
  }

  public interface OnFragmentInteractionListener {
    void logInButtonListener();

    void signUpButtonListener();

    void serverSettingsListener();
  }

  public class ConnectivityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      reinitialization();
    }
  }
}
