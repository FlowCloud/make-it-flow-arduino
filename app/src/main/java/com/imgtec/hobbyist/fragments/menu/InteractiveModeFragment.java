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
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.imgtec.flow.MessagingEvent;
import com.imgtec.flow.client.core.FlowException;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.activities.SearchUsersActivity;
import com.imgtec.hobbyist.adapters.CommandsAdapter;
import com.imgtec.hobbyist.flow.AsyncMessage;
import com.imgtec.hobbyist.flow.AsyncMessageListener;
import com.imgtec.hobbyist.flow.AsyncMessageNodeKeys;
import com.imgtec.hobbyist.flow.Command;
import com.imgtec.hobbyist.flow.DevicePresenceListener;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.fragments.menu.setupguide.SpannedAdapter;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.BroadcastReceiverWithRegistrationState;
import com.imgtec.hobbyist.utils.DateFormatter;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalRun;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;
import com.imgtec.hobbyist.utils.WifiUtil;
import com.imgtec.hobbyist.views.InstantAutoCompleteTextView;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Fragment used to interact with Board through Flow.
 * There are two modes: Commands and Messages.
 * Commands mode allows interacting with board by command messages.
 * Messages mode allows sending text messages to other users.
 *
 * NOTE: Messages feature is currently not available in the app. Logic is here, but ui is hidden.
 */
public class InteractiveModeFragment extends NDListeningFragment implements AsyncMessageListener, DevicePresenceListener {

  public static final String TAG = "InteractiveModeFragment";

  public static final String TX_MESSAGE = "<b>TX message:</b>";
  public static final String RX_MESSAGE = "<b>RX message:</b>";
  public static final String CMD_MESSAGE = "<b>Received message:</b>";
  private RadioGroup interactiveModeChoice;
  private TextView deviceName;
  private EditText messageText;
  private InstantAutoCompleteTextView commandEditText;
  private Button sendButton;
  private Button clearButton;
  private Button searchUsersButton;
  private ListView messagesListView;
  private List<Spanned> messageList = new CopyOnWriteArrayList<>();
  private SpannedAdapter messageListAdapter;
  private ConnectivityReceiver connectionReceiver;

  private boolean isCommandMode = true;

  private String recipientName = "";
  private String recipientAor = "";

  private FlowHelper flowHelper;
  private Handler handler = new Handler();

  public static InteractiveModeFragment newInstance() {
    return new InteractiveModeFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_interactive_mode, container, false);
    interactiveModeChoice = (RadioGroup) rootView.findViewById(R.id.interactiveModeChoice);
    deviceName = (TextView) rootView.findViewById(R.id.deviceName);
    searchUsersButton = (Button) rootView.findViewById(R.id.searchUsersButton);
    messageText = (EditText) rootView.findViewById(R.id.messageText);
    commandEditText = (InstantAutoCompleteTextView) rootView.findViewById(R.id.commandTextButton);
    sendButton = (Button) rootView.findViewById(R.id.sendCommandsButton);
    clearButton = (Button) rootView.findViewById(R.id.clearCommandsButton);
    messagesListView = (ListView) rootView.findViewById(R.id.messagesListView);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    activity.onSelectionAndTitleChange(NDMenuItem.InteractiveMode);
    initFlowHelper();
    initListAdapter();
    initUI();
    initInteractiveModeChangeListener();
    initButtonsListeners();
    initEditText();
  }

  @Override
  public void onResume() {
    super.onResume();
    flowHelper.addDevicePresenceListener(this);
    connectionReceiver = new ConnectivityReceiver(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    connectionReceiver.register(appContext);
  }

  @Override
  public void onPause() {
    flowHelper.removeDevicePresenceListener(this);
    connectionReceiver.unregister(appContext);
    super.onPause();
  }

  /**
   * ---------------------------------- Initialization functions -----------------------------------*
   */

  private void initFlowHelper() {
    flowHelper = FlowHelper.getInstance(getActivity());
    flowHelper.setAsyncMessageListener(this);
  }

  private void initListAdapter() {
    messageListAdapter = new SpannedAdapter(getActivity(), android.R.layout.simple_list_item_1, messageList);
    messagesListView.setAdapter(messageListAdapter);
  }

  public void initUI() {
    clearUI();
    setDeviceName();
    initCommandsUI();
    initMessagesUI();
  }

  /**
   * Makes UI raw and default.
   */
  private void setDeviceName() {
    deviceName.setText(flowHelper.getCurrentDevice().getName());
  }

  private void clearUI() {
    messagesListView.setVisibility(View.GONE);
    clearButton.setEnabled(false);
    messageList.clear();
    messageListAdapter.notifyDataSetChanged();
    commandEditText.setText("");
    messageText.setText("");
  }

  private void initCommandsUI() {
    if (isCommandMode) {
      commandEditText.setVisibility(View.VISIBLE);
      searchUsersButton.setVisibility(View.GONE);
      messageText.setVisibility(View.GONE);
    }
  }

  private void initMessagesUI() {
    if (!isCommandMode) {
      if (!recipientName.equals("")) {
        searchUsersButton.setText(recipientName);
        sendButton.setEnabled(true);
      }
      if (!isCommandMode) {
        interactiveModeChoice.check(R.id.messages);
      }
      commandEditText.setVisibility(View.GONE);
      searchUsersButton.setVisibility(View.VISIBLE);
      messageText.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Listener for changing this fragment state of current mode (Commands or Messages).
   */
  private void initInteractiveModeChangeListener() {
    interactiveModeChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
          case R.id.commands:
            isCommandMode = true;
            break;
          case R.id.messages:
            isCommandMode = false;
            break;
        }
        initUI();
      }
    });
  }

  private void initEditText() {
    final ArrayList<String> commands = (ArrayList<String>) Command.getInteractiveModeCommands();
    final ArrayAdapter adapter = new CommandsAdapter(getActivity(), android.R.layout.simple_list_item_1, commands);
    commandEditText.setAdapter(adapter);
    commandEditText.setThreshold(0);

    commandEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        commandEditText.setText((String) adapter.getItem(position));
        commandEditText.dismissDropDown();
        sendButton.setEnabled(true);
        ActivitiesAndFragmentsHelper.hideSoftInput(appContext, commandEditText);
      }
    });
  }

  private void initButtonsListeners() {
    /**
     * Click sends message written in {@link #messageText}.
     */
    sendButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showMessagesListAndEnableClearButton();
        final String commandString = commandEditText.getText().toString();
        if (isCommandMode && !commandString.equals("")) {
          sendCommandMessage(commandString);
          setCommandUIEnabled(false);
        } else if (!isCommandMode && !recipientName.equals("")) {
          ActivitiesAndFragmentsHelper.hideSoftInput(appContext, messageText);
          sendTextMessage();
        }
      }
    });

    /**
     * Click on this button clears list of messages, no matter if they come from Commands or Messages mode as well.
     */
    clearButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clearButton.setEnabled(false);
        messageListAdapter.clear();
        messageListAdapter.notifyDataSetChanged();
      }
    });

    /**
     * Start activity for the result of user's searching.
     */
    searchUsersButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent searchUsersIntent = new Intent(getActivity(), SearchUsersActivity.class);
        startActivityForResult(searchUsersIntent, SearchUsersActivity.REQUEST_CODE);
      }
    });
  }

  private void showMessagesListAndEnableClearButton() {
    messagesListView.setVisibility(View.VISIBLE);
    clearButton.setEnabled(true);
  }

  private void setCommandUIEnabled(boolean enabled) {
    sendButton.setEnabled(enabled);
    commandEditText.setEnabled(enabled);
  }

  /**---------------------------------- Start of command message functions -----------------------------------**/

  /**
   * Send AsyncMessage command to Flow.
   */
  private void sendCommandMessage(final String input) {
    BackgroundExecutor.execute(new ExternalRun() {
      @Override
      public void execute() {
        try {
          String command = Command.prepareCommand(input);
          AsyncMessage asyncMsg = flowHelper.createAsyncCommandMessage(command);
          boolean isAsyncMessageSent = flowHelper.postAsyncMessage(asyncMsg);
          if (isAsyncMessageSent) {
            showCommandTXMessage(input);
          } else {
            ActivitiesAndFragmentsHelper.showToast(appContext, R.string.message_send_other_problem, handler);
          }
        } catch (FlowException e) {
          DebugLogger.log(getClass().getSimpleName(), e);
          ActivitiesAndFragmentsHelper.showToast(appContext,
                  ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
                  handler);
        }
      }
    });
  }

  /**
   * Add message to messageList and update UI adhered to messageListAdapter.
   *
   * @param commandString previously sent command text.
   */
  private void showCommandTXMessage(String commandString) {
    messageList.add(0,  Html.fromHtml("<font color='#006400'>" + TX_MESSAGE + "<br/>" +
                        DateFormatter.now(appContext) + "<br/>" +
                        TextUtils.htmlEncode(commandString) + "<br/></font>"));
    removeMessageIfListIsTooLong();
    notifyMessageListAdapter();
  }

  /**
   * Update messageListAdapter on UI thread. Necessary when updated from another thread.
   */
  private void notifyMessageListAdapter() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        messageListAdapter.notifyDataSetChanged();
      }
    });
  }

  /**
   * Shows Flow's response to sent async command.
   */
  @Override
  public void onAsyncMessageResponse(MessagingEvent.AsyncMessageResponse response) {
    String responseText;
    if (getActivity() != null) {
      switch (response) {
        case SEND_SUCCESS:
          responseText = getString(R.string.response_success);
          break;
        case SEND_BUFFER_FULL:
          responseText = getString(R.string.response_buffer_full);
          break;
        case SENT_BUT_NOT_DELIVERED:
          responseText = getString(R.string.response_offline);
          break;
        case SEND_FAILED:
          responseText = getString(R.string.response_failed);
          break;
        default:
          responseText = "";
          break;
      }
      messageList.add(0, Html.fromHtml("<font color='fuchsia'><b>Response:</b> " + TextUtils.htmlEncode(responseText) +  "</font>"));
      removeMessageIfListIsTooLong();
      notifyMessageListAdapter();
      setCommandUIEnabled(true);
    }
  }

  /**
   * Asynchronously shows board's response to command.
   * If command was REBOOT or REBOOT SOFTAP, it will also show a dialog
   * forcing user to go back to connected devices screen.
   *
   * @param msg AsyncMessage object which can be parsed to get additional data.
   */
  @Override
  public void onCommandRXMessageReceived(final AsyncMessage msg) {
    showCommandRXMessage(msg);
  }

  /**
   * Asynchronously shows command message incoming from a board.
   *
   * @param msg
   */
  @Override
  public void onCommandMessageReceived(AsyncMessage msg) {
    showMessagesListAndEnableClearButton();
    showCommandMessage(msg);
  }

  /**
   * Add message to messageList and update UI adhered to messageListAdapter.
   *
   * @param msg AsyncMessage object which can be parsed to get additional data.
   */
  private void showCommandRXMessage(AsyncMessage msg) {
    String html = RX_MESSAGE + "<br/>" +
            DateFormatter.now(appContext) + "<br/>" +
            Html.fromHtml(commandEditText.getText().toString()).toString() + " &#8594; " +
            Html.fromHtml(msg.getNode(AsyncMessageNodeKeys.RESPONSE_CODE)).toString() + "<br/>";
    String params = msg.getNode(AsyncMessageNodeKeys.RESPONSE_PARAMS);
    if (params != null && !params.equals("")) {
      try {
        html += "</font><font color='#4169E1'>" + formatXML(params) + "</font>";
      } catch (TransformerException e) {
        e.printStackTrace();
        Log.e("InteractiveModeFragment.showCommandRXMessage", "Failed to format response parameters from command", e);
        html += "</font><font color='red'>Error formatting response \"" + params + "\"</font>";
      }
    } else {
      html += "</font><font color='black'>No response parameters</font>";
    }
    messageList.add(0, Html.fromHtml("<font color='blue'>" + html + "</font>"));
    removeMessageIfListIsTooLong();
    notifyMessageListAdapter();
  }

  private String formatXML(String input) throws TransformerException {
    // adapted from http://stackoverflow.com/a/139096

    // add on temporary tags to ensure correct parsing
    // this is necessary as we are pretty-printing <i>part</i>
    // of a XML document, so we allow a simple string (content)
    // and allow multiple children at the "root" level
    input = "<t>" + input + "</t>";

    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    StreamResult result = new StreamResult(new StringWriter());
    StreamSource source = new StreamSource(new StringReader(input));
    transformer.transform(source, result);
    String xmlString = result.getWriter().toString();

    // remove temporary tags and newlines at beginning and end
    xmlString = xmlString.substring(3, xmlString.length()-5).replaceAll("(?m)^\\s*\r?\n", "");
    if (xmlString.endsWith("\n")) xmlString = xmlString.substring(0, xmlString.length()-2);

    // if this is a one-line string then add a tab to the beginning
    if (!xmlString.contains("\n")) xmlString = "\t" + xmlString;

    return TextUtils.htmlEncode(xmlString).replace("\n", "<br>").replace("\t", "    ").replace(" ", "&nbsp;");
  }

    private void showCommandMessage(AsyncMessage msg) {
    messageList.add(0, Html.fromHtml(   CMD_MESSAGE + DateFormatter.now(appContext) + "<br/>" +
                                        Html.fromHtml(msg.getNode(AsyncMessageNodeKeys.DETAILS)).toString()));
    removeMessageIfListIsTooLong();
    notifyMessageListAdapter();
  }

  private void removeMessageIfListIsTooLong() {
    if (messageList.size() > 100) {
      messageList.remove(messageList.size() - 1);
    }
  }

  /**---------------------------------- Start of text message functions -----------------------------------**/

  /**
   * Shows text messages which have sent before.
   * Shows chosen user's information.
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == SearchUsersActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      recipientName = data.getStringExtra(SearchUsersActivity.FLOW_USER_NAME);
      recipientAor = data.getStringExtra(SearchUsersActivity.FLOW_USER_AOR);
      initMessagesUI();
      BackgroundExecutor.execute(new ExternalRun() {
        @Override
        public void execute() {
          try {
            showTextMessagesFromHourAgoOn();
          } catch (FlowException e) {
            DebugLogger.log(getClass().getSimpleName(), e);
            ActivitiesAndFragmentsHelper.showToast(appContext,
                ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
                handler);
          }
        }
      });
    }
  }

  /**
   * Add messages to messageList and update UI adhered to messageListAdapter.
   */
  private void showTextMessagesFromHourAgoOn() {
    Calendar hourAgo = Calendar.getInstance(Locale.UK);
    hourAgo.setTimeInMillis(new Date().getTime() - 1000 * 60 * 60);
    String query = "@from == '" + recipientName + "' AND @STime > '" + DateFormatter.fromCalendar(hourAgo) + "'";
    List<AsyncMessage> messages = flowHelper.queryTextMessages(query);
    for (AsyncMessage msg : messages) {
      addTextMessage(msg);
    }
    notifyMessageListAdapter();
  }

  /**
   * {@link #notifyMessageListAdapter()} after is necessary to show results on UI.
   *
   * @param msg AsyncMessage object which can be parsed to get additional data.
   */
  private void addTextMessage(AsyncMessage msg) {
    messageList.add(0, Html.fromHtml(DateFormatter.formatForDisplay(msg.getNode(AsyncMessageNodeKeys.SENT_WITH_TYPE_INFO), appContext) + "<br/>" +
                            msg.getNode(AsyncMessageNodeKeys.FROM) + ":<br/>" +
                            Html.fromHtml(msg.getNode(AsyncMessageNodeKeys.MESSAGE)).toString()
            )
    );
  }

  /**
   * Send AsyncMessage text to Flow.
   */
  private void sendTextMessage() {
    BackgroundExecutor.execute(new ExternalRun() {
      @Override
      public void execute() {
        try {
          AsyncMessage asyncMsg = createAsyncTextMessage();
          boolean isAsyncMessageSent = flowHelper.postAsyncMessage(asyncMsg);
          if (isAsyncMessageSent) {
            addTextMessage(asyncMsg);
            notifyMessageListAdapter();
          } else {
            ActivitiesAndFragmentsHelper.showToast(appContext, R.string.message_send_other_problem, handler);
          }
        } catch (FlowException e) {
          DebugLogger.log(getClass().getSimpleName(), e);
          ActivitiesAndFragmentsHelper.showToast(appContext,
              ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
              handler);
        }
      }

      private AsyncMessage createAsyncTextMessage() {
        final String messageString = messageText.getText().toString();
        AsyncMessage asyncMsg = AsyncMessage.newInstance(AsyncMessage.MessageType.MESSAGE, null);
        asyncMsg.addNode(AsyncMessageNodeKeys.MESSAGE, messageString);
        String dateString = DateFormatter.fromCalendar(GregorianCalendar.getInstance());
        asyncMsg.addNode(AsyncMessageNodeKeys.SENT_WITH_TYPE_INFO, dateString);
        asyncMsg.addNode(AsyncMessageNodeKeys.TO, recipientAor);
        asyncMsg.addNode(AsyncMessageNodeKeys.FROM, flowHelper.getUserName());
        return asyncMsg;
      }
    });
  }

  /**
   * Asynchronously shows text message response from Flow.
   *
   * @param msg AsyncMessage object which can be parsed to get additional data.
   */
  @Override
  public void onTextMessageReceived(final AsyncMessage msg) {
    addTextMessage(msg);
    notifyMessageListAdapter();
  }

  /**
   * --------------------------------- Dialog functions ----------------------------------------*
   */

  @Override
  public void onDevicePresenceChangeListener(boolean isConnected) {
    if (!isConnected) {
      showConnectedDevicesFragmentDialog(SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
    }
  }

  private void showConnectedDevicesFragmentDialog(Fragment fragment) {
      ActivitiesAndFragmentsHelper.showFragmentChangeDialog(
          R.string.device_is_offline,
          R.string.back_to_connected_devices,
          (FlowActivity) activity,
          fragment);
  }

  public class ConnectivityReceiver extends BroadcastReceiverWithRegistrationState {

    public ConnectivityReceiver(IntentFilter intentFilter) {
      super(intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      connectionReceiver.unregister(appContext);
      if (!new WifiUtil(appContext).isInternetConnected()) {
        ActivitiesAndFragmentsHelper.showFragmentChangeDialog(
            R.string.connectivity_problems,
            R.string.back_to_connected_devices,
            (FlowActivity) activity,
            SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
      }
    }
  }
}
