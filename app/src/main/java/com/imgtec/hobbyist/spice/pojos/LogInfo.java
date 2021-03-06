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

package com.imgtec.hobbyist.spice.pojos;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "loginfo")
public class LogInfo {

  @Element
  private String datetime;
  @Element
  private String level;
  @Element
  private String category;
  @Element
  private String message;

  public String getDatetime() {
    return datetime;
  }

  public String getLevel() {
    return level;
  }

  public String getCategory() {
    return category;
  }

  public String getMessage() {
    return message;
  }
}
