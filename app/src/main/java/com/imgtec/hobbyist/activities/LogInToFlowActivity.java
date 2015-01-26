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

import android.os.Bundle;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.fragments.loginsignup.LogInFragment;
import com.imgtec.hobbyist.fragments.loginsignup.LogInOrSignUpFragment;
import com.imgtec.hobbyist.fragments.loginsignup.SettingsFragment;
import com.imgtec.hobbyist.fragments.loginsignup.SignUpFragment;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

/**
 * Simple activity encapsulated LogIn, SignUp and Settings fragments.
 */
public class LogInToFlowActivity extends BaseActivity implements LogInOrSignUpFragment.OnFragmentInteractionListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.actv_login_to_flow);
    getSupportActionBar().setDisplayShowHomeEnabled(false);
    ActivitiesAndFragmentsHelper.replaceFragmentWithBackStackClear(this, SimpleFragmentFactory.createFragment(LogInOrSignUpFragment.TAG));
  }

  @Override
  public void logInButtonListener() {
    ActivitiesAndFragmentsHelper.replaceFragment(this, SimpleFragmentFactory.createFragment(LogInFragment.TAG));
  }

  @Override
  public void signUpButtonListener() {
    ActivitiesAndFragmentsHelper.replaceFragment(this, SimpleFragmentFactory.createFragment(SignUpFragment.TAG));
  }

  @Override
  public void serverSettingsListener() {
    ActivitiesAndFragmentsHelper.replaceFragment(this, SimpleFragmentFactory.createFragment(SettingsFragment.TAG));
  }

  public void setActionBarTitle(int resource) {
    actionBarTitleView.setText(resource);
  }
}
