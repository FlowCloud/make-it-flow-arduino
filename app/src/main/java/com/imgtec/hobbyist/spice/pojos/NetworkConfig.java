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

@Root(name = "network-config")
public class NetworkConfig {
  public NetworkConfig(@Element(name = "success") String success, @Element(name = "config") InnerNetworkConfig innerNetworkConfig) {
    this.success = success;
    this.innerNetworkConfig = innerNetworkConfig;
  }

  @Element
  private String success;
  @Element(name = "config")
  private InnerNetworkConfig innerNetworkConfig;

  public String getSuccess() {
    return success;
  }

  public InnerNetworkConfig getInnerNetworkConfig() {
    return innerNetworkConfig;
  }
}