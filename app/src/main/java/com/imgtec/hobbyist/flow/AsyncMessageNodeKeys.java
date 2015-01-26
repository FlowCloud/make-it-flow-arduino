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

/**
 * Contains tags that may appear in xml contents of async messages.
 */
public interface AsyncMessageNodeKeys {

  public static final String SENT_WITH_TYPE_INFO = "sent type=\"datetime\"";
  public static final String SENT = "sent";
  public static final String TO = "to";
  public static final String FROM = "from";
  public static final String CLIENTID = "clientid type=\"integer\"";
  public static final String REQUESTID = "requestid type=\"integer\"";
  public static final String DETAILS = "details";
  public static final String MESSAGE = "message";
  public static final String RESPONSE_CODE = "responsecode";
  public static final String RESPONSE_PARAMS = "responseparams";

}
