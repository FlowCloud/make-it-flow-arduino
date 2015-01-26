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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

/**
 * BroadcastReceiver which allows dynamic registration / unregistration of itself.
 * Android does not provide any way to check if given receiver is registered, and attempt to unregister
 * a receiver that is not registered causes an exception. This class allows safe unregistering
 * in most cases.
 */
public abstract class BroadcastReceiverWithRegistrationState extends BroadcastReceiver {

  private boolean isRegistered;
  private final IntentFilter intentFilter;

  public BroadcastReceiverWithRegistrationState(IntentFilter intentFilter) {
    this.intentFilter = intentFilter;
  }

  public void register(Context appContext) {
    if (!isRegistered) {
      appContext.registerReceiver(this, intentFilter);
      isRegistered = true;
    }
  }

  public void unregister(Context appContext) {
    if (isRegistered) {
      appContext.unregisterReceiver(this);
      isRegistered = false;
    }
  }

}
