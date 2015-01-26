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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * {@link android.widget.AutoCompleteTextView} that instantly shows auto-completion suggestions.
 * Suggestions list appears at the moment of gaining focus and shows all known phrases.
 */
public class InstantAutoCompleteTextView extends AutoCompleteTextView {

  public InstantAutoCompleteTextView(Context context) {
    super(context);
  }

  public InstantAutoCompleteTextView(Context arg0, AttributeSet arg1) {
    super(arg0, arg1);
  }

  public InstantAutoCompleteTextView(Context arg0, AttributeSet arg1, int arg2) {
    super(arg0, arg1, arg2);
  }

  @Override
  public boolean enoughToFilter() {
    return true;
  }

  @Override
  protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
    super.onFocusChanged(focused, direction, previouslyFocusedRect);
    if (focused) {
      performFiltering(getText(), 0);
    }
  }
}
