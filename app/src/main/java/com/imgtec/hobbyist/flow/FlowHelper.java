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

package com.imgtec.hobbyist.flow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.imgtec.flow.FlowHandler;
import com.imgtec.flow.client.core.API;
import com.imgtec.flow.client.core.Client;
import com.imgtec.flow.client.core.Core;
import com.imgtec.flow.client.core.InvalidCredentialsException;
import com.imgtec.flow.client.core.Setting;
import com.imgtec.flow.client.users.Contact;
import com.imgtec.flow.client.users.Contacts;
import com.imgtec.flow.client.users.DataStore;
import com.imgtec.flow.client.users.DataStoreItem;
import com.imgtec.flow.client.users.DataStoreItems;
import com.imgtec.flow.client.users.Device;
import com.imgtec.flow.client.users.DeviceHelper;
import com.imgtec.flow.client.users.DeviceRegistrationKey;
import com.imgtec.flow.client.users.Devices;
import com.imgtec.flow.client.users.User;
import com.imgtec.flow.client.users.UserHelper;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.DateFormatter;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.Preferences;
import com.imgtec.hobbyist.utils.WifiUtil;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Class encapsulating all communication with Flow.
 * Provides methods allowing to perform all Flow requests used in the app.
 */
public class FlowHelper {

  private static volatile FlowHelper instance;

  private static final String LOG_TAG = FlowHelper.class.toString();

  private static boolean flowInitialized = false; //Is automatically set to false after app process is being killed
  private static boolean licenseCorrect = false;
  private FlowEntities flowEntities;
  private SharedPreferences sharedPreferences;
  private Context appContext;
  private Handler handler = new Handler();

  public static FlowHelper getInstance(Activity activity) {
    if (instance == null) {
      synchronized (FlowHelper.class) {
        if (instance == null) {
          instance = new FlowHelper(activity);
        }
      }
    }
    return instance;
  }

  /**
   * HACK! BE CAREFUL!
   * Use this function only one time per activity which indicates logged in to Flow state
   * in it's onCreate(), onStart() or onResume().
   * No more, because you can cause multiple application restarts, e.g. if you
   * use it in fragment.
   */
  public static FlowHelper getInstanceAndRestartAppIfRequired(Activity activity) {
    restartAppIfFlowIsNOTInitialized(activity);
    return getInstance(activity);
  }

  private FlowHelper(Activity activity) {
    flowEntities = FlowEntities.getInstance(activity);
    appContext = activity.getApplicationContext();
    sharedPreferences = appContext.getSharedPreferences(Preferences.SETTINGS, Context.MODE_PRIVATE);
  }

  public static boolean isFlowInitialized() {
    return flowInitialized;
  }

  public static boolean isLicenseCorrect() {
    return licenseCorrect;
  }

  /**
   * CAREFUL!
   * Restart application if Flow is not initialized.
   * USE ONLY when we are in place in the app where it is a must to be initialized, because in other case
   * we can hang the app.
   */
  private static boolean restartAppIfFlowIsNOTInitialized(Activity activity) {
    WifiUtil wifiUtil = new WifiUtil(activity);
    if (!flowInitialized && wifiUtil.isInternetNotBoardConnected()) {
      ActivitiesAndFragmentsHelper.restartApplication(activity, new Handler(Looper.getMainLooper()));
      return true;
    }
    return false;
  }

  public static boolean isUserLoggedInToFlow() {
    return instance != null && Core.getDefaultClient().isUserLoggedIn();
  }

  /**
   * Initializes flow if not initialized. If init succeeds, it attempts to set URL addresses
   * for links like Online help or Forgot password, basing on license.
   * @return whether initialization was successful
   */
  public boolean initFlowIfNotInitialized(Activity activity) {
    boolean result = false;
    if (!flowInitialized) {
      result = flowInit();
      if (result) {
        flowInitialized = true;
        readUrlsForLicensee();
      } else {
        DebugLogger.log(getClass().getSimpleName(), activity.getApplicationContext().getString(R.string.connectivity_problems));
        ActivitiesAndFragmentsHelper.showToastAndFinishActivity(
            activity,
            R.string.connectivity_problems,
            handler);
      }
    }
    return result;
  }

  public boolean flowInit() {
    return flowEntities.getFlowInstance().init(
        flowEntities.getInitXml(
            sharedPreferences.getString(Preferences.ROOT_URL, Preferences.ROOT_URL_DEFAULT_VALUE),
            sharedPreferences.getString(Preferences.OAUTH_KEY, Preferences.OAUTH_KEY_DEFAULT_VALUE),
            sharedPreferences.getString(Preferences.OAUTH_SECRET, Preferences.OAUTH_SECRET_DEFAULT_VALUE))
    );
  }

  public void shutdown() { //operation opposite to initialization
    if (flowEntities.getFlowInstance().shutdown()) {
      flowInitialized = false;
    }
  }

  public void readUrlsForLicensee() {
    API clientAPI = Core.getDefaultClient().getAPI();
    licenseCorrect = clientAPI.hasSettings();
    if (licenseCorrect) {
      for (Setting setting : clientAPI.getSettings()) {
        switch (setting.getKey()) {
          case "MakeItFlow.OnlineHelpUrl":
            Constants.FLOW_ONLINE_HELP_URL = setting.getValue();
            break;
          case "MakeItFlow.TroubleshootingUrl":
            Constants.FLOW_TROUBLESHOOTING_URL = setting.getValue();
            break;
          case "MakeItFlow.ForgotPasswordUrl":
            Constants.FLOW_FORGOT_PASSWORD_URL = setting.getValue();
            break;
          case "MakeItFlow.ForumUrl":
            Constants.FLOW_FORUM_URL = setting.getValue();
            break;
          case "MakeItFlow.UsingBoardUrl":
            Constants.FLOW_USING_BOARD_URL = setting.getValue();
            break;
          default:
            break;
        }
      }
    }
  }

  public boolean userLoginToFlow(String name, String password) {
    if (!isUserLoggedInToFlow()) {
      flowEntities.setCurrentCredentials(name, password);
      User user = UserHelper.newUser(Core.getDefaultClient());
      boolean loggedIn = flowEntities.getFlowInstance().userLogin(name, password, user, flowEntities.getUserFlowHandler());
      if (loggedIn) {
        flowEntities.subscribeASyncMessaging();
      }
      return loggedIn;
    }
    return true;
  }

  /**
   * Log user back in using credentials used earlier in this session. Used to renew login.
   *
   * @return
   */
  public boolean userLogBackInToFlow() {
    return userLoginToFlow(flowEntities.getCurrentUsername(), flowEntities.getCurrentPassword());
  }

  /**
   * Registers new user.
   * Username and password must have minimum 5 characters.
   * Contains networking.
   *
   * @param email
   * @param password
   * @return true if registered, false if not
   */
  public boolean registerNewUser(String username, String email, String password) {
    Client client = Core.getDefaultClient();
    return UserHelper.createUser(client, username, email, password, true, false, Locale.getDefault().toString());
  }

  /**
   * Logs currently logged in user out.
   */
  public void userLogOut() {
    logOut(flowEntities.getUserFlowHandler());
    clearDevicesData();
  }

  /**
   * Log out specified entity (user or device). Contains networking.
   *
   * @param handler FlowHandler to entity to log out
   */
  private void logOut(FlowHandler handler) {
    flowEntities.getFlowInstance().logOut(handler);
  }

  /**
   * Clears all local data about users devices. Should be called after user log out.
   */
  public void clearDevicesData() {
    flowEntities.clearDevicesCache();
  }

  /**
   * Gets logged in user's AoR. Contains networking.
   *
   * @return AoR
   */
  public String getUserAor() {
    User user = Core.getDefaultClient().getLoggedInUser();
    return user.getFlowMessagingAddress().getAddress();
  }

  /**
   * Gets logged in user's name. Contains networking.
   *
   * @return name
   */
  public String getUserName() {
    User user = Core.getDefaultClient().getLoggedInUser();
    return user.getUserName();
  }

  /**
   * Gets logged in user's client id. Contains networking.
   *
   * @return client id
   */
  public String getUserId() {
    User user = Core.getDefaultClient().getLoggedInUser();
    return user.getUserID();
  }

  /**
   * Returns a map of usernames and their AoRs. Contains networking.
   *
   * @return map of users <username, aor>
   */
  public Map<String, String> getUsersMap() {
    User user = Core.getDefaultClient().getLoggedInUser();
    Contacts contacts = user.getContacts();
    Map<String, String> userNames = new HashMap<>();
    for (Contact contact : contacts) {
      userNames.put(contact.getDisplayName(), contact.getFlowMessagingAddress().getAddress());
    }
    return userNames;
  }

  /**
   * Registration key getter. Contains networking.
   *
   * @return first registration key which meet "Free" status requirement
   */
  public String getFirstFreeRegistrationKey() {
    String result = "";
    User user = Core.getDefaultClient().getLoggedInUser();
    if (user != null && user.hasDeviceRegistrationKeys()) {
      for (DeviceRegistrationKey key : user.getDeviceRegistrationKeys()) {
        if ((key != null) && key.hasStatus() && key.hasRegistrationKey() && key.getStatus().equalsIgnoreCase(Constants.FREE)) {
          result = key.getRegistrationKey();
          break;
        }
      }
    } else {
      DebugLogger.log(LOG_TAG, appContext.getString(R.string.registration_keys_error));
      //this means that either he has no limit on devices registered (no keys needed then) or all keys are used
    }
    return result;
  }

  /**
   * Contains networking.
   *
   * @return list of pairs of "Claimed" keys and MACs for currently registered devices.
   */
  public List<Pair<String, String>> getClaimedKeyMACPair() {
    List<Pair<String, String>> result = new ArrayList<>();
    User user = Core.getDefaultClient().getLoggedInUser();
    if (user != null && user.hasDeviceRegistrationKeys()) {
      for (DeviceRegistrationKey key : user.getDeviceRegistrationKeys()) {
        if ((key != null) && key.hasStatus() && key.hasRegistrationKey() && key.getStatus().equalsIgnoreCase(Constants.CLAIMED)) {
          // create Pair
          Device linkedDevice;
          try {
            linkedDevice = key.getDevice();
            if (linkedDevice != null) {
              result.add(new Pair<>(key.getRegistrationKey(), linkedDevice.getMACAddress())); //order is important
            }
          } catch (InvalidCredentialsException exception) { // Thrown after inappropriate device deletion on the server. It shouldn't happen, but happened once.
            DebugLogger.log(LOG_TAG, appContext.getString(R.string.error_there_is) + " \"" + Constants.CLAIMED + "\" " + appContext.getString(R.string.key_without_device));
          }
        }
      }
    } else {
      DebugLogger.log(LOG_TAG, appContext.getString(R.string.registration_keys_error));
    }
    return result;
  }

  /**
   * Sends specified message to the entity specified in that message in <to> tag. Contains networking.
   *
   * @param msg asynchronous message with correct format and nodes
   */
  public boolean postAsyncMessage(AsyncMessage msg) {
    String recipient = msg.getNode("to");
    if (recipient == null || "".equals(recipient)) {
      return false;
    }
    return flowEntities.getFlowInstance().sendAsyncMessage(flowEntities.getUserFlowHandler(),
        new String[]{recipient}, msg.buildXml());
  }

  /**
   * Queries user's data store for messages. Contains networking.
   *
   * @param query query by which items will be retrieved from data store
   */
  public List<AsyncMessage> queryTextMessages(String query) {
    List<AsyncMessage> messages = new ArrayList<>();
    User user = Core.getDefaultClient().getLoggedInUser();
    DataStore ds = user.getDataStore("msgHistory");
    DataStoreItems items = ds.getItemsByQuery(query);
    for (DataStoreItem item : items) {
      AsyncMessage message = AsyncMessage.newInstance(AsyncMessage.MessageType.MESSAGE, null);
      message.setNodesFromXml(item.getContent());
      messages.add(message);
    }
    return messages;
  }

  /**
   * Renames currently selected device. Invalidates cache. Contains networking.
   *
   * @param name
   */
  public void renameCurrentDevice(String name) {
    Device newDetails = DeviceHelper.newDevice(Core.getDefaultClient());
    newDetails.setDeviceName(name);
    getCurrentDevice().getDevice().update(newDetails);
    String id = getCurrentDevice().getDevice().getDeviceID();
    clearDevicesData();
    WifireDevice currentDevice = new WifireDevice(Core.getDefaultClient().getAPI().getDevice(id));
    currentDevice.setNetworkState(true); //to retain online status in ui
    setCurrentDevice(currentDevice);
  }

  /**
   * Clears configuration from device and removes it from Flow. Contains networking.
   *
   * @param device
   */
  public void removeDevice(WifireDevice device) {
    postAsyncMessage(createAsyncCommandMessage(Command.FACTORY_RESET.getCommand()));
    postAsyncMessage(createAsyncCommandMessage(Command.REBOOT.getCommand()));
    Devices ownedDevices = Core.getDefaultClient().getLoggedInUser().getOwnedDevices();
    for (Device ownedDevice : ownedDevices) {
      if (ownedDevice.getMACAddress().equals(device.getDevice().getMACAddress())) {
        ownedDevice.remove();
      }
    }
  }

  /**
   * Creates AsyncMessage command, based on string, which should contain details of command
   *
   * @param commandString
   * @return
   */
  public AsyncMessage createAsyncCommandMessage(String commandString) {
    AsyncMessage asyncMsg = AsyncMessage.newInstance(AsyncMessage.MessageType.COMMAND, null);
    asyncMsg.addNode(AsyncMessageNodeKeys.SENT_WITH_TYPE_INFO, DateFormatter.fromCalendar(GregorianCalendar.getInstance()));
    asyncMsg.addNode(AsyncMessageNodeKeys.TO, getCurrentDevice().getAor());
    asyncMsg.addNode(AsyncMessageNodeKeys.FROM, getUserAor());
    asyncMsg.addNode(AsyncMessageNodeKeys.CLIENTID, getUserId());
    asyncMsg.addNode(AsyncMessageNodeKeys.REQUESTID, asyncMsg.getRequestId());
    asyncMsg.addNode(AsyncMessageNodeKeys.DETAILS, commandString);
    return asyncMsg;
  }

  /**
   * Gets devices list from Flow. Contains networking.
   *
   * @return list of user's devices
   */
  public List<WifireDevice> getWifireDeviceList() {
    return flowEntities.requestWifireDevices();
  }

  public void subscribeAllDevices() {
    flowEntities.subscribeAllDevicesPresence();
  }

  public void unsubscribeAllDevices() {
    flowEntities.unsubscribeAllDevicesPresence();
  }

  public WifireDevice getCurrentDevice() {
    return flowEntities.getCurrentDevice();
  }

  public void setCurrentDevice(WifireDevice currentDevice) {
    flowEntities.setCurrentDevice(currentDevice);
  }

  public void setAsyncMessageListener(AsyncMessageListener listener) {
    flowEntities.setAsyncMessageListener(listener);
  }

  public void addDevicePresenceListener(DevicePresenceListener listener) {
    flowEntities.addDevicePresenceListener(listener);
  }

  public void removeDevicePresenceListener(DevicePresenceListener listener) {
    flowEntities.removeDevicePresenceListener(listener);
  }

  public void addFlowConnectedListener(DeviceToFlowConnectionListener listener) {
    flowEntities.addDeviceToFlowConnectionListener(listener);
  }

  public void removeFlowConnectedListener(DeviceToFlowConnectionListener listener) {
    flowEntities.removeDeviceToFlowConnectionListener(listener);
  }

  public void addDeviceOnlineInFlowListener(DeviceOnlineInFlowListener listener) {
    flowEntities.addDeviceOnlineInFlowListener(listener);
  }

  public void removeDeviceOnlineInFlowListener(DeviceOnlineInFlowListener listener) {
    flowEntities.removeDeviceOnlineInFlowListener(listener);
  }
}
