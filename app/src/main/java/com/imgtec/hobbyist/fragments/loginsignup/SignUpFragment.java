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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.imgtec.flow.client.core.BadRequestException;
import com.imgtec.flow.client.core.ConflictException;
import com.imgtec.flow.client.core.FlowException;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalRun;
import com.imgtec.hobbyist.utils.OnTextChangedListener;
import com.imgtec.hobbyist.utils.Preferences;

/**
 * Fragment representing a sign up screen.
 */
public class SignUpFragment extends FragmentWithProgressBar {

  public static final String TAG = "SignUpFragment";
  private Button signUpButton;
  private EditText usernameField;
  private EditText emailAddressField;
  private EditText passwordField;
  private FlowHelper flowHelper;
  private Handler handler = new Handler();

  public static Fragment newInstance() {
    return new SignUpFragment();
  }

  @Override
  protected void setActionBarTitleText() {
    actionBarTitle.setText(appContext.getString(R.string.sign_up_title));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_sign_up, container, false);
    signUpButton = (Button) rootView.findViewById(R.id.signUp);
    usernameField = (EditText) rootView.findViewById(R.id.username);
    emailAddressField = (EditText) rootView.findViewById(R.id.emailAddress);
    passwordField = (EditText) rootView.findViewById(R.id.password);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    flowHelper = FlowHelper.getInstance(getActivity());
    initListeners();
  }

  private void initListeners() {
    signUpButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View signUpButton) {
        final String username = usernameField.getText().toString().toLowerCase();
        final String email = emailAddressField.getText().toString();
        final String password = passwordField.getText().toString();
        if (validate(username, email, password)) {
          showProgress(appContext.getString(R.string.signing_up));
          signUp(username, email, password);
        }
      }
    });

    new OnTextChangedListener(passwordField) {
      @Override
      public void onTextChanged(CharSequence s) {
        if (s.length() < Constants.FLOW_ACCOUNT_MINIMUM_CHARACTERS_COUNT) {
          signUpButton.setEnabled(false);
        } else {
          signUpButton.setEnabled(true);
        }
      }
    };
  }

  private boolean validate(String username, String email, String password) {
    boolean result = true;
    int usernameLength = username.length();
    if (usernameLength < 5) {
      usernameField.setError(appContext.getString(R.string.username_too_short));
      result = false;
    } else {
      usernameField.setError(null);
    }
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      emailAddressField.setError(appContext.getString(R.string.email_address_is_required));
      result = false;
    } else {
      emailAddressField.setError(null);
    }
    final int passLength = password.length();
    if (passLength < 5 || passLength > Constants.DEFAULT_MAXIMUM_FIELD_CHARACTERS_COUNT) {
      passwordField.setError(appContext.getString(R.string.incorrect_password_character_count));
      result = false;
    } else {
      passwordField.setError(null);
    }
    return result;
  }

  private void signUp(final String username, final String email, final String password) {
    BackgroundExecutor.execute(new ExternalRun() {
      @Override
      public void execute() {
        boolean isUserRegistered;
        try {
          isUserRegistered = flowHelper.registerNewUser(username, email, password);
          if (isUserRegistered) {
            if (flowHelper.userLoginToFlow(email, password)) {
              Preferences.saveEmailCredential(appContext, emailAddressField.getText().toString());
            }
            flowHelper.clearDevicesData();
            afterSignUp();
          } else {
            ActivitiesAndFragmentsHelper.showToast(appContext,
                ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
                handler);
          }
        } catch (ConflictException | BadRequestException ce) { //handled separately because getLastError returns no error for this case
          ActivitiesAndFragmentsHelper.showToast(appContext, R.string.signup_conflict, handler);
        } catch (FlowException e) {
          ActivitiesAndFragmentsHelper.showToast(appContext,
              ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
              handler);
        } finally {
          hideProgress();
        }
      }
    });
  }

  private void afterSignUp() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        ActivitiesAndFragmentsHelper.hideSoftInput(appContext, emailAddressField, passwordField);
        ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne(getActivity(), new Intent(getActivity(), FlowActivity.class));
      }
    });
  }

}
