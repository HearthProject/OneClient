package com.hearthproject.oneclient.util;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;

public class AsyncTask<T> implements ListenableFuture<T>, Runnable {
	public ListenableFutureTask<T> task;
	public T t;

	public AsyncTask(Callable<T> callable) {
		task = ListenableFutureTask.create(callable);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return task.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return task.isCancelled();
	}

	@Override
	public boolean isDone() {
		return task.isDone();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		if (t != null)
			return t;
		return t = task.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return null;
	}

	public boolean isPresent() {
		try {
			return get() != null;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return false;
	}

	public <U> U map(Function<? super T, ? extends U> var1) {
		Objects.requireNonNull(var1);
		return this.isPresent() ? var1.apply(t) : null;
	}

	@Override
	public void addListener(Runnable listener, Executor executor) {
		task.addListener(listener, executor);
	}

	@Override
	public void run() {
		task.run();
	}
}
