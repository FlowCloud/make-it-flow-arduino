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

import com.imgtec.flow.MessagingEvent;

/**
 * Listener to provide change in UI part responsible for fetching AsyncMessages in appropriate time.
 */
public interface AsyncMessageListener {
  public void onCommandRXMessageReceived(AsyncMessage msg);

  public void onAsyncMessageResponse(MessagingEvent.AsyncMessageResponse response);

  public void onTextMessageReceived(AsyncMessage msg);

  public void onCommandMessageReceived(AsyncMessage msg);
}
