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

package com.imgtec.hobbyist.utils;

import android.content.Context;
import android.os.Handler;

import com.imgtec.flow.ErrorType;
import com.imgtec.flow.client.core.NetworkException;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.flow.FlowEntities;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class that executes longer operations in background.
 */
public class BackgroundExecutor {

  public static final String BACKGROUND_THREAD_INTERRUPTED = "Background thread interrupted";
  private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private static Handler handler = new Handler();

  /**
   * Executes code provided in externalCall on the background thread and notifies listener about its returned result.
   * taskCode may be used to identify background calls when calling several of the same kind at once.
   */
  public static <RESULT> void submit(final ExternalCall<RESULT> externalCall,
                                     final Callbacks<RESULT> listener, final int taskCode, final Context appContext) {
    Callable<RESULT> call = new Callable<RESULT>() {
      @Override
      public RESULT call() throws Exception {
        return externalCall.submit();
      }
    };

    final Future<RESULT> result = executorService.submit(call);

    new Thread(new Runnable() {
      @Override
      public void run() {
        RESULT futureResult = null;
        try {
          futureResult = result.get();
        } catch (ExecutionException e) {
          showAndLogError(e, appContext);
          listener.onBackgroundExecutionResult(null, taskCode);
        } catch (InterruptedException e) {
          DebugLogger.log(getClass().getSimpleName(), BACKGROUND_THREAD_INTERRUPTED);
          Thread.currentThread().interrupt();
          listener.onBackgroundExecutionResult(null, taskCode);
        }
        listener.onBackgroundExecutionResult(futureResult, taskCode);
      }
    }).start();
  }

  private static void showAndLogError(ExecutionException e, Context appContext) {
    DebugLogger.log(BackgroundExecutor.class.getSimpleName(), e);
    if (isUnknownHostException(e)) {
      ActivitiesAndFragmentsHelper.showToast(appContext,
          ErrorHtmlLogger.log(ErrorType.FLOW_ERROR_NETWORK),
          handler);
    } else {
      ActivitiesAndFragmentsHelper.showToast(appContext,
          ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
          handler);
    }
  }

  private static boolean isUnknownHostException(ExecutionException e) {
    return e.getCause() instanceof NetworkException &&
        extractNetworkExceptionCause((NetworkException) e.getCause()) instanceof UnknownHostException;
  }

  private static Throwable extractNetworkExceptionCause(NetworkException ne) {
    if (ne.getCause() instanceof NetworkException) {
      return extractNetworkExceptionCause((NetworkException) ne.getCause());
    } else {
      return ne.getCause();
    }
  }

  /**
   * Executes code provided in externalRun on the background thread.
   */
  public static void execute(final ExternalRun externalRun) {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        externalRun.execute();
      }
    };
    executorService.execute(run);
  }

  /**
   * Interface to be implemented by classes willing to get notified about results of {@link com.imgtec.hobbyist.utils.ExternalCall}s
   * passed to the submit() method.
   */
  public interface Callbacks<RESULT> {
    /**
     * Called on the listener when background execution completes.
     * Result will be null if execution error occurred.
     * @param result
     * @param taskCode
     */
    void onBackgroundExecutionResult(RESULT result, int taskCode);
  }
}
