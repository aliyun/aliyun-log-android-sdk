package com.aliyun.sls.android.sdk.core;


import com.aliyun.sls.android.sdk.ClientException;
import com.aliyun.sls.android.sdk.ServiceException;

import java.util.concurrent.ExecutionException;
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
     * @throws ClientException
     * @throws ServiceException
     */
    public T getResult() throws ClientException, ServiceException {
        try {
            T result = future.get();
            return result;
        } catch (InterruptedException e) {
            throw new ClientException(e.getMessage(), e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClientException) {
                throw (ClientException) cause;
            } else if (cause instanceof ServiceException) {
                throw (ServiceException) cause;
            } else {
                cause.printStackTrace();
                throw new ClientException("Unexpected exception!" + cause.getMessage());
            }
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
