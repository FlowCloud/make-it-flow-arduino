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


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.imgtec.hobbyist.R;

import java.util.List;

/**
 * Adapter to link ssid data with UI
 * More information about Android adapter: {@link android.widget.Adapter}
 */

public class BoardWifiChoiceListAdapter extends AbstractListAdapter<String, BoardWifiChoiceListAdapter.RowHolder> {

  public BoardWifiChoiceListAdapter(Context context, List<String> ssidList) {
    super(context, R.layout.wdgt_board_ssid, ssidList);
    this.dataList = ssidList;
  }

  protected RowHolder createViewHolder(View rowView) {
    RowHolder rowHolder = new RowHolder();
    rowHolder.boardSSID = (TextView) rowView.findViewById(R.id.boardSSID);
    return rowHolder;
  }

  @TargetApi(16)
  protected void fillViewHolder(final RowHolder viewHolder, int position, ViewGroup parent) {
    viewHolder.boardSSID.setText(dataList.get(position));
    Drawable whiteBackgroundShape = context.getResources().getDrawable(R.drawable.purple_white_list_item_selector);
    Drawable grayBackgroundShape = context.getResources().getDrawable(R.drawable.purple_gray_list_item_selector);
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      viewHolder.boardSSID.setBackgroundDrawable(position % 2 == 0 ? whiteBackgroundShape : grayBackgroundShape);
    } else {
      viewHolder.boardSSID.setBackground(position % 2 == 0 ? whiteBackgroundShape : grayBackgroundShape);
    }
  }

  static class RowHolder {
    TextView boardSSID;
  }

}

