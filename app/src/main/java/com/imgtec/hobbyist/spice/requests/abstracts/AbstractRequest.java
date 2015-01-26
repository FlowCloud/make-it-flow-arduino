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

package com.imgtec.hobbyist.spice.requests.abstracts;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.HttpsClient;
import com.imgtec.hobbyist.utils.Preferences;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.SimpleXmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public abstract class AbstractRequest<T, S> extends SpringAndroidSpiceRequest<T> {

  private enum TransferProtocol {http, https}

  protected enum RequestType {get, post}

  private TransferProtocol transferProtocol;
  private Context appContext;
  private String requestUrl;
  private RequestType requestType;
  private S postRequestObject;

  /**
   * @param clazz             to provide information about result type to request. Can retrieve by getResultType()
   * @param appContext        application context to retrieve informations from SharedPreferences
   * @param endpoint          endpoint of request url
   * @param requestType       get or post types are supported
   * @param postRequestObject if {@code requestType == RequestType.get} this should be null.
   *                          if {@code requestType == RequestType.post} this can be post request object
   */
  protected AbstractRequest(Class<T> clazz, Context appContext, String endpoint, RequestType requestType, S postRequestObject) {
    super(clazz);
    this.appContext = appContext;
    this.requestType = requestType;
    this.postRequestObject = postRequestObject;

    this.requestUrl = getHostString() + "/cgi-bin" + endpoint;

    try {
      URL url = new URL(requestUrl);
      setTransferProtocol(url);
    } catch (MalformedURLException e) {
      DebugLogger.log(clazz.getSimpleName(), "malformed URL");
    }
  }

  private void setTransferProtocol(URL url) {
    if ((url).getProtocol().equals(TransferProtocol.http.name())) {
      this.transferProtocol = TransferProtocol.http;
    } else if ((url).getProtocol().equals(TransferProtocol.https.name())) {
      this.transferProtocol = TransferProtocol.https;
    }
  }

  private String getHostString() {
    SharedPreferences sharedPreferences = appContext.getSharedPreferences(Preferences.SETTINGS, Context.MODE_PRIVATE);
    return sharedPreferences.getString(Preferences.WIFIRE_URL,
        Constants.WIFIRE_BOARD_REQUESTS_MODE
            ? Preferences.WIFIRE_URL_BOARD_WEBSERVICE_URL
            : Preferences.WIFIRE_URL_APIARY_WEBSERVICE_URL
    );
  }

  public T loadDataFromNetwork() throws Exception {
    if (transferProtocol == TransferProtocol.http) {
      return executeRequest();
    } else if (transferProtocol == TransferProtocol.https) {
      return executeSSLRequest();
    } else {
      Toast.makeText(appContext, appContext.getString(R.string.not_appropriate_transfer_protocol), Toast.LENGTH_SHORT).show();
      DebugLogger.log(this.getClass().getSimpleName(), appContext.getString(R.string.not_appropriate_transfer_protocol));
    }
    return null;
  }

  private T executeRequest() {
    Log.d(this.getClass().getSimpleName(), "URL of request is: " + requestUrl);
    return getRequestResponse(requestUrl);
  }

  private T executeSSLRequest() {
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpsClient.getNewHttpClient());
    RestTemplate restTemplate = new RestTemplate(requestFactory);
    Log.d(this.getClass().getSimpleName(), "URL of request is: " + requestUrl);
    SimpleXmlHttpMessageConverter xmlConverter = new SimpleXmlHttpMessageConverter();
    List<HttpMessageConverter<?>> listHttpMessageConverters = restTemplate.getMessageConverters();
    listHttpMessageConverters.add(xmlConverter);
    restTemplate.setMessageConverters(listHttpMessageConverters);
    setRestTemplate(restTemplate);
    return getRequestResponse(requestUrl);
  }

  private T getRequestResponse(String requestUrl) {
    if (requestType == RequestType.get) {
      return getRestTemplate().getForObject(requestUrl, getResultType());
    } else if (requestType == RequestType.post) {
      return getRestTemplate().postForObject(requestUrl, postRequestObject, getResultType());
    } else {
      DebugLogger.log(this.getClass().getSimpleName(), "Wrong request type.");
      return null;
    }
  }

  public abstract String createCacheKey();

}
