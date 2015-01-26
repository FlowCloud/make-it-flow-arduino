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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

/**
 * Fragment representing one of the screens of device setup.
 * This is an initial screen of setup.
 */
public class SetupModeFragment extends NDListeningFragment {

  public static final String TAG = "SetupModeFragment";

  public static SetupModeFragment newInstance() {
    return new SetupModeFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_setup_mode, container, false);
    Button continueButton = (Button) rootView.findViewById(R.id.continueButton);
    continueButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (activity != null) {
          activity.onFragmentChange(SimpleFragmentFactory.createFragment(NetworkChoiceFragment.TAG, true));
        }
      }
    });

    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    activity.onTitleChange(appContext.getString(R.string.setup_mode));
  }
}
