package com.hearthproject.oneclient.util.files;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

public class ImageUtil {

	public static final Cache<String, Image> IMAGE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

	public static Image openImage(File file) {
		if (file == null)
			return null;
		Image image = IMAGE_CACHE.getIfPresent(file.getName());
		if (image == null) {
			try {
				image = new Image(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			IMAGE_CACHE.put(file.getName(), image);
		}
		return image;
	}

}
