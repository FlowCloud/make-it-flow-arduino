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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.BaseActivity;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.spice.listeners.RequestListenerForDeviceInfo;
import com.imgtec.hobbyist.spice.listeners.RequestListenerForEmptyResponse;
import com.imgtec.hobbyist.spice.pojos.DeviceInfo;
import com.imgtec.hobbyist.spice.pojos.EmptyResponse;
import com.imgtec.hobbyist.spice.pojos.InnerNetworkConfig;
import com.imgtec.hobbyist.spice.pojos.NetworkConfig;
import com.imgtec.hobbyist.spice.requests.RobospiceRequestsHandler;
import com.imgtec.hobbyist.spice.requests.gets.GetDeviceInfoRequest;
import com.imgtec.hobbyist.spice.requests.posts.RebootWifireDeviceRequest;
import com.imgtec.hobbyist.spice.requests.posts.SetNetworkAndFlowConfigurationRequest;
import com.imgtec.hobbyist.utils.AnimationUtils;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.Preferences;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;
import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Fragment representing one of the screens of device setup.
 * Details of WiFi connection to be used by device are inputted on this screen.
 */
public class LogInToWifiFragment extends NDListeningFragment {

  public static final String TAG = "LogInToWifiFragment";
  public static final String UNKNOWN_SSID = "<unknown ssid>"; // Android default SSID value for no Internet connectivity.

  private SetupGuideInfoSingleton setupGuideInfoSingleton = SetupGuideInfoSingleton.getInstance();
  private Handler handler = new Handler();

  private enum StateOfSecurityProtocol {
    WEP(R.string.wep),
    WPA(R.string.wpa),
    WPA2(R.string.wpa2),
    OPEN(R.string.open);

    private final int textId;

    StateOfSecurityProtocol(int textId) {
      this.textId = textId;
    }

    public int getTextId() {
      return textId;
    }
  }

  private RobospiceRequestsHandler requestsHandler;
  private ProgressBar progressBar;
  private TextView headerTv;
  private TextView yourDevice;
  private TextView yourWifiNetwork;
  private EditText ssidField;
  private EditText passwordField;
  private EditText staticIpField;
  private EditText staticDnsField;
  private EditText staticNetmaskField;
  private EditText staticGatewayField;
  private RadioGroup securityProtocolChoice;
  private RadioGroup networkingProtocolChoice;
  private Button connect;
  private LinearLayout linksLayout;
  private Button selectAnotherNetwork;
  private Button manualConfiguration;
  private ImageView leds12Animation;
  private SharedPreferences sharedPreferences;
  private boolean isManual = false;
  private boolean isStaticIp = false;

  /**
   * Fragment state variables.
   */
  private String deviceMacAddress = "";
  private StateOfSecurityProtocol stateOfSecurityProtocol = StateOfSecurityProtocol.WPA2;

  public static LogInToWifiFragment newInstance() {
    return new LogInToWifiFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_login_to_wifi, container, false);
    progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
    headerTv = (TextView) rootView.findViewById(R.id.headerTv);
    yourDevice = (TextView) rootView.findViewById(R.id.yourDevice);
    yourWifiNetwork = (TextView) rootView.findViewById(R.id.yourWifiNetwork);
    ssidField = (EditText) rootView.findViewById(R.id.ssidField);
    passwordField = (EditText) rootView.findViewById(R.id.passwordField);
    staticIpField = (EditText) rootView.findViewById(R.id.staticIpField);
    staticDnsField = (EditText) rootView.findViewById(R.id.staticDnsField);
    staticNetmaskField = (EditText) rootView.findViewById(R.id.staticNetmaskField);
    staticGatewayField = (EditText) rootView.findViewById(R.id.staticGatewayField);
    securityProtocolChoice = (RadioGroup) rootView.findViewById(R.id.securityProtocolChoice);
    networkingProtocolChoice = (RadioGroup) rootView.findViewById(R.id.networkingProtocolChoice);
    connect = (Button) rootView.findViewById(R.id.connect);
    linksLayout = (LinearLayout) rootView.findViewById(R.id.linksLayout);
    selectAnotherNetwork = (Button) rootView.findViewById(R.id.selectAnotherNetwork);
    manualConfiguration = (Button) rootView.findViewById(R.id.manualConfiguration);
    leds12Animation = (ImageView) rootView.findViewById(R.id.leds12Animation);
    sharedPreferences = appContext.getSharedPreferences(Preferences.SETTINGS, Context.MODE_PRIVATE);
    requestsHandler = ((BaseActivity) activity).getRequestsHandler();
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    performGettingDeviceInfoRequest();
    initLedsAnimation();
    initLinks();
    initTextViews();
    initViewsListeners();
  }

  @Override
  public void onResume() {
    super.onResume();
    activity.onTitleChange(appContext.getString(R.string.log_in_to_wifi));
  }

  private void performGettingDeviceInfoRequest() {
    requestsHandler.performRequest(new RequestListenerForDeviceInfo() {
      @Override
      public void onRequestFailure(SpiceException e) {
        if (LogInToWifiFragment.this.isAdded()) {
          progressBar.setVisibility(View.GONE);
          ActivitiesAndFragmentsHelper.showToast(appContext, R.string.check_connectivity, handler);
        }
      }

      /**
       * Getting board's MAC address
       * @param deviceInfo
       */
      @Override
      public void onRequestSuccess(DeviceInfo deviceInfo) {
        if (LogInToWifiFragment.this.isAdded()) {
          SetupGuideInfoSingleton.getInstance().setDeviceName(deviceInfo.getInfo().getDeviceName());
          String success = deviceInfo.getSuccess();
          if (success != null && success.equals("true")) {
            deviceMacAddress = deviceInfo.getInfo().getMACAddress();
            SetupGuideInfoSingleton.getInstance().setDeviceMacAddress(deviceMacAddress);
          }
          updateUIAfterSuccessfulConnection();
        }
      }

      private void updateUIAfterSuccessfulConnection() {
        ActivitiesAndFragmentsHelper.showToast(appContext, R.string.successful_connection_with_board, handler);
        connect.setEnabled(true);
        ((FlowActivity) activity).setUIMode(NDMenuMode.Setup);
        progressBar.setVisibility(View.GONE);
      }

    }, new GetDeviceInfoRequest(appContext));
  }

  private void initLedsAnimation() {
    AnimationUtils.startAnimation(leds12Animation, R.drawable.led1on2flashing);
  }

  private void initTextViews() {
    String boardSSID = SetupGuideInfoSingleton.getInstance().getBoardSsid();
    yourDevice.setText(boardSSID);
    String currentNetworkSSID = SetupGuideInfoSingleton.getInstance().getSsid();
    yourWifiNetwork.setText(currentNetworkSSID);
  }

  private void initLinks() {
    selectAnotherNetwork.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        activity.onFragmentChange(SimpleFragmentFactory.createFragment(NetworkChoiceFragment.TAG, false));
      }
    });

    manualConfiguration.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AnimationUtils.animateViewSetVisible(true, ssidField);
        AnimationUtils.animateViewSetVisible(false, linksLayout);
        AnimationUtils.animateViewSetVisible(false, headerTv);
        AnimationUtils.animateViewSetVisible(false, yourWifiNetwork);
        AnimationUtils.animateViewSetVisible(true, networkingProtocolChoice);
        isManual = true;
      }
    });
  }

  private void initViewsListeners() {
    securityProtocolChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
          case R.id.wep:
            stateOfSecurityProtocol = StateOfSecurityProtocol.WEP;
            break;
          case R.id.wpa:
            stateOfSecurityProtocol = StateOfSecurityProtocol.WPA;
            break;
          case R.id.wpa2:
            stateOfSecurityProtocol = StateOfSecurityProtocol.WPA2;
            break;
          case R.id.open:
            stateOfSecurityProtocol = StateOfSecurityProtocol.OPEN;
            passwordField.setError(null);
            break;
        }
      }
    });

    networkingProtocolChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.dhcp) {
          AnimationUtils.animateViewSetVisible(false, staticIpField);
          AnimationUtils.animateViewSetVisible(false, staticDnsField);
          AnimationUtils.animateViewSetVisible(false, staticNetmaskField);
          AnimationUtils.animateViewSetVisible(false, staticGatewayField);
          isStaticIp = false;
        } else if (checkedId == R.id.staticIP) {
          AnimationUtils.animateViewSetVisible(true, staticIpField);
          AnimationUtils.animateViewSetVisible(true, staticDnsField);
          AnimationUtils.animateViewSetVisible(true, staticNetmaskField);
          AnimationUtils.animateViewSetVisible(true, staticGatewayField);
          isStaticIp = true;
        }
      }
    });

    connect.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        if (validate()) {
          progressBar.setVisibility(View.VISIBLE);
          NetworkConfig networkConfig = getNetworkConfig();
          performConnectWithDevice(networkConfig);
        }
      }

      private boolean validate() {
        if (!(stateOfSecurityProtocol == StateOfSecurityProtocol.OPEN)) {
          if (!isFieldValid(passwordField, R.string.password_is_required)) {
            return false;
          }
        }
        if (isManual) {
          if (!isFieldValid(ssidField, R.string.wireless_name_is_required)) {
            return false;
          }
          if (isStaticIp) {
            if (!isFieldValid(staticIpField, R.string.static_ip_is_required)) {
              return false;
            }
            if (!isFieldValid(staticDnsField, R.string.dns_is_required)) {
              return false;
            }
            if (!isFieldValid(staticNetmaskField, R.string.netmask_is_required)) {
              return false;
            }
            if (!isFieldValid(staticGatewayField, R.string.gateway_is_required)) {
              return false;
            }
          }
        }
        if (yourWifiNetwork.getText().toString().equalsIgnoreCase(UNKNOWN_SSID)) {
          ActivitiesAndFragmentsHelper.showFragmentChangeDialog(
              R.string.ssid_is_wrong,
              R.string.back_to_set_up_a_device,
              (FlowActivity) activity,
              SimpleFragmentFactory.createFragment(SetUpADeviceFragment.TAG));
          return false;
        }
        return true;
      }

      private boolean isFieldValid(EditText field, int errorRes) {
        if (TextUtils.isEmpty(field.getText().toString())) {
          field.setError(appContext.getString(errorRes));
          field.requestFocus();
          return false;
        } else {
          field.setError(null);
          return true;
        }
      }
    });
  }

  private NetworkConfig getNetworkConfig() {
    NetworkConfig networkConfig;
    if (isStaticIp) {
      networkConfig = new NetworkConfig(appContext.getString(R.string.true_text), //"true" by default
          new InnerNetworkConfig(appContext.getString(R.string.wifire_device),
              sharedPreferences.getString(Preferences.OAUTH_KEY, Preferences.OAUTH_KEY_DEFAULT_VALUE),
              sharedPreferences.getString(Preferences.OAUTH_SECRET, Preferences.OAUTH_SECRET_DEFAULT_VALUE),
              getRegistrationKey(),
              sharedPreferences.getString(Preferences.ROOT_URL, Preferences.ROOT_URL_DEFAULT_VALUE),
              isManual ? ssidField.getText().toString() : yourWifiNetwork.getText().toString(),
              appContext.getString(stateOfSecurityProtocol.getTextId()),
              passwordField.getText().toString(),
              appContext.getString(R.string.static_ip),
              staticDnsField.getText().toString(),
              staticIpField.getText().toString(),
              staticNetmaskField.getText().toString(),
              staticGatewayField.getText().toString())
      );
    } else {
      networkConfig = new NetworkConfig(appContext.getString(R.string.true_text), //"true" by default
          new InnerNetworkConfig(appContext.getString(R.string.wifire_device),
              sharedPreferences.getString(Preferences.OAUTH_KEY, Preferences.OAUTH_KEY_DEFAULT_VALUE),
              sharedPreferences.getString(Preferences.OAUTH_SECRET, Preferences.OAUTH_SECRET_DEFAULT_VALUE),
              getRegistrationKey(),
              sharedPreferences.getString(Preferences.ROOT_URL, Preferences.ROOT_URL_DEFAULT_VALUE),
              isManual ? ssidField.getText().toString() : yourWifiNetwork.getText().toString(),
              appContext.getString(stateOfSecurityProtocol.getTextId()),
              passwordField.getText().toString(),
              appContext.getString(R.string.dhcp))
      );
    }
    return networkConfig;
  }


  /**
   * There are few cases of getting registrationKey:
   * 1) If device is registered, always use key from Flow. If device has a registrationKey,
   * keys on device and in flow may be different. Flow key always takes precedence.
   * 2) If device is NOT registered in Flow - get new "free" key from Flow and use it,
   * also regardless of what already is on the device.
   *
   * @return correct registrationKey
   */
  private String getRegistrationKey() {
    String claimedKey = getClaimedKeyForMAC(deviceMacAddress);
    if (isKeyExistForMAC(claimedKey)) { //1
      return claimedKey;
    } else { //2
      return setupGuideInfoSingleton.getFreeRegistrationKey();
    }
  }

  private boolean isKeyExistForMAC(String claimedKey) {
    return !TextUtils.isEmpty(claimedKey);
  }

  private String getClaimedKeyForMAC(String mac) {
    for (Pair<String, String> pair : setupGuideInfoSingleton.getListOfKeyMACPairs()) {
      if (pair.second.equalsIgnoreCase(mac)) {
        return pair.first;
      }
    }
    return "";
  }

  private void performConnectWithDevice(NetworkConfig networkConfig) {
    requestsHandler.performRequest(new RequestListenerForEmptyResponse() {
      @Override
      public void onRequestFailure(SpiceException e) {
        if (LogInToWifiFragment.this.isAdded()) {
          progressBar.setVisibility(View.GONE);
          ActivitiesAndFragmentsHelper.showToast(appContext, R.string.check_connectivity, handler);
        }
      }

      @Override
      public void onRequestSuccess(EmptyResponse result) {
        performRebootRequest();
      }
    }, new SetNetworkAndFlowConfigurationRequest(appContext, networkConfig));
  }

  private void performRebootRequest() {
    requestsHandler.performRequest(new RequestListenerForEmptyResponse() {
      @Override
      public void onRequestFailure(SpiceException e) {
        if (LogInToWifiFragment.this.isAdded()) {
          progressBar.setVisibility(View.GONE);
        }
        ActivitiesAndFragmentsHelper.showToast(appContext, R.string.connection_failure_try_again, handler);
      }

      @Override
      public void onRequestSuccess(EmptyResponse result) {
        startFragmentAfterRestartedDevice();
      }
    }, new RebootWifireDeviceRequest(appContext));

  }

  private void startFragmentAfterRestartedDevice() {
    if (this.isAdded()) {
      progressBar.setVisibility(View.GONE);
      if (activity != null) {
        activity.onFragmentChange(SimpleFragmentFactory.createFragment(ConnectingFragment.TAG));
      }
    }
  }

}
