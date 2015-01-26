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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imgtec.flow.client.core.Core;
import com.imgtec.flow.client.core.FlowException;
import com.imgtec.flow.client.extdep.cache.FlowCache;
import com.imgtec.flow.client.flowmessaging.FlowMessagingAddress;
import com.imgtec.flow.client.users.Device;
import com.imgtec.flow.client.users.Devices;
import com.imgtec.flow.client.users.User;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.flow.DeviceToFlowConnectionListener;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.flow.WifireDevice;
import com.imgtec.hobbyist.fragments.menu.ConnectedDevicesFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.utils.AnimationUtils;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.BroadcastReceiverWithRegistrationState;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalRun;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;
import com.imgtec.hobbyist.utils.WifiUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * Fragment showing WiFire board registration progress. After board successfully registers in Flow,
 * {@link com.imgtec.hobbyist.fragments.menu.ConnectedDevicesFragment} is automatically shown.
 */
public class ConnectingFragment extends NDListeningFragment implements DeviceToFlowConnectionListener {

  public static final String TAG = "ConnectingFragment";

  private ImageView ledsAnimation;
  private TextView connectionFailed;
  private TextView connecting;
  private Button cancelButton;
  private Button tryAgainButton;
  private Button doneButton;
  private TextView troubleshooting;
  private LinearLayout doneButtonContainer;

  private WifiUtil wifiUtil;
  private FlowHelper flowHelper;
  private Handler handler = new Handler();

  private NetworkChangeReceiver networkChangeReceiver =
      new NetworkChangeReceiver(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
  private Runnable waitForConnectionRunnable;
  private Runnable flowConnectedRunnable;
  private Runnable wifiConnectedToFlowRunnable;
  private BoardState currentBoardState = BoardState.WIFI_CONNECTING;

  /**
   * Possible states. WIFI_CONNECTED_MOCK, FLOW_CONNECTING_MOCK are mocked, because we cannot check them easily.
   * Device and mobile are in the same WiFi and mobile should know somehow how to get information about
   * WiFi network devices connected. It is possible, but for now we left it mocked.
   * {@link #FLOW_CONNECTING_MOCK} is actually {@link #FLOW_CONNECTED}.
   *
   * Can be upgraded IN THE FUTURE.
   */
  private enum BoardState {
    WIFI_CONNECTING, WIFI_CONNECTED_MOCK, FLOW_CONNECTING_MOCK, TAKING_LONG, FLOW_CONNECTED
  }

  public static ConnectingFragment newInstance() {
    return new ConnectingFragment();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    initFlow(activity);
  }

  private void initFlow(Activity activity) {
    flowHelper = FlowHelper.getInstance(activity);
    flowHelper.addFlowConnectedListener(this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_connecting, container, false);
    ledsAnimation = (ImageView) rootView.findViewById(R.id.ledsAnimation);
    connectionFailed = (TextView) rootView.findViewById(R.id.connectionFailed);
    connecting = (TextView) rootView.findViewById(R.id.connecting);
    cancelButton = (Button) rootView.findViewById(R.id.cancelButton);
    tryAgainButton = (Button) rootView.findViewById(R.id.tryAgainButton);
    doneButton = (Button) rootView.findViewById(R.id.doneButton);
    doneButtonContainer = (LinearLayout) rootView.findViewById(R.id.doneButtonContainer);
    troubleshooting = (TextView) rootView.findViewById(R.id.troubleshooting);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    currentBoardState = BoardState.WIFI_CONNECTING;
    initUIActions();
    updateUI();
    wifiUtil = new WifiUtil(appContext);
  }

  private void initUIActions() {
    troubleshooting.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FLOW_TROUBLESHOOTING_URL)));
      }
    });
    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        networkChangeReceiver.unregister(appContext);
        flowHelper.removeFlowConnectedListener(ConnectingFragment.this);
        if (activity != null) {
          activity.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
        }
      }
    });
    tryAgainButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        networkChangeReceiver.unregister(appContext);
        flowHelper.removeFlowConnectedListener(ConnectingFragment.this);
        if (activity != null) {
          activity.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(SetupModeFragment.TAG));
        }
      }
    });
    doneButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        networkChangeReceiver.unregister(appContext);
        flowHelper.removeFlowConnectedListener(ConnectingFragment.this);
        if (activity != null) {
          activity.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
        }
      }
    });
  }

  private void updateUI() {
    switch (currentBoardState) {
      case WIFI_CONNECTING:
        AnimationUtils.startAnimation(ledsAnimation, R.drawable.led12on3flashing);
        connectionFailed.setVisibility(View.GONE);
        connecting.setVisibility(View.VISIBLE);
        troubleshooting.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        tryAgainButton.setVisibility(View.GONE);
        break;
      case WIFI_CONNECTED_MOCK:
        AnimationUtils.startAnimation(ledsAnimation, R.drawable.led12on3flashing);
        break;
      case FLOW_CONNECTING_MOCK:
        AnimationUtils.startAnimation(ledsAnimation, R.drawable.led123on4flashing);
        break;
      case TAKING_LONG:
        AnimationUtils.animateViewSetVisible(true, troubleshooting);
        AnimationUtils.animateViewSetVisible(true, cancelButton);
        AnimationUtils.animateViewSetVisible(true, tryAgainButton);
        AnimationUtils.animateViewSetVisible(true, connectionFailed);
        AnimationUtils.animateViewSetVisible(false, connecting);
        //AnimationUtils.animateViewSetVisible(false, ledsAnimation);
        ledsAnimation.setVisibility(View.INVISIBLE);
        doneButtonContainer.setVisibility(View.GONE);
        break;
      case FLOW_CONNECTED:
        ledsAnimation.setBackgroundResource(R.drawable.leds_1234_lit);
        connecting.setText(R.string.device_setup_succeeded);
        doneButton.setVisibility(View.VISIBLE);
        break;
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    ((FlowActivity) activity).setUIMode(NDMenuMode.Initial);
    startConnecting();
    enableChosenNetwork();
  }

  private void startConnecting() {
    activity.onTitleChange(appContext.getString(R.string.device_setup));
    networkChangeReceiver.register(appContext);
    waitForConnectionRunnable = new Runnable() {
      @Override
      public void run() {
        if (currentBoardState != BoardState.FLOW_CONNECTING_MOCK && currentBoardState != BoardState.FLOW_CONNECTED) {
          currentBoardState = BoardState.TAKING_LONG;
          updateUI();
          handler.removeCallbacks(this);
        }
      }
    };
    handler.postDelayed(waitForConnectionRunnable, 3 * Constants.SIXTY_SECONDS_MILLIS);
  }

  private void enableChosenNetwork() {
    String ssid = "\"" + SetupGuideInfoSingleton.getInstance().getSsid() + "\"";
    wifiUtil.enableChosenNetwork(ssid);
  }

  @Override
  public void onPause() {
    handler.removeCallbacks(waitForConnectionRunnable);
    handler.removeCallbacks(wifiConnectedToFlowRunnable);
    handler.removeCallbacks(flowConnectedRunnable);
    networkChangeReceiver.unregister(appContext);
    super.onPause();
  }

  @Override
  public void onDetach() {
    flowHelper.removeFlowConnectedListener(this);
    super.onDetach();
  }

  private class NetworkChangeReceiver extends BroadcastReceiverWithRegistrationState {

    public NetworkChangeReceiver(IntentFilter intentFilter) {
      super(intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      NetworkInfo netInfo = new WifiUtil(context).getNetworkInfo();
      if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
        if (currentBoardState == BoardState.WIFI_CONNECTING) {
          startCheckingDeviceConnectedToFlow();
        }
      }
    }

    /**
     * For future's sake: background thread could not work correctly on Android API < 11 in BroadcastReceiver's onReceive().
     */
    private void startCheckingDeviceConnectedToFlow() {
      BackgroundExecutor.execute(new ExternalRun() {
        @Override
        public void execute() {
          try {
            wifiConnectedToFlowRunnable = new Runnable() {
              @Override
              public void run() {
                currentBoardState = BoardState.WIFI_CONNECTED_MOCK;
                updateUI();
              }
            };
            handler.post(wifiConnectedToFlowRunnable);
            Device device = waitForDevice(); //wait until device is added to user's owned
            waitForAddress(device); //make sure everything is set up
            flowHelper.getWifireDeviceList(); //to refresh cache and subscribe
          } catch (FlowException e) {
            DebugLogger.log(getClass().getSimpleName(), e);
            ActivitiesAndFragmentsHelper.showToast(appContext,
                ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
                handler);
          }
        }
      });
    }

    private Device waitForDevice() {
      int priorCount = FlowEntities.getInstance(appContext).getCachedDevices().size();
      User user = Core.getDefaultClient().getLoggedInUser();
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.add(Calendar.SECOND, 60);
      Date expiryTime = cal.getTime();

      Device configuringDevice = null;
      Devices devices;
      do {
        try {
          Thread.sleep(5000, 0);
        } catch (InterruptedException ignored) {
          DebugLogger.log(ConnectingFragment.class.getSimpleName(), "waitForDevice - thread interrupted");
        }
        FlowCache.clear();
        devices = user.getOwnedDevices();
        for (Device device : devices) {
          if (device.getMACAddress().compareTo(SetupGuideInfoSingleton.getInstance().getDeviceMacAddress()) == 0) {
            configuringDevice = device;
            break;
          }
        }
      } while ((configuringDevice == null) && (devices.size() <= priorCount) && new Date().before(expiryTime));
      return configuringDevice;
    }

    private void waitForAddress(Device device) {
      if (device != null) {
        FlowMessagingAddress address = null;
        while (address == null) {
          try {
            Thread.sleep(1000, 0);
            FlowCache.clear();
            address = device.getFlowMessagingAddress();
          } catch (InterruptedException ignored) {
            DebugLogger.log(ConnectingFragment.class.getSimpleName(), "waitForAddress - thread interrupted");
          }
        }
      }
    }
  }

  @Override
  public void onDeviceConnectedToFlow(WifireDevice device) {
    if (device.getName().equals(SetupGuideInfoSingleton.getInstance().getDeviceName())) {
      flowHelper.removeFlowConnectedListener(this);
      currentBoardState = BoardState.FLOW_CONNECTING_MOCK;
      updateUI();
      flowConnectedRunnable = new Runnable() {
        @Override
        public void run() {
          currentBoardState = BoardState.FLOW_CONNECTED;
          updateUI();
          handler.removeCallbacks(this);
        }
      };
      handler.postDelayed(flowConnectedRunnable, Constants.TWO_SECONDS_MILLIS);
    }
  }

}
