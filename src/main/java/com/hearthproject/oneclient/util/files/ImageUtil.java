package com.hearthproject.oneclient.util.files;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ImageUtil {

	public static final Cache<String, Image> IMAGE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

	public static void downloadFromURL(String url, File location) {
		try {
			downloadFromURL(new URL(url), location);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public static void downloadFromURL(URL url, File location) {
		try {
			Files.write(Resources.toByteArray(url), location);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Image openImage(File file) {
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
