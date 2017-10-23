package com.aliyun.sls.android.sdk.core;


import com.aliyun.sls.android.sdk.LogException;

import java.util.concurrent.Future;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class AsyncTask<T extends Result> {

    private Future<T> future;

    private ExecutionContext context;

    private volatile boolean canceled;

    /**
     * Cancel the task
     */
    public void cancel() {
        canceled = true;
        if (context != null) {
            context.getCancellationHandler().cancel();
        }
    }

    /**
     * Checks if the task is complete
     *
     * @return
     */
    public boolean isCompleted() {
        return future.isDone();
    }

    /**
     * Waits and gets the result.
     * @return
     * @throws LogException
     */
    public T getResult() throws LogException{
        try {
            T result = future.get();
            return result;
        } catch (Exception e) {
            throw new LogException("","",e.getCause(), "");
        }
    }

    public static AsyncTask wrapRequestTask(Future future, ExecutionContext context) {
        AsyncTask asynTask = new AsyncTask();
        asynTask.future = future;
        asynTask.context = context;
        return asynTask;
    }

    /**
     * Waits until the task is finished
     */
    public void waitUntilFinished() {
        try {
            future.get();
        } catch (Exception ignore) {
        }
    }

    /**
     * Gets the flag if the task has been canceled.
     */
    public boolean isCanceled() {
        return canceled;
    }
}
