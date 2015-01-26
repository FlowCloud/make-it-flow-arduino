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

package com.imgtec.hobbyist.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;

/**
 * Class handling view animations.
 */
public class AnimationUtils {

  /**
   * Start given animation on provided view.
   */
  public static void startAnimation(View view, int animationId) {
    view.setBackgroundResource(animationId);
    AnimationDrawable animationDrawable = (AnimationDrawable) view.getBackground();
    if (animationDrawable != null) {
      animationDrawable.start();
    }
  }

  /**
   * Fade in or fade out a given view.
   */
  public static void animateViewSetVisible(final boolean visible, final View view) {
    view.setVisibility(View.VISIBLE);
    view.setAlpha(visible ? 0 : 1);
    view.animate()
        .setDuration(300)
        .alpha(visible ? 1 : 0)
        .setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
          }
        });
  }

}
