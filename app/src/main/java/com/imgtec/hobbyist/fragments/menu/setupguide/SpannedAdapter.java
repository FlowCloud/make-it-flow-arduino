package com.imgtec.hobbyist.fragments.menu.setupguide;

import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.imgtec.hobbyist.R;

import java.util.List;

/**
 * Created by simon.pinfold on 27/01/2015.
 */
public class SpannedAdapter extends ArrayAdapter<Spanned> {
  private LayoutInflater mInflater;

  public SpannedAdapter(Context context, int resource, List<Spanned> articleList) {
    super(context, resource, articleList);
    mInflater = LayoutInflater.from(context);
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.single_row, null);
      holder = new ViewHolder();
      holder.text = (TextView) convertView.findViewById(R.id.singleRow);

      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    holder.text.setText(getItem(position));

    return convertView;
  }

  static class ViewHolder {
    TextView text;
  }
}
