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

package com.imgtec.hobbyist.spice.requests;

import com.imgtec.hobbyist.activities.BaseActivity;
import com.imgtec.hobbyist.spice.requests.abstracts.AbstractRequest;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Class which encapsulates {@link #spiceManager} lifecycle.
 * Also provide general request performer {@link #performRequest}
 */

public class RobospiceRequestsHandler {

  private final BaseActivity baseActivity;

  /**
   * spiceManager should run every time we try to {@link #performRequest}
   */
  private final SpiceManager spiceManager;

  public RobospiceRequestsHandler(BaseActivity baseActivity, SpiceManager spiceManager) {
    this.baseActivity = baseActivity;
    this.spiceManager = spiceManager;
  }

  public void startSpiceManager() {
    spiceManager.start(baseActivity);
  }

  public void stopSpiceManager() {
    if (spiceManager.isStarted()) {
      spiceManager.shouldStop();
    }
  }

  /**
   * @param listener listens request response
   * @param request  just the request code
   * @param <T>      success request response content POJO. Used mainly in GET requests.
   * @param <S>      request POST content. Void for GET requests.
   */
  public <T, S> void performRequest(RequestListener<T> listener, AbstractRequest<T, S> request) {
    spiceManager.execute(request, null, DurationInMillis.ALWAYS_EXPIRED, listener);
  }

}
