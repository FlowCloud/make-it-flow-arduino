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

package com.imgtec.hobbyist.fragments.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;

/**
 * Fragment representing a screen showing Flow communication error logs.
 */
public class ApplicationLogsFragment extends NDListeningFragment {

  public static final String TAG = "ApplicationLogsFragment";

  private WebView webView;
  private ProgressBar progressBar;
  private Button retrieveLogs;

  public static ApplicationLogsFragment newInstance() {
    return new ApplicationLogsFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_application_logs, container, false);
    ((TextView) rootView.findViewById(R.id.title)).setText(R.string.application_logs);
    webView = (WebView) rootView.findViewById(R.id.logsList);
    progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
    retrieveLogs = (Button) rootView.findViewById(R.id.retrieveLogs);
    return rootView;
  }

  @Override
  public void onStart() {
    super.onStart();
    retrieveLogs.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getApplicationLogs();
      }
    });
    getApplicationLogs();
  }

  public void getApplicationLogs() {
    progressBar.setVisibility(View.VISIBLE);
    webView.loadData(ErrorHtmlLogger.getHtml(), "text/html", "utf-8");
    progressBar.setVisibility(View.GONE);
  }
}
