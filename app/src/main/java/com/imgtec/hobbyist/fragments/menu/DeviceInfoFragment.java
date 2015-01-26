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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.BaseActivity;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.spice.listeners.RequestListenerForDeviceInfo;
import com.imgtec.hobbyist.spice.listeners.RequestListenerForEmptyResponse;
import com.imgtec.hobbyist.spice.pojos.DeviceInfo;
import com.imgtec.hobbyist.spice.pojos.DeviceName;
import com.imgtec.hobbyist.spice.pojos.EmptyResponse;
import com.imgtec.hobbyist.spice.pojos.Info;
import com.imgtec.hobbyist.spice.requests.RobospiceRequestsHandler;
import com.imgtec.hobbyist.spice.requests.gets.GetDeviceInfoRequest;
import com.imgtec.hobbyist.spice.requests.posts.SetDeviceNameRequest;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;
import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Fragment representing a screen with WiFire board details.
 * Information is retrieved from REST API (board itself), not from Flow.
 */
public class DeviceInfoFragment extends NDListeningFragment {

  public static final String TAG = "DeviceInfoFragment";
  private RobospiceRequestsHandler requestsHandler;
  private TextView macValue;
  private TextView serialNumberValue;
  private TextView deviceTypeValue;
  private TextView softwareVersionValue;
  private TextView nameValue;
  private ProgressBar progressBar;
  private EditText deviceNameField;
  private Button saveButton;
  private Handler handler = new Handler();

  public static DeviceInfoFragment newInstance() {
    return new DeviceInfoFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_device_info, container, false);
    progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
    macValue = (TextView) rootView.findViewById(R.id.macValue);
    serialNumberValue = (TextView) rootView.findViewById(R.id.serialNumberValue);
    deviceTypeValue = (TextView) rootView.findViewById(R.id.deviceTypeValue);
    softwareVersionValue = (TextView) rootView.findViewById(R.id.softwareVersionValue);
    nameValue = (TextView) rootView.findViewById(R.id.nameValue);
    deviceNameField = (EditText) rootView.findViewById(R.id.deviceNameField);
    saveButton = (Button) rootView.findViewById(R.id.saveButton);
    initSaveButtonListener();

    return rootView;
  }

  private void initSaveButtonListener() {
    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final String deviceName = deviceNameField.getText().toString();
        if (activity != null && validate(deviceName)) {
          progressBar.setVisibility(View.VISIBLE);
          requestsHandler.performRequest(new RequestListenerForEmptyResponse() {
            @Override
            public void onRequestFailure(SpiceException e) {
              if (DeviceInfoFragment.this.isAdded()) {
                progressBar.setVisibility(View.GONE);
                ActivitiesAndFragmentsHelper.showToast(appContext, R.string.check_connectivity, handler);
              }
            }

            @Override
            public void onRequestSuccess(EmptyResponse emptyResponse) {
              if (DeviceInfoFragment.this.isAdded()) {
                updateDeviceName(deviceName);
              }
            }
          }, new SetDeviceNameRequest(appContext, new DeviceName("true", deviceName)));
        }
      }

      private boolean validate(String deviceName) {
        boolean result = true;
        if (deviceName.length() > Constants.DEFAULT_MAXIMUM_FIELD_CHARACTERS_COUNT || deviceName.length() == 0) {
          deviceNameField.setError(appContext.getString(R.string.incorrect_field_character_count));
          result = false;
        } else {
          deviceNameField.setError(null);
        }
        return result;
      }

    });
  }

  private void updateDeviceName(String deviceName) {
    SetupGuideInfoSingleton.getInstance().setDeviceName(deviceName);
    if(activity != null) {
      activity.onTitleChange(deviceName);
      ((FlowActivity) activity).setUIMode(NDMenuMode.Setup);// for NDFragmentMenu actualization
    }
    performGetDeviceInfoRequest();
  }

  @Override
  public void onStart() {
    super.onStart();
    requestsHandler = ((BaseActivity) getActivity()).getRequestsHandler();
    performGetDeviceInfoRequest();
  }

  private void performGetDeviceInfoRequest() {
    progressBar.setVisibility(View.VISIBLE);
    requestsHandler.performRequest(new RequestListenerForDeviceInfo() {
      @Override
      public void onRequestFailure(SpiceException e) {
        if (DeviceInfoFragment.this.isAdded()) {
          progressBar.setVisibility(View.GONE);
          Toast.makeText(appContext, appContext.getString(R.string.check_connectivity), Toast.LENGTH_SHORT).show();
        }
      }

      @Override
      public void onRequestSuccess(DeviceInfo deviceInfo) {
        if (DeviceInfoFragment.this.isAdded()) {
          updateDeviceInfo(deviceInfo);
        }
      }
    }, new GetDeviceInfoRequest(appContext));
  }

  private void updateDeviceInfo(DeviceInfo deviceInfo) {
    ((FlowActivity)activity).setUIMode(NDMenuMode.Setup);// for NDFragmentMenu actualization
    if (deviceInfo != null && deviceInfo.getSuccess().equals("true")) {
      Info info = deviceInfo.getInfo();
      macValue.setText(info.getMACAddress());
      serialNumberValue.setText(info.getSerialNumber());
      deviceTypeValue.setText(info.getDeviceType());
      softwareVersionValue.setText(info.getSoftwareVersion());
      String deviceName = info.getDeviceName();
      nameValue.setText(deviceName);
      if (activity != null) {
        activity.onTitleChange(deviceName);
      }
      progressBar.setVisibility(View.GONE);
    }
  }

}

