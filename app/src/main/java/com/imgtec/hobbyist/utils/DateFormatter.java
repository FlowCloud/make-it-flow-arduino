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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Handles transitions between {@link java.util.Date} objects and Strings and vice versa.
 */
public final class DateFormatter {

  private static String FLOW_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /**
   * Transform Calendar to date string for use in Flow Messaging.
   */
  public static String fromCalendar(final Calendar calendar) {
    Date date = calendar.getTime();
    return new SimpleDateFormat(FLOW_DATE_FORMAT).format(date);
  }

  public static String fromCalendarUTC(Calendar calendar) {
    Date date = calendar.getTime();
    SimpleDateFormat sdf = new SimpleDateFormat(FLOW_DATE_FORMAT);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    return sdf.format(date);
  }

  /**
   * Transform Flow messaging date string to Date.
   */
  public static Date toDate(final String dateString) throws ParseException {
    return new SimpleDateFormat(FLOW_DATE_FORMAT).parse(dateString);
  }

  /**
   * Transforms Flow Messaging date string to localized date string for display.
   */
  public static String formatForDisplay(String dateString, Context context) {
    try {
      Date date = DateFormatter.toDate(dateString);
      return DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(date)
          + " " + android.text.format.DateFormat.getTimeFormat(context).format(date);
    } catch (ParseException e) {
      DebugLogger.log(DateFormatter.class.getSimpleName(), "parsing string from date failed");
    }
    return null;
  }

  /**
   * Get current date and time formatted for display.
   */
  public static String now(Context context) {
    return DateFormatter.formatForDisplay(fromCalendar(GregorianCalendar.getInstance()), context);
  }
}
