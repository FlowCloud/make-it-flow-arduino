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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.adapters.BoardWifiChoiceListAdapter;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;
import com.imgtec.hobbyist.utils.WifiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment representing one of the screens of device setup.
 * Allows picking network of a device that is to be configured.
 * Basing on {@link #IS_BOARD_WIFI_LIST_FRAGMENT_ARG} value this fragment can show different lists:
 * true  - showing list of WiFire device's networks.
 * false - showing list of all available networks.
 */
public class NetworkChoiceFragment extends NDListeningFragment {

  public static final String IS_BOARD_WIFI_LIST_FRAGMENT_ARG = "IS_BOARD_WIFI_LIST_FRAGMENT_ARG";
  public static final String TAG = "NetworkChoiceFragment";

  private ImageView dots;
  private ListView wifiSSIDs;
  private TextView selectBoard;
  private WifiUtil wifiUtil;
  private Handler handler = new Handler();
  private List<String> ssidList = new ArrayList<>();

  private boolean isBoardWifiListFragment;
  private Runnable refreshWifiListRunnable;

  public static NetworkChoiceFragment newInstance(boolean isBoardWifiList) {
    Bundle args = new Bundle();
    args.putBoolean(IS_BOARD_WIFI_LIST_FRAGMENT_ARG, isBoardWifiList);
    final NetworkChoiceFragment fragment = new NetworkChoiceFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_board_wifi_choice_fragment, container, false);
    dots = (ImageView) rootView.findViewById(R.id.dots);
    wifiSSIDs = (ListView) rootView.findViewById(R.id.wifiSSIDs);
    selectBoard = (TextView) rootView.findViewById(R.id.selectBoard);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    init();
  }

  @Override
  public void onResume() {
    super.onResume();
    updateUI();
    activity.onTitleChange(isBoardWifiListFragment ? appContext.getString(R.string.select_device) : appContext.getString(R.string.log_in_to_wifi));
  }

  private void init() {
    isBoardWifiListFragment = getArguments().getBoolean(IS_BOARD_WIFI_LIST_FRAGMENT_ARG);
    wifiUtil = new WifiUtil(appContext);
    wifiSSIDs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (activity != null) {
          SetupGuideInfoSingleton setupGuideInfoSingleton = SetupGuideInfoSingleton.getInstance();
          if (isBoardWifiListFragment) {
            setupGuideInfoSingleton.setBoardSsid(ssidList.get(position));
            activity.onFragmentChange(SimpleFragmentFactory.createFragment(LoginToDeviceFragment.TAG));
          } else {
            setupGuideInfoSingleton.setSsid(ssidList.get(position));
            activity.onFragmentChange(SimpleFragmentFactory.createFragment(LogInToWifiFragment.TAG));
          }
        }
      }
    });
  }

  /**
   * Constant refreshing with interval of {@link com.imgtec.hobbyist.utils.Constants#TWO_SECONDS_MILLIS}
   */
  private void updateUI() {
    refreshWifiListRunnable = new Runnable() {
      @Override
      public void run() {
        // It's asynchronous, but it should be refreshed, because it really speeds up the refreshing process.
        // Android scans networks rarely, by default, because of performance.
        wifiUtil.startWifiScan();
        int delay;
        if (isBoardWifiListFragment) {
          updateBoardWifiList();
          dots.setVisibility(View.VISIBLE);
          delay = Constants.TWO_SECONDS_MILLIS;
        } else {
          updateWifiList();
          dots.setVisibility(View.GONE);
          selectBoard.setText(R.string.please_select_your_wifi_network);
          delay = Constants.TEN_SECONDS_MILLIS;
        }
        handler.postDelayed(this, delay);
      }
    };
    handler.post(refreshWifiListRunnable);
  }

  private void updateBoardWifiList() {
    ssidList = wifiUtil.getBoardWifiList();
    Collections.sort(ssidList);
    wifiSSIDs.setAdapter(new BoardWifiChoiceListAdapter(appContext, ssidList));
  }

  private void updateWifiList() {
    ssidList = wifiUtil.getAvailableWifiList();
    Collections.sort(ssidList);
    wifiSSIDs.setAdapter(new BoardWifiChoiceListAdapter(appContext, ssidList));
  }

  @Override
  public void onPause() {
    handler.removeCallbacks(refreshWifiListRunnable);
    super.onPause();
  }

}
