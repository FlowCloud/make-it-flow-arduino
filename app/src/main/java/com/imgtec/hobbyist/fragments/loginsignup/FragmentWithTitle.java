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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.imgtec.hobbyist.R;

/**
 * Fragment capable of changing title of the ActionBar of activity the fragment is attached to.
 * Changing the title is based on fragment's lifecycle.
 */
public abstract class FragmentWithTitle extends Fragment {

  protected TextView actionBarTitle;
  protected ActionBar actionBar;
  protected Context appContext;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    appContext = activity.getApplicationContext();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
    actionBarTitle = (TextView) (actionBar.getCustomView().findViewById(R.id.actionBarTitle));
    setActionBarTitleText();
  }

  @Override
  public void onDestroyView() {
    actionBarTitle.setText("");
    super.onDestroyView();
  }

  protected abstract void setActionBarTitleText();

}
