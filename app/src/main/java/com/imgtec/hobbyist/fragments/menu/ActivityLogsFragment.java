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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.BaseActivity;
import com.imgtec.hobbyist.adapters.LogsAdapter;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.spice.listeners.RequestListenerForDeviceLog;
import com.imgtec.hobbyist.spice.pojos.DeviceLog;
import com.imgtec.hobbyist.spice.requests.RobospiceRequestsHandler;
import com.imgtec.hobbyist.spice.requests.gets.GetLogRequest;
import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Fragment representing a screen containing logs loaded from device.
 */
public class ActivityLogsFragment extends NDListeningFragment {

  public static final String TAG = "ActivityLogsFragment";
  private RobospiceRequestsHandler requestsHandler;
  private ListView listView;
  private ProgressBar progressBar;
  private Button retrieveLogs;
  private RequestListenerForDeviceLog requestListener;
  private LogsAdapter adapter;

  public static ActivityLogsFragment newInstance() {
    return new ActivityLogsFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_activity_log_entries, container, false);
    listView = (ListView) rootView.findViewById(R.id.logsList);
    listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
    retrieveLogs = (Button) rootView.findViewById(R.id.retrieveLogs);
    return rootView;
  }

  @Override
  public void onStart() {
    super.onStart();
    requestListener = new RequestListenerForDeviceLog() {
      @Override
      public void onRequestFailure(SpiceException e) {
        if (ActivityLogsFragment.this.isAdded()) {
          progressBar.setVisibility(View.GONE);
          if (getActivity() != null) {
            Toast.makeText(appContext, appContext.getString(R.string.check_connectivity), Toast.LENGTH_SHORT).show();
          }
        }
      }

      @Override
      public void onRequestSuccess(DeviceLog deviceLog) {
        if (ActivityLogsFragment.this.isAdded()) {
          if (deviceLog != null) {
            adapter = new LogsAdapter(appContext, deviceLog.getList());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.clearChoices();
                listView.requestLayout();
                listView.setItemChecked(position, true);
              }
            });
          }
          progressBar.setVisibility(View.GONE);
        }
      }
    };
    requestsHandler = ((BaseActivity) getActivity()).getRequestsHandler();
    initRetrieveLogsButton();
    getActivityLogEntries();
  }

  public void initRetrieveLogsButton() {
    retrieveLogs.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivityLogEntries();
      }
    });
  }

  public void getActivityLogEntries() {
    progressBar.setVisibility(View.VISIBLE);
    requestsHandler.performRequest(requestListener, new GetLogRequest(appContext));
  }
}
