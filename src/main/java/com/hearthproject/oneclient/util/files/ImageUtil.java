package com.hearthproject.oneclient.util.files;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class ImageUtil {

	public static final Cache<String, Image> IMAGE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

	public static Image openImage(File file) {
		if (file == null || !file.exists()) {
			return null;
		}
		Image image = null;
		try {
			image = new Image(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			OneClientLogging.error(e);
		}
		return image;
	}

	public static Image openImage(InputStream stream) {
		return new Image(stream);
	}

	public static Image openCachedImage(File file) {
		OneClientLogging.logger.debug("Opening Image : {}", file);
		if (file == null || !file.exists()) {
			return null;
		}
		Image image = IMAGE_CACHE.getIfPresent(file.getName());
		if (image == null) {
			try {
				image = new Image(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				OneClientLogging.error(e);
			}
			if (image == null)
				return null;
			IMAGE_CACHE.put(file.getName(), image);
		}
		return image;
	}

	public static Image openCachedImage(InputStream inputStream, String name) {
		OneClientLogging.logger.debug("Opening Image : {}", name);
		Image image = IMAGE_CACHE.getIfPresent(name);
		if (image == null) {
			image = new Image(inputStream);
			IMAGE_CACHE.put(name, image);
		}
		return image;
	}

}
