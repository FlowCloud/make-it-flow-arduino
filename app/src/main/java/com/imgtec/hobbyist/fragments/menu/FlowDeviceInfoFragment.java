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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.imgtec.flow.client.core.FlowException;
import com.imgtec.flow.client.users.Device;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.flow.Command;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.flow.WifireDevice;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalCall;
import com.imgtec.hobbyist.utils.NDMenuMode;

/**
 * Fragment representing a screen with WiFire board details.
 * Information is retrieved from Flow.
 */
public class FlowDeviceInfoFragment extends NDListeningFragment implements BackgroundExecutor.Callbacks<Boolean> {

  public static final String TAG = "FlowDeviceInfoFragment";
  private static final int CHANGE_NAME_ID = 1;
  private TextView macValue;
  private TextView serialNumberValue;
  private TextView deviceTypeValue;
  private TextView softwareVersionValue;
  private TextView nameValue;
  private ProgressBar progressBar;
  private Button saveButton;
  private EditText deviceNameField;
  private FlowHelper flowHelper;

  public static FlowDeviceInfoFragment newInstance() {
    return new FlowDeviceInfoFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    flowHelper = FlowHelper.getInstance(getActivity());
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
    saveButton = (Button) rootView.findViewById(R.id.saveButton);
    deviceNameField = (EditText) rootView.findViewById(R.id.deviceNameField);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupNextButton();
    updateDeviceInfo();
  }

  private void setupNextButton() {
    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (validate()) {
          changeDeviceName(deviceNameField.getText().toString());
        }
      }

      private boolean validate() {
        if (TextUtils.isEmpty(deviceNameField.getText().toString())) {
          deviceNameField.setError(appContext.getString(R.string.device_name_required));
          deviceNameField.requestFocus();
          return false;
        } else {
          return true;
        }
      }
    });
  }

  private void updateDeviceInfo() {
    WifireDevice wifireDevice = flowHelper.getCurrentDevice();
    if (wifireDevice != null) {
      Device device = wifireDevice.getDevice();
      if (device != null) {
        macValue.setText(device.getMACAddress());
        serialNumberValue.setText(device.getSerialNumber());
        deviceTypeValue.setText(device.getDeviceType());
        softwareVersionValue.setText(device.getSoftwareVersion());
        nameValue.setText(device.getDeviceName());
        deviceNameField.setText(device.getDeviceName());
        if (activity != null) {
          activity.onTitleChange(device.getDeviceName());
        }
      }
    } else {
      DebugLogger.log(TAG, appContext.getString(R.string.log_device_info_error));
    }
    progressBar.setVisibility(View.GONE);
  }

  private void changeDeviceName(final String name) {
    final Handler handler = new Handler();
    progressBar.setVisibility(View.VISIBLE);
    BackgroundExecutor.submit(new ExternalCall<Boolean>() {
      @Override
      public Boolean submit() {
        try {
          flowHelper.postAsyncMessage(flowHelper.createAsyncCommandMessage(Command.RENAME_DEVICE.getCommand() + " " + name));
          flowHelper.renameCurrentDevice(name);
          updateUIWithNewName(name, handler);
          return name.equals(flowHelper.getCurrentDevice().getName());
        } catch (FlowException e) {
          DebugLogger.log(getClass().getSimpleName(), e);
          ActivitiesAndFragmentsHelper.showToast(appContext,
              ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
              handler);
          return false;
        }
      }
    }, this, CHANGE_NAME_ID, appContext);
  }

  private void updateUIWithNewName(final String name, final Handler handler) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (activity != null) {
          activity.onTitleChange(name);
          ((FlowActivity) activity).setUIMode(NDMenuMode.Interactive);// to update name in drawer menu
          nameValue.setText(name);
        }
        progressBar.setVisibility(View.GONE);
      }
    });
  }

  @Override
  public void onBackgroundExecutionResult(final Boolean result, int taskCode) {
    if (taskCode == CHANGE_NAME_ID) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (result) {
            ActivitiesAndFragmentsHelper.showDeviceInfoChangeDialog(
                R.string.device_name_change_success_dialog_tilte,
                R.string.device_name_change_success_dialog_content,
                (FlowActivity) activity);
          } else {
            ActivitiesAndFragmentsHelper.showDeviceInfoChangeDialog(
                R.string.device_name_change_failure_dialog_title,
                R.string.device_name_change_failure_dialog_content,
                (FlowActivity) activity);
          }
        }
      });
    }
  }
}
