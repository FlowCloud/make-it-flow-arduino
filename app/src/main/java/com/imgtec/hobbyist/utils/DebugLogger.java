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

import android.util.Log;

public class DebugLogger {

  public static final boolean DEBUG = true;

  public static void log(String tag, String msg) {
    if (DEBUG) {
      Log.e(tag, msg);
    }
  }

  public static void log(String tag, Exception e) {
    if (DEBUG) {
      if (e.getCause() != null) {
        Log.e(tag, e.toString() + e.getMessage() + "\n" + e.getCause().toString());
      } else {
        Log.e(tag, e.toString() + e.getMessage());
      }
    }
  }

}
