package com.hearthproject.oneclient.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.mashape.unirest.http.Unirest;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class MiscUtil {

	public static boolean checksumEquals(File file, String checksum) {
		if (file == null || !file.exists()) {
			return false;
		}
		try {
			HashCode hash = Files.hash(file, Hashing.sha1());
			StringBuilder builder = new StringBuilder();
			for (Byte hashBytes : hash.asBytes()) {
				builder.append(Integer.toString((hashBytes & 0xFF) + 0x100, 16).substring(1));
			}
			return builder.toString().equals(checksum);
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
		return false;
	}

	public static boolean checksumEquals(File file, List<String> checksum) {
		if (file == null || !file.exists()) {
			return false;
		}
		try {
			HashCode hash = Files.hash(file, Hashing.sha1());
			StringBuilder builder = new StringBuilder();
			for (Byte hashBytes : hash.asBytes()) {
				builder.append(Integer.toString((hashBytes & 0xFF) + 0x100, 16).substring(1));
			}
			return checksum.contains(builder.toString());
		} catch (IOException e) {
			OneClientLogging.error(e);
		}
		return false;
	}

	public static int getResponseCode(URL url) throws IOException {
		HttpURLConnection huc = (HttpURLConnection) url.openConnection();
		huc.setRequestMethod("GET");
		huc.connect();
		int code = huc.getResponseCode();
		return code;
	}

	@FunctionalInterface
	public interface ThrowingRunnable {
		void run() throws Exception;
	}

	@FunctionalInterface
	public interface ThrowingConsumer<T> {
		void accept(T t) throws Exception;
	}

	@FunctionalInterface
	public interface ThrowingSupplier<T> {
		T get() throws Exception;
	}

	public static double round(double value, int precision) {
		int scale = (int) Math.pow(10, precision);
		return (double) Math.round(value * scale) / scale;
	}

	public static void runLaterIfNeeded(Runnable runnable) {
		if (Platform.isFxApplicationThread()) {
			runnable.run();
		} else {
			Platform.runLater(runnable);
		}
	}

	public static <T> Collector<T, ?, ObservableList<T>> toObservableList() {
		return Collector.of(
			FXCollections::observableArrayList,
			ObservableList::add,
			(l1, l2) -> {
				l1.addAll(l2);
				return l1;
			});
	}

	public static class ProviderProperty<T> extends ObjectProperty<T> {
		private Supplier<T> supplier;

		public ProviderProperty(Supplier<T> supplier) {
			this.supplier = supplier;
		}

		@Override
		public void bind(ObservableValue<? extends T> observableValue) {

		}

		@Override
		public void unbind() {

		}

		@Override
		public boolean isBound() {
			return false;
		}

		@Override
		public T get() {
			return supplier.get();
		}

		@Override
		public void set(T t) {}

		@Override
		public Object getBean() {
			return null;
		}

		@Override
		public String getName() {
			return "";
		}

		@Override
		public void addListener(ChangeListener<? super T> changeListener) {

		}

		@Override
		public void removeListener(ChangeListener<? super T> changeListener) {

		}

		@Override
		public void addListener(InvalidationListener invalidationListener) {

		}

		@Override
		public void removeListener(InvalidationListener invalidationListener) {

		}
	}

	public static String hastebin(String text) {

		try {
			String key = Unirest.post("https://hastebin.com/documents").body(text).asJson().getBody().getObject().getString("key");
			String url = "https://hastebin.com/" + key + ".hs";
			return url;

		} catch (Throwable e) {
			OneClientLogging.error(e);
		}
		return null;
	}

	public static void uploadLog(String text) {
		new Thread(() -> {
			String url = hastebin(text);
			OperatingSystem.browseURI(url);
		}).start();
	}

}
