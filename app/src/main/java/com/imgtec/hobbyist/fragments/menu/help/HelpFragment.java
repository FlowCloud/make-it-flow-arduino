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

package com.imgtec.hobbyist.fragments.menu.help;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

public class HelpFragment extends NDListeningFragment {

  public static final String TAG = "HelpFragment";

  private Button flowCloudForum;
  private Button introduction;
  private Button wirelessNetworks;
  private Button deviceSetup;
  private Button usingTheApp;
  private Button usingTheBoard;
  private Button troubleshooting;

  public static HelpFragment newInstance() {
    return new HelpFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_help, container, false);
    introduction = (Button) rootView.findViewById(R.id.introduction);
    wirelessNetworks = (Button) rootView.findViewById(R.id.wirelessNetworks);
    deviceSetup = (Button) rootView.findViewById(R.id.deviceSetup);
    usingTheApp = (Button) rootView.findViewById(R.id.usingTheApp);
    usingTheBoard = (Button) rootView.findViewById(R.id.usingTheBoard);
    troubleshooting = (Button) rootView.findViewById(R.id.troubleshooting);
    flowCloudForum = (Button) rootView.findViewById(R.id.flowCloudForum);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    introduction.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (activity != null) {
          activity.onFragmentChange(SimpleFragmentFactory.createFragment(HelpIntroductionFragment.TAG));
        }
      }
    });
    wirelessNetworks.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (activity != null) {
          activity.onFragmentChange(SimpleFragmentFactory.createFragment(HelpWifiNetworkFragment.TAG));
        }
      }
    });
    deviceSetup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (activity != null) {
          activity.onFragmentChange(SimpleFragmentFactory.createFragment(HelpDeviceSetupFragment.TAG));
        }
      }
    });
    usingTheApp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (activity != null) {
          activity.onFragmentChange(SimpleFragmentFactory.createFragment(HelpUsingTheAppFragment.TAG));
        }
      }
    });
    usingTheBoard.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (activity != null) {
          activity.onFragmentChange(SimpleFragmentFactory.createFragment(HelpUsingTheBoardFragment.TAG));
        }
      }
    });
    troubleshooting.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FLOW_TROUBLESHOOTING_URL)));
      }
    });
    flowCloudForum.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FLOW_FORUM_URL)));
      }
    });
  }
}
