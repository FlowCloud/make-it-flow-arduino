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

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.imgtec.flow.client.core.FlowException;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.adapters.ConnectedDevicesAdapter;
import com.imgtec.hobbyist.flow.DeviceOnlineInFlowListener;
import com.imgtec.hobbyist.flow.DevicePresenceListener;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.flow.WifireDevice;
import com.imgtec.hobbyist.fragments.menu.setupguide.SetUpADeviceFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalCall;
import com.imgtec.hobbyist.utils.ExternalRun;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.Preferences;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

import java.util.List;

/**
 * Fragment with list of devices (boards) owned by currently logged in Flow account.
 * ConnectedDevicesFragment is updated (and short sound is played) if one of connected
 * devices returns new presence state, e.g.:
 * device was offline, but is becoming online right now, and vice-versa.
 * There is an appropriate view if there are no devices connected with Flow account.
 */
public class ConnectedDevicesFragment extends NDListeningFragment implements DevicePresenceListener, DeviceOnlineInFlowListener,
    SwipeRefreshLayout.OnRefreshListener, BackgroundExecutor.Callbacks<List<WifireDevice>> {

  public static final String TAG = "ConnectedDevicesFragment";
  private static final int GET_DEVICES_ID = 1;
  private static final String DISABLE_INTERACTIVE_TILL_DEVICE_ONLINE = "DISABLE INTERACTIVE TILL DEVICE ONLINE";

  private ListView deviceListView;
  private LinearLayout devicesLayout;
  private RelativeLayout emptyLayout;
  private ProgressBar progressBar;
  private Button connectYourDevice;
  private Button interactWithSelected;
  private SwipeRefreshLayout swipeLayout;

  private List<WifireDevice> devices;
  private FlowHelper flowHelper;
  private SoundPool soundPool;
  private int soundId;
  private boolean soundPoolLoadedState = false;
  private float volume;
  private Handler handler = new Handler();
  private boolean disableInteractiveTillDeviceOnline = false;
  private Runnable activateListenerRunnable;

  private boolean cabActive = false;

  public static ConnectedDevicesFragment newInstance(boolean disableInteractiveTillDeviceOnline) {
    Bundle args = new Bundle();
    ConnectedDevicesFragment fragment = new ConnectedDevicesFragment();
    args.putBoolean(DISABLE_INTERACTIVE_TILL_DEVICE_ONLINE, disableInteractiveTillDeviceOnline);
    fragment.setArguments(args);
    return fragment;
  }

  public static ConnectedDevicesFragment newInstance() {
    return new ConnectedDevicesFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    flowHelper = FlowHelper.getInstance(getActivity());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_connected_devices, container, false);
    progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
    devicesLayout = (LinearLayout) rootView.findViewById(R.id.devicesLayout);
    emptyLayout = (RelativeLayout) rootView.findViewById(R.id.emptyLayout);
    connectYourDevice = (Button) rootView.findViewById(R.id.connectYourDevice);
    interactWithSelected = (Button) rootView.findViewById(R.id.interactWithSelected);
    deviceListView = (ListView) rootView.findViewById(R.id.deviceListView);
    swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeLayout);
    swipeLayout.setOnRefreshListener(this);
    swipeLayout.setColorScheme(R.color.light_purple, R.color.lime_green,
        R.color.dark_purple, R.color.nice_green);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    activity.onSelectionAndTitleChange(NDMenuItem.ConnectedDevices);
    initDeviceList();
    initSoundPool();
    if (getArguments() != null) {
      disableInteractiveTillDeviceOnline = getArguments().getBoolean(DISABLE_INTERACTIVE_TILL_DEVICE_ONLINE, false);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (disableInteractiveTillDeviceOnline) {
      activateListenerRunnable = new Runnable() {
        @Override
        public void run() {
          flowHelper.addDeviceOnlineInFlowListener(ConnectedDevicesFragment.this);
        }
      };
      handler.postDelayed(activateListenerRunnable, Constants.THIRTY_SECONDS_MILLIS);
    }
    flowHelper.addDevicePresenceListener(this);
  }

  @Override
  public void onPause() {
    flowHelper.removeDevicePresenceListener(this);
    flowHelper.removeDeviceOnlineInFlowListener(this);
    handler.removeCallbacks(activateListenerRunnable);
    super.onPause();
  }

  private void initSoundPool() {
    soundPool = new SoundPool(10, AudioManager.STREAM_NOTIFICATION, 0);
    soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
      @Override
      public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        soundPoolLoadedState = true;
      }
    });
    soundId = soundPool.load(appContext, R.raw.ding, 1);
    AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
    float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    volume = actualVolume / maxVolume;
  }

  @Override
  public void onDeviceOnlineInFlow() {
    disableInteractiveTillDeviceOnline = false;
    int id = deviceListView.getCheckedItemPosition();
    boolean connected = ((WifireDevice)deviceListView.getItemAtPosition(id)).isNetworkConnected();
    interactWithSelected.setEnabled(connected);
  }

  @Override
  public void onDevicePresenceChangeListener(boolean isConnected) {
    playSound();
    initDeviceList();
  }

  private void playSound() {
    if (soundPoolLoadedState) {
      soundPool.play(soundId, volume, volume, 1, 0, 1f);
    }
  }

  public void initDeviceList() {
    progressBar.setVisibility(View.VISIBLE);
    getDevices();
  }

  private void getDevices() {
    BackgroundExecutor.submit(new ExternalCall<List<WifireDevice>>() {
      @Override
      public List<WifireDevice> submit() {
        flowHelper.userLogBackInToFlow();
        return flowHelper.getWifireDeviceList();
      }
    }, this, GET_DEVICES_ID, appContext);
  }

  private void afterGetDevices() {
    if (devices == null || devices.isEmpty()) {
      updateNoDevices();
    } else {
      updateDevices();
    }
  }

  private void selectDevice(WifireDevice device) {
    if (device.isNetworkConnected()) {
      interactWithSelected.setEnabled(true);
    } else {
      interactWithSelected.setEnabled(false);
    }
  }

  private void updateNoDevices() {
    emptyLayout.setVisibility(View.VISIBLE);
    devicesLayout.setVisibility(View.GONE);
    connectYourDevice.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (activity != null) {
          activity.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(SetUpADeviceFragment.TAG));
          ((FlowActivity)activity).setUIMode(NDMenuMode.Initial);
        }
      }
    });
  }

  private void updateDevices() {
    restartDeviceList();
    devicesLayout.setVisibility(View.VISIBLE);
    emptyLayout.setVisibility(View.GONE);
    interactWithSelected.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (activity != null) {
          Preferences.interactiveModeHasStartedAtLeastOnce(((Activity) activity).getApplicationContext());
          setInteractiveMode();
          activity.onFragmentChange(SimpleFragmentFactory.createFragment(InteractiveModeFragment.TAG));
        }
      }

      private void setInteractiveMode() {
        flowHelper.setCurrentDevice(devices.get(deviceListView.getCheckedItemPosition()));
        ((FlowActivity)activity).setUIMode(NDMenuMode.Interactive);
      }
    });
  }

  private void restartDeviceList() {
    deviceListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    ConnectedDevicesAdapter devicesAdapter = new ConnectedDevicesAdapter(appContext, devices);
    deviceListView.setAdapter(devicesAdapter);
    initDeviceListContextualActions(devicesAdapter);
    deviceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (!cabActive) { //not called in cab. Call outside of cab starts cab.
          interactWithSelected.setEnabled(false);
          deviceListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
          cabActive = true;
        }
        return false;
      }
    });
    deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!cabActive) { //not called in cab except for first choice. First call after cab ends sets choice mode.
          deviceListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
          selectDevice(devices.get(position));
        }
        deviceListView.clearChoices();
        deviceListView.setItemChecked(position, true);
        deviceListView.requestLayout();
      }
    });
    restartFirstDevice();
  }

  private void initDeviceListContextualActions(final ConnectedDevicesAdapter devicesAdapter) {
    deviceListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

      @Override
      public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
        devicesAdapter.switchContextualChecked(position);
        actionMode.setTitle(deviceListView.getCheckedItemCount() + " selected");
      }

      @Override
      public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.devices_cab_menu, menu);
        return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
      }

      @Override
      public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
          case R.id.remove:
            removeDevices(devicesAdapter.getContextualSelectedDevices());
            devicesAdapter.clearContextualSelected();
            actionMode.finish();
            return true;
          default:
            return false;
        }
      }

      @Override
      public void onDestroyActionMode(ActionMode actionMode) {
        cabActive = false;
        devicesAdapter.clearContextualSelected();
        deviceListView.setAdapter(devicesAdapter); //needed to reset choices...
        interactWithSelected.setEnabled(false);
      }
    });
  }

  private void removeDevices(final List<WifireDevice> devices) {
    deviceListView.setAdapter(null);
    activity.onMenuChange(NDMenuMode.Initial);
    progressBar.setVisibility(View.VISIBLE);
    BackgroundExecutor.execute(new ExternalRun() {
      @Override
      public void execute() {
        try {
          for (WifireDevice device : devices) {
            flowHelper.setCurrentDevice(device); //needed to send reset and reboot commands to removed board
            flowHelper.removeDevice(device);
            flowHelper.setCurrentDevice(null);
          }
          getDevices();
        } catch (FlowException e) {
          DebugLogger.log(getClass().getSimpleName(), e);
          ActivitiesAndFragmentsHelper.showToast(appContext,
              ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
              handler);
        }
      }
    });
    progressBar.setVisibility(View.GONE);
  }

  private void restartFirstDevice() {
    deviceListView.setItemChecked(0, true);
    selectDevice(devices.get(0));
  }

  @Override
  public void onRefresh() {
    getDevices();
  }

  @Override
  public void onBackgroundExecutionResult(final List<WifireDevice> wifireDevices, final int taskCode) {
    if (activity != null) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (wifireDevices != null && taskCode == GET_DEVICES_ID) {
            devices = wifireDevices;
            afterGetDevices();
          }
          progressBar.setVisibility(View.GONE);
          swipeLayout.setRefreshing(false);
        }
      });
    }
  }
}