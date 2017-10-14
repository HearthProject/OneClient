package com.hearthproject.oneclient.util;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class AsyncService<T> extends Service<T> {

    private Callable<T> function;

    public AsyncService(Callable<T> function) {
        this.function = function;
        this.setExecutor(Executors.newSingleThreadExecutor());
    }

    @Override
    protected AsyncTask<T> createTask() {
        return new AsyncTask<>(function);
    }

    public class AsyncTask<T> extends Task<T> {
        private Callable<T> function;

        public AsyncTask(Callable<T> function) {
            this.function = function;
        }

        @Override
        protected T call() throws Exception {
            return function.call();
        }
    }

}
