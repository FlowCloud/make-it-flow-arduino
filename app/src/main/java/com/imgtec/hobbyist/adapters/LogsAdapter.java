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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.spice.pojos.LogInfo;
import com.imgtec.hobbyist.utils.DateFormatter;
import com.imgtec.hobbyist.utils.DebugLogger;

import java.util.List;

/**
 * Adapter to link logs information with logs UI list.
 * <p/>
 * More information about Android adapter: {@link android.widget.Adapter}
 *
 */

public class LogsAdapter extends ArrayAdapter {

  private static final String ERROR_LOG_LEVEL = "ERROR";
  private static final String WARNING_LOG_LEVEL = "WARNING";
  private static final String INFORMATION_LOG_LEVEL = "INFORMATION";
  private static final String DEBUG_LOG_LEVEL = "DEBUG";
  private static final String UNKNOWN_LOG_LEVEL = "UNKNOWN";
  private static final int DEFAULT_TEXT_LINES = 2;
  private static final int MAX_TEXT_LINES = 10;

  private LayoutInflater inflater;
  private final Context context;
  private List<LogInfo> logInfoList;

  public LogsAdapter(Context context, List<LogInfo> logInfoList) {
    super(context, 0);
    this.context = context;
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.logInfoList = logInfoList;
  }

  @Override
  public int getCount() {
    return logInfoList.size();
  }

  @Override
  public Object getItem(int position) {
    return logInfoList.get(position);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final LogInfo logInfo = logInfoList.get(position);
    View rowView = convertView;
    RowHolder viewHolder;
    if (rowView == null || rowView.getTag() == null) {
      rowView = inflater.inflate(R.layout.wdgt_log_info_text_item, parent, false);
      viewHolder = createViewHolder(rowView);
      rowView.setTag(viewHolder);
    } else {
      viewHolder = (RowHolder) rowView.getTag();
    }
    fillViewHolder(viewHolder, logInfo, position, ((ListView) parent).getCheckedItemPosition());
    return rowView;
  }

  private RowHolder createViewHolder(View rowView) {
    RowHolder rowHolder = new RowHolder();
    rowHolder.logMessage = (TextView) rowView.findViewById(R.id.logMessage);
    return rowHolder;
  }

  @TargetApi(16)
  private void fillViewHolder(final RowHolder viewHolder, final LogInfo logInfo, int position, int checkedItemPosition) {
    LogMessageUI logMessageUI = new LogMessageUI(logInfo.getLevel()).invoke();
    Drawable backgroundShape = logMessageUI.getBackgroundShape();

    viewHolder.logMessage.setTextColor(logMessageUI.getTextColor());
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      viewHolder.logMessage.setBackgroundDrawable(checkedItemPosition == position ? backgroundShape : null);
    } else {
      viewHolder.logMessage.setBackground(checkedItemPosition == position ? backgroundShape : null);
    }
    viewHolder.logMessage.setMaxLines(checkedItemPosition == position ? MAX_TEXT_LINES : DEFAULT_TEXT_LINES);
    viewHolder.logMessage.setText(logMessageUI.getLevelText() + " - " +
        DateFormatter.formatForDisplay(logInfo.getDatetime(), context) + "\n" + logInfo.getMessage());
  }

  static class RowHolder {
    TextView logMessage;
  }

  /**
   * Retrieve appropriate log level
   */
  private class LogMessageUI {
    private String levelText;
    private int textColor;
    private Drawable backgroundShape;

    public LogMessageUI(String levelText) {
      this.levelText = levelText;
    }

    public String getLevelText() {
      return levelText;
    }

    public int getTextColor() {
      return textColor;
    }

    public Drawable getBackgroundShape() {
      return backgroundShape;
    }

    public LogMessageUI invoke() {
      try {
        switch (levelText) {
          case "Error": {
            setLogView(R.color.log_error_text, R.drawable.log_error_shape, ERROR_LOG_LEVEL);
            break;
          }
          case "Warning": {
            setLogView(R.color.log_warning_text, R.drawable.log_warning_shape, WARNING_LOG_LEVEL);
            break;
          }
          case "Information": {
            setLogView(R.color.log_information_text, R.drawable.log_information_shape, INFORMATION_LOG_LEVEL);
            break;
          }
          case "Debug": {
            setLogView(R.color.log_debug_text, R.drawable.log_debug_shape, DEBUG_LOG_LEVEL);
            break;
          }
          case "Unknown": {
            setLogView(R.color.log_debug_text, R.drawable.log_debug_shape, UNKNOWN_LOG_LEVEL);
            break;
          }
          default: {
            setLogView(R.color.log_error_text, R.drawable.log_error_shape, UNKNOWN_LOG_LEVEL);
            break;
          }
        }
      } catch (NumberFormatException nfe) {
        DebugLogger.log(LogsAdapter.class.toString(), nfe);
        setLogView(R.color.log_error_text, R.drawable.log_error_shape, UNKNOWN_LOG_LEVEL);
      }
      return this;
    }

    private void setLogView(int textColor, int backgroundShape, String levelText) {
      this.textColor = context.getResources().getColor(textColor);
      this.backgroundShape = context.getResources().getDrawable(backgroundShape);
      this.levelText = levelText;
    }
  }
}
