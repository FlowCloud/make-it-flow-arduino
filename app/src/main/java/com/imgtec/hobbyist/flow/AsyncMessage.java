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

import com.imgtec.hobbyist.utils.DebugLogger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates XML content of async message, that will be sent to Flow and then directed to board.
 */
public class AsyncMessage {

  public enum MessageType {
    COMMAND("command"), RESPONSE("response"), MESSAGE("message"), EMPTY("");

    String string;

    MessageType(String s) {
      string = s;
    }

    public static MessageType retrieveMessageTypeFromXmlTag(String tag) {
      for (MessageType messageType : values()) {
        if (messageType.string.equals(tag)) {
          return messageType;
        }
      }
      return null;
    }
  }

  private final static String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  private static short requestId = -1;

  private MessageType type;
  private Map<String, String> nodes = new LinkedHashMap<>();

  private AsyncMessage() {
  }

  /**
   * Create a new asynchronous message.
   *
   * @param type type
   * @param map  nodes. Pass null to init an empty message
   * @return asynchronous message
   */
  public static AsyncMessage newInstance(MessageType type, Map<String, String> map) {
    if (requestId == 255) {
      requestId = 0;
    } else {
      ++requestId;
    }

    AsyncMessage asyncMessage = new AsyncMessage();
    asyncMessage.setType(type);
    if (map != null) {
      asyncMessage.nodes.putAll(map);
    }
    return asyncMessage;
  }

  /**
   * This will also change message's type to value found in xml.
   *
   * @param xml
   */
  public void setNodesFromXml(String xml) {
    XmlPullParserFactory factory;
    XmlPullParser xpp;
    try {
      factory = XmlPullParserFactory.newInstance();
      xpp = factory.newPullParser();
      xpp.setInput(new StringReader(xml));
      int eventType = xpp.getEventType();
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG) {
          final MessageType messageType = MessageType.retrieveMessageTypeFromXmlTag(xpp.getName());
          if (messageType != null && !messageType.equals(MessageType.EMPTY)) {
            setType(messageType);
          } else {
            addNode(xpp.getName(), xpp.nextText());
          }
        }
        eventType = xpp.next();
      }
    } catch (XmlPullParserException | IOException e) {
      DebugLogger.log(getClass().getSimpleName(), "parsing xml failed");
    }
  }

  public void setType(MessageType type) {
    if (type.equals(MessageType.COMMAND) || type.equals(MessageType.RESPONSE) || type.equals(MessageType.MESSAGE)) {
      this.type = type;
    } else {
      throw new IllegalArgumentException("Message must have a type of COMMAND, RESPONSE or MESSAGE");
    }
  }

  public MessageType getType() {
    return type;
  }

  public void addNode(String key, String value) {
    nodes.put(key, value);
  }

  public String getNode(String key) {
    return nodes.get(key);
  }

  public String getRequestId() {
    return String.valueOf(requestId);
  }

  /**
   * Creates XML content based on previously set nodes. Returns String result.
   *
   * @return
   */
  public String buildXml() {
    String result = xmlHeader;
    result = result.concat("<" + type.string + ">");
    for (String key : nodes.keySet()) {
      String endTag = key.contains(" type") ? key.substring(0, key.indexOf(" ")) : key;
      result = result.concat("<" + key + ">").concat(nodes.get(key)).concat("</" + endTag + ">");
    }
    result = result.concat("</" + type.string + ">");

    return result;
  }

}
