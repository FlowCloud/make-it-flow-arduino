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

package com.imgtec.hobbyist.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.imgtec.hobbyist.fragments.tour.TourConfigureFragment;
import com.imgtec.hobbyist.fragments.tour.TourLoginFragment;
import com.imgtec.hobbyist.fragments.tour.TourSendCommandFragment;

/**
 * Adapter to service {@link com.imgtec.hobbyist.activities.TourActivity#viewPager} pages.
 * One particular fragment per each page.
 */

public class TourFragmentPagerAdapter extends FragmentPagerAdapter {

  private final int TOUR_FRAGMENTS_COUNT = 3;

  public TourFragmentPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case 0:
        return TourLoginFragment.newInstance();
      case 1:
        return TourConfigureFragment.newInstance();
      case 2:
        return TourSendCommandFragment.newInstance();
    }
    return null;
  }

  @Override
  public int getCount() {
    return TOUR_FRAGMENTS_COUNT;
  }
}