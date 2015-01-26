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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.imgtec.flow.client.core.FlowException;
import com.imgtec.hobbyist.BuildConfig;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.StartApplicationActivity;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalCall;
import com.imgtec.hobbyist.utils.Preferences;

/**
 * Fragment which allows changing Flow initialization values (license details, Flow server URL and WiFire board URL).
 */
public class SettingsFragment extends FragmentWithProgressBar implements BackgroundExecutor.Callbacks<Boolean> {

  public static final String TAG = "ServerSettingsFragment";
  private static final int CHECK_LICENSEE_ID = 1;

  private EditText rootEdit;
  private EditText keyEdit;
  private EditText secretEdit;
  private EditText wifireEdit;
  private TextView buildNumber;
  private Button saveButton;
  private Context appContext;
  private Handler handler = new Handler();
  private FlowHelper flowHelper;

  public static Fragment newInstance() {
    return new SettingsFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    flowHelper = FlowHelper.getInstance(getActivity());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_settings, container, false);

    rootEdit = (EditText) view.findViewById(R.id.rootEdit);
    rootEdit.setOnFocusChangeListener(new Trimmer());
    keyEdit = (EditText) view.findViewById(R.id.keyEdit);
    keyEdit.setOnFocusChangeListener(new Trimmer());
    secretEdit = (EditText) view.findViewById(R.id.secretEdit);
    secretEdit.setOnFocusChangeListener(new Trimmer());
    wifireEdit = (EditText) view.findViewById(R.id.wifireEdit);
    wifireEdit.setOnFocusChangeListener(new Trimmer());
    buildNumber = (TextView) view.findViewById(R.id.buildNumber);
    saveButton = (Button) view.findViewById(R.id.save);

    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    buildNumber.setText(BuildConfig.VERSION_NAME);

    SharedPreferences sharedPreferences = appContext.getSharedPreferences(Preferences.SETTINGS, Context.MODE_PRIVATE);
    rootEdit.setText(sharedPreferences.getString(Preferences.ROOT_URL, Preferences.ROOT_URL_DEFAULT_VALUE));
    keyEdit.setText(sharedPreferences.getString(Preferences.OAUTH_KEY, Preferences.OAUTH_KEY_DEFAULT_VALUE));
    secretEdit.setText(sharedPreferences.getString(Preferences.OAUTH_SECRET, Preferences.OAUTH_SECRET_DEFAULT_VALUE));
    wifireEdit.setText(sharedPreferences.getString(Preferences.WIFIRE_URL,
        Constants.WIFIRE_BOARD_REQUESTS_MODE
            ? Preferences.WIFIRE_URL_BOARD_WEBSERVICE_URL
            : Preferences.WIFIRE_URL_APIARY_WEBSERVICE_URL
    ));

    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (validate()) {
          saveSettingsAndReinitializeApp();
        }
      }
    });

    buildNumber.setOnClickListener(new View.OnClickListener() {

      int tapCounter = 0;

      @Override
      public void onClick(View v) {
        ++tapCounter;
        if (tapCounter > 4) {
          rootEdit.setVisibility(View.VISIBLE);
          wifireEdit.setVisibility(View.VISIBLE);
        }
      }
    });
  }

  private void saveSettingsAndReinitializeApp() {
    Preferences.saveSettings(appContext, rootEdit.getText().toString(), keyEdit.getText().toString(),
        secretEdit.getText().toString(), wifireEdit.getText().toString());

    showProgress(appContext.getString(R.string.reinitialization));

    BackgroundExecutor.submit(new ExternalCall<Boolean>() {
      @Override
      public Boolean submit() {
        flowHelper.shutdown();
        flowHelper.initFlowIfNotInitialized(getActivity());
        flowHelper.readUrlsForLicensee();
        return FlowHelper.isLicenseCorrect();
      }
    }, this, CHECK_LICENSEE_ID, appContext);
  }

  private boolean validate() {
    if (!singleValidation(rootEdit, Constants.DEFAULT_MAXIMUM_FLOW_REST_URL_CHARACTERS_COUNT)) {
      return false;
    }
    if (!keyValidation(keyEdit, Constants.DEFAULT_KEY_CHARACTERS_COUNT)) {
      return false;
    }
    if (!keyValidation(secretEdit, Constants.DEFAULT_KEY_CHARACTERS_COUNT)) {
      return false;
    }
    if (!singleValidation(wifireEdit, Constants.DEFAULT_MAXIMUM_FIELD_CHARACTERS_COUNT)) {
      return false;
    }

    return true;
  }

  private boolean singleValidation(EditText editText, int charCount) {
    if (editText.getText().toString().length() > charCount) {
      editText.setError(appContext.getString(R.string.incorrect_field_character_count));
      return false;
    } else {
      editText.setError(null);
      return true;
    }
  }

  private boolean keyValidation(EditText editText, int charCount) {
    if (editText.getText().toString().length() != charCount) {
      editText.setError(appContext.getString(R.string.incorrect_field_character_count));
      return false;
    } else {
      editText.setError(null);
      return true;
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    appContext = activity.getApplicationContext();
  }

  @Override
  protected void setActionBarTitleText() {
    actionBarTitle.setText(appContext.getString(R.string.settings));
  }

  @Override
  public void onBackgroundExecutionResult(Boolean isLicenceCorrect, int taskCode) {
    if (isLicenceCorrect != null && isLicenceCorrect && taskCode == CHECK_LICENSEE_ID) {
      try {
        hideProgress();
        ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne(getActivity(), new Intent(getActivity(), StartApplicationActivity.class));
      } catch (FlowException e) {
        DebugLogger.log(getClass().getSimpleName(), e);
        ActivitiesAndFragmentsHelper.showToast(appContext,
            ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
            handler);
        getActivity().finish();
      }
    } else {
      hideProgress();
      ActivitiesAndFragmentsHelper.showToast(appContext, R.string.error_invalid_api_key, handler);
    }
  }

  private class Trimmer implements View.OnFocusChangeListener {
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
      if (!hasFocus) {
        String newText = ((EditText) v).getText().toString().replace(" ", "");
        ((EditText) v).setText(newText);
      }
    }
  }
}
