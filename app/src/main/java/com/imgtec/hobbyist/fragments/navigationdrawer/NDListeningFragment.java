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

package com.imgtec.hobbyist.fragments.navigationdrawer;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;


/**
 * A fragment which can change contents of NavigationDrawer Menu by switching application state to one of {@link com.imgtec.hobbyist.utils.NDMenuMode}.
 * It works through {@link com.imgtec.hobbyist.activities.FlowActivity}
 * which implements {@link NDMenuListener}.
 *
 * onAttach and onDetach are just for appropriate activity reference initialization/removing
 */
public abstract class NDListeningFragment extends Fragment {

  protected NDMenuListener activity;
  protected Context appContext;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.appContext = activity.getApplicationContext();
    if (activity instanceof NDMenuListener) {
      this.activity = (NDMenuListener) activity;
    }
  }

  @Override
  public void onDetach() {
    activity = null;
    super.onDetach();
  }

}
