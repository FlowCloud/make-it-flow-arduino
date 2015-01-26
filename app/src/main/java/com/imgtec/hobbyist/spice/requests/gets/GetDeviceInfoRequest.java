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

package com.imgtec.hobbyist.spice.requests.gets;

import android.content.Context;

import com.imgtec.hobbyist.spice.pojos.DeviceInfo;
import com.imgtec.hobbyist.spice.requests.abstracts.AbstractRequest;

/**
 * Create a request in its own Java file, it should not be an inner class of a Context
 */
public class GetDeviceInfoRequest extends AbstractRequest<DeviceInfo, Void> {

  public GetDeviceInfoRequest(Context appContext) {
    super(DeviceInfo.class, appContext, "/device_info.cgi?output=xml", RequestType.get, null);
  }

  public String createCacheKey() {
    return GetDeviceInfoRequest.class.toString();
  }
}
