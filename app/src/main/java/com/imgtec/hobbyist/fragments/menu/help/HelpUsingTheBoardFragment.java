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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.utils.Constants;

public class HelpUsingTheBoardFragment extends Fragment {

  public static final String TAG = "HelpUsingTheBoardFragment";

  public static HelpUsingTheBoardFragment newInstance() {
    return new HelpUsingTheBoardFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_using_the_board_help, container, false);
    Button flowCloudLink = (Button) rootView.findViewById(R.id.flowCloudLink);
    flowCloudLink.setText(Constants.FLOW_USING_BOARD_URL);
    flowCloudLink.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FLOW_USING_BOARD_URL)));
      }
    });
    return rootView;
  }

}
