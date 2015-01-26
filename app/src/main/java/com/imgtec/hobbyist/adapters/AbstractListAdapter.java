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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Abstract version of ListAdapter
 * More information about Android adapter: {@link android.widget.Adapter}
 */

public abstract class AbstractListAdapter<T, S> extends ArrayAdapter<T> {

  protected final Context context;
  protected List<T> dataList;
  protected int resource;
  protected LayoutInflater inflater;

  public AbstractListAdapter(Context context, int resource, List<T> dataList) {
    super(context, resource, dataList);
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.context = context;
    this.resource = resource;
    this.dataList = dataList;
  }

  /**
   * Implementation of Adapter.getView(). It's called by list and should not be called by user.
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View rowView = convertView;
    S viewHolder;
    if (rowView == null || rowView.getTag() == null) {
      rowView = inflater.inflate(resource, parent, false);
      viewHolder = createViewHolder(rowView);
      rowView.setTag(viewHolder);
    } else {
      viewHolder = (S) rowView.getTag();
    }
    fillViewHolder(viewHolder, position, parent);
    return rowView;
  }

  /**
   * Method creating a new view holder for row view
   *
   * @param rowView
   * @return new view holder
   */
  protected abstract S createViewHolder(View rowView);

  /**
   * Fill UI items
   *
   * @param viewHolder views holder to fill (never null)
   * @param position   position of item in list
   * @param parent     parent of textView
   */
  protected abstract void fillViewHolder(final S viewHolder, int position, ViewGroup parent);

}
