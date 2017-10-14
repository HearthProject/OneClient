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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Supplier;

public class MiscUtil {

	public static String parseLetters(String name) {
		if (StringUtils.containsWhitespace(name)) {
			return WordUtils.initials(name);
		}
		return name.substring(0, Math.min(7, name.length()));
	}

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

	public static Hyperlink setupLink(Hyperlink hyperlink, String text, String url) {
		hyperlink.setText(text);
		hyperlink.setOnAction(event -> OperatingSystem.browseURI(url));
		return hyperlink;
	}

	public static Hyperlink createLink(String text, String url) {
		Hyperlink link = new Hyperlink();
		setupLink(link, text, url);
		return link;
	}

	private static String[] suffix = new String[] { "", "k", "m", "b", "t" };
	private static int MAX_LENGTH = 4;

	public static String formatNumbers(double number) {
		String r = new DecimalFormat("##0E0").format(number);
		r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
		while (r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")) {
			r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
		}
		return r;
	}

	public static boolean checkCancel() {
		if (Thread.currentThread().isInterrupted()) {
			return true;
		}
		return false;
	}

    public static Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }

}
