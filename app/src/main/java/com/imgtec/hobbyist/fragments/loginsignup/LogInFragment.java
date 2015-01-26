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
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.imgtec.flow.client.core.FlowException;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.activities.LogInToFlowActivity;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.fragments.menu.ApplicationLogsFragment;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalCall;
import com.imgtec.hobbyist.utils.ExternalRun;
import com.imgtec.hobbyist.utils.OnTextChangedListener;
import com.imgtec.hobbyist.utils.Preferences;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;
import com.imgtec.hobbyist.utils.WifiUtil;

/**
 * Fragment representing a log in screen.
 * Flow login and subscription are performed from this screen.
 */
public class LogInFragment extends FragmentWithProgressBar implements BackgroundExecutor.Callbacks<Boolean> {

  public static final String TAG = "LogInFragment";
  private static final int LOGIN_TASK_ID = 1;

  private EditText emailAddressField;
  private EditText passwordField;
  private Button logIn;
  private TextView forgotPassword;
  private FlowHelper flowHelper;
  private Runnable loginTimeoutRunnable;
  private WifiUtil wifiUtil;
  private ConnectivityReceiver connectionReceiver;
  private boolean loginError = false;

  public static Fragment newInstance() {
    return new LogInFragment();
  }

  @Override
  protected void setActionBarTitleText() {
    actionBarTitle.setText(appContext.getString(R.string.log_in_title));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_log_in, container, false);
    logIn = (Button) rootView.findViewById(R.id.logIn);
    forgotPassword = (TextView) rootView.findViewById(R.id.forgotPassword);
    emailAddressField = (EditText) rootView.findViewById(R.id.emailAddress);
    passwordField = (EditText) rootView.findViewById(R.id.password);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setHasOptionsMenu(true);
    flowHelper = FlowHelper.getInstance(getActivity());
    setEmailAddressFieldText();
    initListeners();
  }

  @Override
  public void onResume() {
    super.onResume();
    connectionReceiver = new ConnectivityReceiver();
    wifiUtil = new WifiUtil(getActivity());
    getActivity().registerReceiver(connectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    reinitialization();
  }

  private void reinitialization() {
    flowReinitialization();
    logIn.setEnabled(isPasswordAndConnectionValid());
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

  private boolean isPasswordAndConnectionValid() {
    return wifiUtil.isWifiConnected()
        && passwordField.length() >= Constants.FLOW_ACCOUNT_MINIMUM_CHARACTERS_COUNT;
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

  private void setEmailAddressFieldText() {
    SharedPreferences sharedPreferences = appContext.getSharedPreferences(Preferences.SETTINGS, Context.MODE_PRIVATE);
    emailAddressField.setText(sharedPreferences.getString(Preferences.EMAIL_CREDENTIAL, ""));
  }

  private void initListeners() {
    new OnTextChangedListener(passwordField) {
      @Override
      public void onTextChanged(CharSequence s) {
        if (isPasswordAndConnectionValid()) {
          logIn.setEnabled(true);
        } else {
          logIn.setEnabled(false);
        }
      }
    };

    logIn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (validate()) {
          if (FlowHelper.isLicenseCorrect()) {
            showProgress(appContext.getString(R.string.logging_in));
            flowLogin();
          } else {
            ActivitiesAndFragmentsHelper.showToast(appContext, R.string.error_invalid_api_key, handler);
          }
        }
      }

      private boolean validate() {
        boolean result = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddressField.getText().toString()).matches()) {
          emailAddressField.setError(appContext.getString(R.string.email_address_is_required));
          result = false;
        } else {
          emailAddressField.setError(null);
        }
        final int passLength = passwordField.getText().toString().length();
        if (passLength < 5 || passLength > Constants.DEFAULT_MAXIMUM_FIELD_CHARACTERS_COUNT) {
          passwordField.setError(appContext.getString(R.string.incorrect_password_character_count));
          result = false;
        } else {
          passwordField.setError(null);
        }
        return result;
      }
    });

    forgotPassword.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FLOW_FORGOT_PASSWORD_URL)));
      }
    });
  }

  private void flowLogin() {
    loginTimeoutRunnable = new Runnable() {
      @Override
      public void run() {
        hideProgress();
        ActivitiesAndFragmentsHelper.showToast(appContext, R.string.error_network, handler);
        handler.removeCallbacks(this);
      }
    };
    handler.postDelayed(loginTimeoutRunnable, Constants.SIXTY_SECONDS_MILLIS / 2);

    BackgroundExecutor.submit(new ExternalCall<Boolean>() {
      @Override
      public Boolean submit() {
        try {
          if (flowHelper.userLoginToFlow(emailAddressField.getText().toString(), passwordField.getText().toString())) {
            return true;
          } else {
            ActivitiesAndFragmentsHelper.showToast(appContext,
                ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
                handler);
            return false;
          }
        } finally {
          hideProgress();
        }
      }
    }, this, LOGIN_TASK_ID, appContext);
  }

  @Override
  public void onPause() {
    getActivity().unregisterReceiver(connectionReceiver);
    super.onPause();
  }

  @Override
  public void onBackgroundExecutionResult(Boolean success, int taskCode) {
    handler.removeCallbacks(loginTimeoutRunnable);
    if (taskCode == LOGIN_TASK_ID) {
      if (success == null || !success) {
        setLogsAccessEnabled(true);
      } else {
        setLogsAccessEnabled(false);
        afterFlowLogin();
      }
    }
  }

  private void afterFlowLogin() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (emailAddressField != null) {
          Preferences.saveEmailCredential(appContext, emailAddressField.getText().toString());
          final Activity activity = getActivity();
          if (activity != null) {
            ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne(activity, new Intent(activity, FlowActivity.class));
          }
        }
      }
    });
  }

  private void setLogsAccessEnabled(boolean enabled) {
    loginError = enabled;
    final Activity activity = getActivity();
    if (activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          activity.invalidateOptionsMenu();
        }
      });
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_login, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem appLogsItem = menu.getItem(0);
    appLogsItem.setVisible(loginError);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.app_logs:
        final LogInToFlowActivity activity = (LogInToFlowActivity) getActivity();
        if (activity != null) {
          ApplicationLogsFragment fragment = (ApplicationLogsFragment) SimpleFragmentFactory.createFragment(ApplicationLogsFragment.TAG);
          ActivitiesAndFragmentsHelper.replaceFragment(activity, fragment);
          activity.setActionBarTitle(R.string.application_logs);
        }
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public class ConnectivityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      reinitialization();
    }
  }
}