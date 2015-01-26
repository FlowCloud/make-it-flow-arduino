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
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.flow.WifireDevice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adapter to link connected devices data with UI
 * More information about Android adapter: {@link android.widget.Adapter}
 */

public class ConnectedDevicesAdapter extends AbstractListAdapter<WifireDevice, ConnectedDevicesAdapter.RowHolder> {

  private Map<Integer, Boolean> contextualChecked = new HashMap<>();

  public ConnectedDevicesAdapter(Context context, List<WifireDevice> connectedDevices) {
    super(context, R.layout.wdgt_connected_device_item, connectedDevices);
    this.dataList = connectedDevices;
  }

  /**
   * Sets checked status of an item at desired position
   *
   * @param position - position of item
   */
  public void switchContextualChecked(int position) {
    if (isContextualChecked(position)) {
      contextualChecked.put(position, false);
    } else {
      contextualChecked.put(position, true);
    }
    notifyDataSetChanged();
  }

  /**
   * Checks status of an item at desired position
   *
   * @param position - position of item
   */
  public boolean isContextualChecked(int position) {
    Boolean result = contextualChecked.get(position);
    return result == null ? false : result;
  }

  /**
   * Returns list of all checked devices
   */
  public List<WifireDevice> getContextualSelectedDevices() {
    List<WifireDevice> selectedDevices = new CopyOnWriteArrayList<>();
    for (Map.Entry<Integer, Boolean> entry : contextualChecked.entrySet()) {
      if (entry.getValue()) {
        selectedDevices.add(dataList.get(entry.getKey()));
      }
    }
    return selectedDevices;
  }

  /**
   * Clears list of checked items
   */
  public void clearContextualSelected() {
    contextualChecked.clear();
  }

  protected RowHolder createViewHolder(View rowView) {
    RowHolder rowHolder = new RowHolder();
    rowHolder.myDevice = (TextView) rowView.findViewById(R.id.myDevice);
    rowHolder.connectedDeviceLayout = (RelativeLayout) rowView.findViewById(R.id.connectedDeviceLayout);
    return rowHolder;
  }

  @TargetApi(16)
  protected void fillViewHolder(final RowHolder viewHolder, int position, ViewGroup parent) {
    viewHolder.myDevice.setText(dataList.get(position).toString());
    boolean isPositionChecked = isItemChecked(position, (ListView) parent);
    viewHolder.myDevice.setTextColor(isPositionChecked
        ? context.getResources().getColor(R.color.nice_lavender)
        : context.getResources().getColor(R.color.nice_dark_gray));
    Drawable checkedBackground = context.getResources().getDrawable(R.drawable.very_light_background_with_stroke_shape);
    Drawable backgroundShape;
    if(position%2==1) {
      backgroundShape = context.getResources().getDrawable(R.drawable.device_list_transparent_background);
    }
    else {
      backgroundShape = context.getResources().getDrawable(R.drawable.device_list_white_background);
    }
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      viewHolder.connectedDeviceLayout.setBackgroundDrawable(isPositionChecked ? checkedBackground : backgroundShape);
    } else {
      viewHolder.connectedDeviceLayout.setBackground(isPositionChecked ? checkedBackground : backgroundShape);
    }
    ImageView symbol = (ImageView) viewHolder.connectedDeviceLayout.findViewById(R.id.deviceSymbol);
    symbol.setImageResource((dataList.get(position)).isNetworkConnected() ?
        R.drawable.device_in_interactive_mode : R.drawable.device_offline_in_interactive_mode);
  }

  private boolean isItemChecked(int position, ListView listView) {
    if (listView.getChoiceMode() == AbsListView.CHOICE_MODE_SINGLE) { //normal mode
      int checkedItemPosition = listView.getCheckedItemPosition();
      return checkedItemPosition == position;
    } else { //contextual mode
      return isContextualChecked(position);
    }
  }

  static class RowHolder {
    RelativeLayout connectedDeviceLayout;
    TextView myDevice;
  }

}
