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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.utils.Constants;

public class HelpWifiNetworkFragment extends Fragment {

  public static final String TAG = "HelpWifiNetworkFragment";

  WebView webViewTable;

  public static HelpWifiNetworkFragment newInstance() {
    return new HelpWifiNetworkFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_wifi_network_help, container, false);
    webViewTable = (WebView) rootView.findViewById(R.id.webViewTable);
    webViewTable.loadData(Constants.WIFIRE_BUTTON_COMMANDS_HTML_TABLE, "text/html", "utf-8");
    return rootView;
  }

}
