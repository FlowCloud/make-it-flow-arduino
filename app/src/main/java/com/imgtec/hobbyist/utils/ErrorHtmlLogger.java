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

package com.imgtec.hobbyist.utils;

import android.content.Context;

import com.imgtec.flow.ErrorType;
import com.imgtec.hobbyist.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Writes and retrieves error logs to/from html file on the device.
 */
public class ErrorHtmlLogger {
  private static String logFilePath;
  private static PrintWriter writer;
  private static Context appContext;

  public static void setup(Context context) {
    appContext = context.getApplicationContext();
    logFilePath = appContext.getFilesDir() + "/log.html";
    setupWriter(false);
  }

  private static void setupWriter(boolean append) {
    try {
      writer = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, append)));
      writer.write(getHead());
    } catch (IOException e) {
      DebugLogger.log(ErrorHtmlLogger.class.getSimpleName(), "failed to create logs file");
    }
  }

  /**
   * Appends log information adequate for given ErrorType to the html file containing logs.
   * @param error type of error
   * @return resource id of String appended to the html file
   */
  public static synchronized int log(ErrorType error) {
    if (!error.equals(ErrorType.FLOW_ERROR_NO_ERROR)) {
      int res;
      switch (error) {
        case FLOW_ERROR_INTERNAL:
          res = R.string.error_internal;
          break;
        case FLOW_ERROR_MEMORY:
          res = R.string.error_insufficient_memory;
          break;
        case FLOW_ERROR_METHOD_UNAVAILABLE:
          res = R.string.error_login_method_unavailable;
          break;
        case FLOW_ERROR_INVALID_ARGUMENT:
          res = R.string.error_invalid_argument;
          break;
        case FLOW_ERROR_RESOURCE_NOT_FOUND:
          res = R.string.error_login_resource_not_available;
          break;
        case FLOW_ERROR_NETWORK:
          res = R.string.error_network;
          break;
        case FLOW_ERROR_UNAUTHORISED:
          res = R.string.error_unauthorized;
          break;
        case FLOW_ERROR_CONFLICT:
          res = R.string.error_conflict_data;
          break;
        case FLOW_ERROR_REMOVED:
          res = R.string.error_resource_removed;
          break;
        case FLOW_ERROR_SERVER:
          res = R.string.error_internal_server;
          break;
        case FLOW_ERROR_SERVER_BUSY:
          res = R.string.error_server_busy;
          break;
        case FLOW_ERROR_SERVER_TIMEOUT:
          res = R.string.error_server_timeout;
          break;
        case FLOW_ERROR_ANONYMOUS:
          res = R.string.error_anonymous;
          break;
        case FLOW_ERROR_VERSION_CONFLICT:
          res = R.string.error_client_not_compatible;
          break;
        default:
          res = R.string.error_unknown;
          break;
      }
      String str = DateFormatter.now(appContext) + " : " + appContext.getString(res) + "</br></br>";
      writer.write(str);
      return res;
    }
    return R.string.error_unknown;
  }

  /**
   * @return current contents of html logs file as a String (with all <tags> included)
   */
  public static String getHtml() {
    writer.write(getTail());
    writer.close();

    StringBuilder sb = new StringBuilder();
    try {
      BufferedReader br = new BufferedReader(new FileReader(logFilePath));
      String line = br.readLine();
      while (line != null) {
        sb.append(line);
        line = br.readLine();
      }
    } catch (IOException e) {
      DebugLogger.log(ErrorHtmlLogger.class.getSimpleName(), "failed to read log file");
    }

    setupWriter(true);
    return sb.toString();
  }

  private static String getHead() {
    return "<!DOCTYPE html>\n";
  }

  private static String getTail() {
    return "</html>";
  }
}
