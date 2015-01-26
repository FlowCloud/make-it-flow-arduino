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

package com.imgtec.hobbyist.fragments.loginsignup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import com.imgtec.hobbyist.R;

public abstract class FragmentWithProgressBar extends FragmentWithTitle {

  private ProgressDialog loginProgressDialog;
  protected Handler handler = new Handler();
  protected Context appContext;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    appContext = activity.getApplicationContext();
  }

  protected void showProgress(String message) {
    loginProgressDialog = ProgressDialog.show(getActivity(), appContext.getString(R.string.please_wait_with_dots), message, true);
    loginProgressDialog.setCanceledOnTouchOutside(false);
  }

  protected void hideProgress() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (loginProgressDialog != null) {
          loginProgressDialog.dismiss();
        }
      }
    });
  }

  @Override
  public void onPause() {
    if (loginProgressDialog != null) {
      loginProgressDialog.dismiss();
    }
    super.onPause();
  }

}
