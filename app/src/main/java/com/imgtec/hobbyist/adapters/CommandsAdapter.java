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
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;

public class CommandsAdapter extends ArrayAdapter<String> {
  private ArrayList<String> itemsAll;
  private ArrayList<String> suggestions;

  public CommandsAdapter(Context context, int viewResourceId, ArrayList<String> items) {
    super(context, viewResourceId, items);
    this.itemsAll = (ArrayList<String>)items.clone();
    this.suggestions = new ArrayList<>();
  }

  @Override
  public Filter getFilter() {
    return filter;
  }

  Filter filter = new Filter() {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      if(constraint != null) {
        suggestions.clear();
        for (String command : itemsAll) {
          if(command.toLowerCase().contains(constraint.toString().toLowerCase())){
            suggestions.add(command);
          }
        }
        FilterResults filterResults = new FilterResults();
        filterResults.values = suggestions;
        filterResults.count = suggestions.size();
        return filterResults;
      } else {
        return new FilterResults();
      }
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      ArrayList<String> filteredList = (ArrayList<String>) results.values;
      if(results.count > 0) {
        clear();
        for (String c : filteredList) {
          add(c);
        }
        notifyDataSetChanged();
      }
    }
  };
}
