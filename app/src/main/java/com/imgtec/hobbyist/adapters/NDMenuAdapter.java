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

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.flow.WifireDevice;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;

/**
 * Adapter to service NavigationDrawer and its 3 options menu possibilities defined in
 * {@link com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem}
 * <p/>
 * More information about Android adapter: {@link android.widget.Adapter}
 */

public class NDMenuAdapter extends ArrayAdapter {

  private LayoutInflater inflater;
  private final Context context;
  private NDMenuItem[] ndMenuItems;

  public NDMenuAdapter(Context context, NDMenuItem[] ndMenuItems) {
    super(context, 0);
    this.context = context;
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.ndMenuItems = ndMenuItems;
  }

  @Override
  public int getCount() {
    return ndMenuItems.length;
  }

  @Override
  public Object getItem(int position) {
    return ndMenuItems[position];
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final NDMenuItem menuItem = ndMenuItems[position];
    View rowView = convertView;
    RowHolder viewHolder;
    if (rowView == null || rowView.getTag() == null) {
      if (!menuItem.isSeparator() && !menuItem.isMyDevice()) {
        rowView = inflater.inflate(R.layout.wdgt_navigation_drawer_item, parent, false);
        viewHolder = createViewHolder(rowView);
        rowView.setTag(viewHolder);
        fillViewHolder(viewHolder, menuItem);
      } else if (menuItem.isSeparator()) {
        rowView = inflater.inflate(R.layout.wdgt_navigation_drawer_separator, parent, false);
      } else {
        rowView = inflater.inflate(R.layout.wdgt_navigation_drawer_header, parent, false);
        viewHolder = createViewHolder(rowView);
        rowView.setTag(viewHolder);
        fillViewHolder(viewHolder, menuItem);
      }
    } else if (!menuItem.isSeparator()) {
      viewHolder = (RowHolder) rowView.getTag();
      fillViewHolder(viewHolder, menuItem);
    }
    return rowView;
  }

  private RowHolder createViewHolder(View rowView) {
    RowHolder rowHolder = new RowHolder();
    rowHolder.title = (TextView) rowView.findViewById(R.id.navigationDrawerItemText);
    rowHolder.selectionMark = (ImageView) rowView.findViewById(R.id.selectionMark);
    rowHolder.deviceStatusImage = (ImageView) rowView.findViewById(R.id.deviceStatusImage);
    return rowHolder;
  }

  private void fillViewHolder(final RowHolder viewHolder, final NDMenuItem menuItem) {
    viewHolder.selectionMark.setVisibility(NDMenuItem.isChecked(menuItem) ? View.VISIBLE : View.INVISIBLE);
    if (menuItem.isMyDevice()) {
      fillDeviceInfo(viewHolder);
    } else if (menuItem.isUserName()) {
      String userName = FlowEntities.getInstance(context).getCurrentUsername();
      viewHolder.title.setSingleLine();
      viewHolder.title.setEllipsize(TextUtils.TruncateAt.END);
      viewHolder.title.setText(userName);
    } else {
      viewHolder.title.setText(context.getString(menuItem.getTextId()));
    }
  }

  private void fillDeviceInfo(RowHolder viewHolder) {
    if (NDMenuMode.getMode() == NDMenuMode.Interactive) {
      fillInteractiveModeDevice(viewHolder);
    } else if (NDMenuMode.getMode() == NDMenuMode.Setup) {
      fillWifiNetworkModeDevice(viewHolder);
    }
  }

  private void fillInteractiveModeDevice(RowHolder viewHolder) {
    WifireDevice device = FlowHelper.getInstance((Activity) context).getCurrentDevice();
    viewHolder.title.setText(device.getName());
    viewHolder.deviceStatusImage.setImageResource(device.isNetworkConnected()
        ? R.drawable.device_in_interactive_mode
        : R.drawable.device_offline_in_interactive_mode);
  }

  private void fillWifiNetworkModeDevice(RowHolder viewHolder) {
    viewHolder.title.setText(SetupGuideInfoSingleton.getInstance().getDeviceName());
    viewHolder.deviceStatusImage.setImageResource(R.drawable.device_in_softap_mode);
  }

  /**
   * Disable possibility of clicking on {@link com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem#Separator}
   *
   * @param position of non-clickable NavigationDrawer Separator
   * @return
   */
  @Override
  public boolean isEnabled(int position) {
    if (ndMenuItems[position].isUserName()) {
      return false;
    }
    return !ndMenuItems[position].isSeparator();
  }

  static class RowHolder {
    ImageView selectionMark;
    TextView title;
    ImageView deviceStatusImage;
  }

}
