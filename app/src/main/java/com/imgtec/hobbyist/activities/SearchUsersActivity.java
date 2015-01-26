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

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.ExternalCall;
import com.imgtec.hobbyist.utils.OnTextChangedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a screen where user base may be searched for specified user.
 * Being a part of text messaging functionality, it is currently not available in the application.
 */
public class SearchUsersActivity extends ListActivity implements BackgroundExecutor.Callbacks<Map<String, String>> {

  public static final int REQUEST_CODE = 5;
  public static final String FLOW_USER_NAME = "name";
  public static final String FLOW_USER_AOR = "aor";
  private static final int GET_USERS_ID = 1;

  private EditText filterEditText;
  private ImageView clearIcon;
  private ProgressBar progressBar;
  private List<String> usersList = new ArrayList<>();
  private Map<String, String> usersMap = new HashMap<>();
  private FlowHelper flowHelper;
  private Context appContext;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.actv_search_users);
    appContext = getApplicationContext();
    progressBar = (ProgressBar) findViewById(R.id.progressBar);
    filterEditText = (EditText) findViewById(R.id.filterBox);

    new OnTextChangedListener(filterEditText) {
      @Override
      public void onTextChanged(CharSequence s) {
        usersList.clear();
        for (String username : usersMap.keySet()) {
          if (username.contains(s.toString())) {
            usersList.add(username);
          }
        }
        getListView().setAdapter(new ArrayAdapter<>(SearchUsersActivity.this,
            android.R.layout.simple_list_item_1, usersList));
      }
    };

    clearIcon = (ImageView) findViewById(R.id.clearIcon);
    clearIcon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        filterEditText.setText("");
      }
    });

    getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(FLOW_USER_AOR, usersMap.get(usersList.get(position)));
        resultIntent.putExtra(FLOW_USER_NAME, usersList.get(position));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
      }
    });

    getUsersAndInitList();
  }

  private void getUsersAndInitList() {
    setLoadingVisible(true);
    flowHelper = FlowHelper.getInstanceAndRestartAppIfRequired(this);
    getUsers();
  }

  private void getUsers() {
    BackgroundExecutor.submit(new ExternalCall<Map<String, String>>() {
      @Override
      public Map<String, String> submit() {
        return flowHelper.getUsersMap();
      }
    }, this, GET_USERS_ID, appContext);
  }

  private void initList() {
    usersList.addAll(usersMap.keySet());
    getListView().setAdapter(new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_1, usersList));
  }

  private void setLoadingVisible(boolean visible) {
    if (visible) {
      progressBar.setVisibility(View.VISIBLE);
      getListView().setVisibility(View.GONE);
    } else {
      progressBar.setVisibility(View.GONE);
      getListView().setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onBackgroundExecutionResult(final Map<String, String> map, int taskCode) {
    if (usersMap != null && taskCode == GET_USERS_ID) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          usersMap = map;
          initList();
          setLoadingVisible(false);
        }
      });
    }
  }
}
