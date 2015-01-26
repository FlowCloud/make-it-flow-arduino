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

package com.imgtec.hobbyist.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.adapters.TourFragmentPagerAdapter;
import com.viewpagerindicator.PageIndicator;

/**
 * Simple tour activity with ViewPager and PageIndicator.
 */
public class TourActivity extends BaseActivity {

  ViewPager viewPager;
  PageIndicator pageIndicator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.actv_tour);
    initActionBar();

    TourFragmentPagerAdapter viewPagerAdapter = new TourFragmentPagerAdapter(getSupportFragmentManager());
    viewPager = (ViewPager) findViewById(R.id.pager);
    viewPager.setAdapter(viewPagerAdapter);
    pageIndicator = (PageIndicator) findViewById(R.id.indicator);
    pageIndicator.setViewPager(viewPager);
  }

  @Override
  protected void initActionBar() {
    getSupportActionBar().hide();
  }

}
