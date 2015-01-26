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

@Root(name = "config")
public class InnerNetworkConfig {

  /**
   * Constructor used to initialize {@link com.imgtec.hobbyist.spice.requests.posts.SetNetworkAndFlowConfigurationRequest} request
   */
  public InnerNetworkConfig(@Element(name = "device") String device,
                            @Element(name = "oauth_key") String oauth_key,
                            @Element(name = "oauth_secret") String oauth_secret,
                            @Element(name = "registration_key", required = false) String registration_key,
                            @Element(name = "root_url") String root_url,
                            @Element(name = "ssid") String ssid,
                            @Element(name = "encryption") String encryption,
                            @Element(name = "password") String password,
                            @Element(name = "addr_method") String addr_method) {
    this.device = device;
    this.oauth_key = oauth_key;
    this.oauth_secret = oauth_secret;
    this.registration_key = registration_key;
    this.root_url = root_url;
    this.ssid = ssid;
    this.encryption = encryption;
    this.password = password;
    this.addr_method = addr_method;
  }

  public InnerNetworkConfig(@Element(name = "device", required = false) String device,
                            @Element(name = "oauth_key", required = false) String oauth_key,
                            @Element(name = "oauth_secret", required = false) String oauth_secret,
                            @Element(name = "registration_key", required = false) String registration_key,
                            @Element(name = "root_url", required = false) String root_url,
                            @Element(name = "ssid", required = false) String ssid,
                            @Element(name = "encryption", required = false) String encryption,
                            @Element(name = "password", required = false) String password,
                            @Element(name = "addr_method", required = false) String addr_method,
                            @Element(name = "static_dns", required = false) String static_dns,
                            @Element(name = "static_ip", required = false) String static_ip,
                            @Element(name = "static_netmask", required = false) String static_netmask,
                            @Element(name = "static_gateway", required = false) String static_gateway) {
    this.device = device;
    this.oauth_key = oauth_key;
    this.oauth_secret = oauth_secret;
    this.registration_key = registration_key;
    this.root_url = root_url;
    this.ssid = ssid;
    this.encryption = encryption;
    this.password = password;
    this.addr_method = addr_method;
    this.static_dns = static_dns;
    this.static_ip = static_ip;
    this.static_netmask = static_netmask;
    this.static_gateway = static_gateway;
  }

  @Element(required = false)
  private String device;
  @Element(required = false)
  private String oauth_key;
  @Element(required = false)
  private String oauth_secret;
  @Element(required = false)
  private String registration_key;
  @Element(required = false)
  private String root_url;
  @Element(required = false)
  private String ssid;
  @Element(required = false)
  private String encryption;
  @Element(required = false)
  private String password; // Is always empty because of security issues. Date of comment: 13.05.2014
  @Element(required = false)
  private String addr_method;
  @Element(required = false)
  private String static_dns;
  @Element(required = false)
  private String static_ip;
  @Element(required = false)
  private String static_netmask;
  @Element(required = false)
  private String static_gateway;

  public String getDevice() {
    return device;
  }

  public String getOauth_key() {
    return oauth_key;
  }

  public String getOauth_secret() {
    return oauth_secret;
  }

  public String getRegistration_key() {
    return registration_key;
  }

  public String getRoot_url() {
    return root_url;
  }

  public String getSsid() {
    return ssid;
  }

  public String getEncryption() {
    return encryption;
  }

  public String getAddr_method() {
    return addr_method;
  }

  public String getStatic_dns() {
    return static_dns;
  }

  public String getStatic_ip() {
    return static_ip;
  }

  public String getStatic_netmask() {
    return static_netmask;
  }

  public String getStatic_gateway() {
    return static_gateway;
  }
}
