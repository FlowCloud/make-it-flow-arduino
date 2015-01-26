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

package com.imgtec.hobbyist.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.imgtec.hobbyist.R;

/**
 * WebView specialized to display Gifs from resources.
 */
public class GifWebView extends WebView {

  public GifWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.GifWebView);
    String path = attributes.getString(R.styleable.GifWebView_path);
    attributes.recycle();
    WebSettings settings = this.getSettings();
    settings.setUseWideViewPort(true);
    settings.setLoadWithOverviewMode(true);
    String folder = path.substring(0, path.lastIndexOf('/') + 1);
    String filename = path.substring(path.lastIndexOf('/') + 1, path.length());
    loadDataWithBaseURL(folder, "<html><center><img src=\"" + filename + "\"></html>","text/html","utf-8","");
  }

}
