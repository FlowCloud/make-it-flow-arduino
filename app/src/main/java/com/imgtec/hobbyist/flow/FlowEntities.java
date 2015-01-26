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

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.imgtec.flow.Flow;
import com.imgtec.flow.FlowHandler;
import com.imgtec.flow.MessagingEvent;
import com.imgtec.flow.client.core.Core;
import com.imgtec.flow.client.extdep.cache.FlowCache;
import com.imgtec.flow.client.users.Device;
import com.imgtec.flow.client.users.Devices;
import com.imgtec.flow.client.users.User;
import com.imgtec.hobbyist.utils.DebugLogger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class that contains entities that are used to communicate with Flow and do not change after
 * first initialization.
 */
public class FlowEntities {

  private static volatile FlowEntities instance;

  private Flow flowInstance;
  private FlowHandler userFlowHandler;
  private List<WifireDevice> cachedDevices = new CopyOnWriteArrayList<>();
  private String username;
  private String password;

  private Handler devicePresenceHandler;
  private Handler userAsyncHandler;
  private Handler asyncResponseHandler;
  private WifireDevice currentDevice;

  /**
   * Listeners which inform appropriate parts of app UI about changes in flow.
   */
  private AsyncMessageListener asyncMessageListener;
  private List<DevicePresenceListener> devicePresenceListeners = new ArrayList<>();
  private List<DeviceToFlowConnectionListener> deviceToFlowConnectionListeners = new ArrayList<>();
  private List<DeviceOnlineInFlowListener> deviceOnlineInFlowListeners = new ArrayList<>();

  private FlowEntities(Context appContext) {
    flowInstance = Flow.getInstance();
    flowInstance.setAppContext(appContext);
    userFlowHandler = new FlowHandler();
    initUserAsyncHandler();
    initAsyncResponseHandler();
    initDevicePresenceHandler();
  }

  private void initUserAsyncHandler() {
    userAsyncHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        String content = ((MessagingEvent) msg.obj).content;
        final AsyncMessage asyncMsg = AsyncMessage.newInstance(AsyncMessage.MessageType.RESPONSE, null);
        asyncMsg.setNodesFromXml(content);
        if (asyncMessageListener != null) {
          switch (asyncMsg.getType()) {
            case MESSAGE:
              asyncMessageListener.onTextMessageReceived(asyncMsg);
              break;
            case COMMAND:
              asyncMessageListener.onCommandMessageReceived(asyncMsg);
              break;
            case RESPONSE:
              new Thread(new Runnable() {
                public void run() {
                  User user = Core.getDefaultClient().getLoggedInUser();
                  if (user.getUserID().equals(asyncMsg.getNode("requestclientid")) && asyncMessageListener != null) {
                    asyncMessageListener.onCommandRXMessageReceived(asyncMsg);
                  }
                }
              }).start();
              break;
            default:
              DebugLogger.log("Async message handler", "Unidentified message type");
              break;
          }
        }
      }
    };
  }

  private void initAsyncResponseHandler() {
    asyncResponseHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (asyncMessageListener != null) {
          asyncMessageListener.onAsyncMessageResponse(((MessagingEvent) msg.obj).messageResponse);
        }
      }
    };
  }

  private void initDevicePresenceHandler() {
    devicePresenceHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        MessagingEvent event = ((MessagingEvent) msg.obj);
        String content = ((MessagingEvent) msg.obj).content;
        WifireDevice senderDevice = getDeviceByAor(event.sender);
        if (senderDevice == null) {
          return; //unknown device - ignore. Should never happen
        }
        if (content == null) { //device offline
          final boolean newNetworkState = false;
          updateNetworkStateIfChanged(senderDevice, newNetworkState);
        } else {
          XmlPullParserFactory factory;
          XmlPullParser xpp;
          boolean insideBoardHealthTuple = false;
          try {
            factory = XmlPullParserFactory.newInstance();
            xpp = factory.newPullParser();
            xpp.setInput(new StringReader(content));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
              if (eventType == XmlPullParser.START_TAG) {
                switch (xpp.getName()) {
                  case "wifire_starterapp:ssid":
                    senderDevice.setNetworkSSID(xpp.nextText());
                    break;
                  case "wifire_starterapp:state":
                    final boolean isConnected = xpp.nextText().equals("connected");
                    for (DeviceToFlowConnectionListener listener : deviceToFlowConnectionListeners) {
                      if (listener != null) {
                        listener.onDeviceConnectedToFlow(senderDevice);
                      }
                    }
                    if (isConnected) {
                      for (DeviceOnlineInFlowListener listener : deviceOnlineInFlowListeners) {
                        if (listener != null) {
                          listener.onDeviceOnlineInFlow();
                        }
                      }
                    }
                    updateNetworkStateIfChanged(senderDevice, isConnected);
                    break;
                  case "wifire_starterapp:rssi_dbm":
                    senderDevice.setNetworkRSSIdBm(xpp.nextText());
                    break;
                  case "wifire_starterapp:mrf24w-status":
                    insideBoardHealthTuple = true;
                    break;
                  case "wifire_starterapp:wifire-status":
                    insideBoardHealthTuple = false;
                    break;
                  case "wifire_starterapp:status":
                    if (insideBoardHealthTuple) {
                      senderDevice.setBoardHealth(xpp.nextText());
                    } else {
                      senderDevice.setStatus(xpp.nextText());
                    }
                    break;
                  case "wifire_starterapp:uptime":
                    senderDevice.setUptime(xpp.nextText());
                    break;
                }
              }
              eventType = xpp.next();
            }
          } catch (XmlPullParserException | IOException e) {
            DebugLogger.log(getClass().getSimpleName(), "parsing device presence xml failed");
          }
        }
      }

      private void updateNetworkStateIfChanged(WifireDevice senderDevice, boolean isConnected) {
        if (senderDevice.isNetworkConnected() != isConnected) {
          senderDevice.setNetworkState(!senderDevice.isNetworkConnected());
          for (DevicePresenceListener listener : devicePresenceListeners) {
            if (listener != null) {
              listener.onDevicePresenceChangeListener(isConnected);
            }
          }
        }
      }
    };
  }

  /**
   * @return type of last error that occurred in communication with Flow.
   */
  public com.imgtec.flow.ErrorType getLastError() {
    return flowInstance.getLastError();
  }

  private WifireDevice getDeviceByAor(String aor) {
    for (WifireDevice device : cachedDevices) {
      if (device.getAor().equals(aor)) {
        return device;
      }
    }
    return null;
  }

  public static FlowEntities getInstance(Context context) {
    if (instance == null) {
      synchronized (FlowEntities.class) {
        if (instance == null) {
          instance = new FlowEntities(context.getApplicationContext());
        }
      }
    }
    return instance;
  }

  public Flow getFlowInstance() {
    return flowInstance;
  }

  public void setAsyncMessageListener(AsyncMessageListener listener) {
    asyncMessageListener = listener;
  }

  public void addDevicePresenceListener(DevicePresenceListener listener) {
    devicePresenceListeners.add(listener);
  }

  public void removeDevicePresenceListener(DevicePresenceListener listener) {
    devicePresenceListeners.remove(listener);
  }

  public void addDeviceToFlowConnectionListener(DeviceToFlowConnectionListener listener) {
    deviceToFlowConnectionListeners.add(listener);
  }

  public void removeDeviceToFlowConnectionListener(DeviceToFlowConnectionListener listener) {
    deviceToFlowConnectionListeners.remove(listener);
  }

  public void addDeviceOnlineInFlowListener(DeviceOnlineInFlowListener listener) {
    deviceOnlineInFlowListeners.add(listener);
  }

  public void removeDeviceOnlineInFlowListener(DeviceOnlineInFlowListener listener) {
    deviceOnlineInFlowListeners.remove(listener);
  }

  public void setCurrentCredentials(String username, String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * @return username (email) of currently logged in user.
   */
  public String getCurrentUsername() {
    return username;
  }

  /**
   * @return password of currently logged in user.
   */
  public String getCurrentPassword() {
    return password;
  }

  public String getInitXml(String oRoot, String oKey, String oSecret) {
    return "<?xml version=\"1.0\"?>" +
        "<Settings>" +
        "<Setting>" +
        "<Name>restApiRoot</Name>" +
        "<Value>" + oRoot + "</Value>" +
        "</Setting>" +
        "<Setting>" +
        "<Name>licenseeKey</Name>" +
        "<Value>" + oKey + "</Value>" +
        "</Setting>" +
        "<Setting>" +
        "<Name>licenseeSecret</Name>" +
        "<Value>" + oSecret + "</Value>" +
        "</Setting>" +
        "<Setting>" +
        "<Name>configDirectory</Name>" +
        "<Value>/mnt/img_messagingtest/outlinux/bin/config</Value>" +
        "</Setting>" +
        "</Settings>";
  }

  /**
   * Returns a list of currently cached devices. No networking.
   *
   * @return list of cached devices
   */
  public List<WifireDevice> getCachedDevices() {
    return cachedDevices;
  }

  /**
   * Gets list of currently connected devices and subscribes them.
   * Contains networking.
   */
  public List<WifireDevice> requestWifireDevices() {
    List<WifireDevice> ownedDevices;
    ownedDevices = WifireDevice.devicesAsWifireDeviceList(getOwnedDevices());
    refreshDevicesCache(ownedDevices);
    subscribeAllDevicesPresence();
    return cachedDevices;
  }

  private Devices getOwnedDevices() {
    FlowCache.clear();
    User user = Core.getDefaultClient().getLoggedInUser();
    return user.getOwnedDevices();
  }

  private void refreshDevicesCache(List<WifireDevice> ownedDevices) {
    for (WifireDevice deviceInCache : cachedDevices) {
      if (!ownedDevices.contains(deviceInCache)) {
        cachedDevices.remove(deviceInCache);
      }
    }
    for (WifireDevice device : ownedDevices) {
      if (!cachedDevices.contains(device)) {
        addSortedDevices(device);
      }
    }
  }

  private void addSortedDevices(WifireDevice device) {
    int insertAt = 0;
    if (cachedDevices.isEmpty()) {
      cachedDevices.add(0, device);
    } else {
      for (int i = 0; i < cachedDevices.size(); ++i) {
        if (cachedDevices.get(i).getName().compareTo(device.getName()) >= 0) {
          //alarm in list is later or equal than the new one - push it down
          insertAt = i;
          break;
        }
        if (i == cachedDevices.size() - 1) {
          insertAt = i + 1;
        }
      }
      cachedDevices.add(insertAt, device);
    }
  }

  /**
   * Subscribe all devices connected to Flow account.
   * Contains networking.
   */
  public void subscribeAllDevicesPresence() {
    for (WifireDevice device : cachedDevices) {
      subscribeDevicePresence(device.getDevice());
    }
  }

  /**
   * Subscribe device-presence
   * Contains networking.
   */
  public void subscribeDevicePresence(Device device) {
    String aor = device.getFlowMessagingAddress().getAddress();
    flowInstance.subscribe(getUserFlowHandler(), aor,
        MessagingEvent.MessagingEventCategory.FLOW_MESSAGING_EVENTCATEGORY_DEVICE_PRESENCE,
        "", 1200, devicePresenceHandler);
  }

  public FlowHandler getUserFlowHandler() {
    return userFlowHandler;
  }

  /**
   * Unsubscribe all devices connected to Flow account.
   * Contains networking.
   */
  public Devices unsubscribeAllDevicesPresence() {
    Devices userDevices = getOwnedDevices();
    for (Device device : userDevices) {
      unsubscribeDevicePresence(device);
    }
    return userDevices;
  }

  /**
   * Unsubscribe device-presence
   * Contains networking.
   */
  public boolean unsubscribeDevicePresence(Device device) {
    String aor = device.getFlowMessagingAddress().getAddress();
    return flowInstance.unsubscribe(getUserFlowHandler(), aor,
        MessagingEvent.MessagingEventCategory.FLOW_MESSAGING_EVENTCATEGORY_DEVICE_PRESENCE);
  }

  /**
   * Subscribe to user's async messages
   * Contains networking.
   */
  public void subscribeASyncMessaging() {
    flowInstance.subscribe(getUserFlowHandler(), getUserAor(),
        MessagingEvent.MessagingEventCategory.FLOW_MESSAGING_EVENTCATEGORY_ASYNC_MESSAGE,
        "", 1200, userAsyncHandler);
    flowInstance.subscribe(getUserFlowHandler(), getUserAor(),
        MessagingEvent.MessagingEventCategory.FLOW_MESSAGING_EVENTCATEGORY_ASYNC_MESSAGE_RESPONSE,
        "", 1200, asyncResponseHandler);
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
   * @return username
   */
  public String getUserName() {
    return Core.getDefaultClient().getLoggedInUser().getUserName();
  }

  public void clearDevicesCache() {
    FlowCache.clear();
    cachedDevices.clear();
  }

  public WifireDevice getCurrentDevice() {
    return currentDevice;
  }

  public void setCurrentDevice(WifireDevice currentDevice) {
    this.currentDevice = currentDevice;
  }
}
