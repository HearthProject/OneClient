package com.hearthproject.oneclient.hearth.api.json;

import com.hearthproject.oneclient.hearth.api.HearthApi;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class User {

	public String accessToken;
	public String uuid;
	public String username;

	public List<Role> roles;

	public User(String uuid) {
		this.uuid = uuid;
	}

	public void save(File dir) {
		try {
			FileUtils.writeStringToFile(getFile(dir), HearthApi.GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getFile(File dir) {
		return new File(dir, uuid.toString() + ".json");
	}

	public static User readFromDisk(String uuid, File dir) throws IOException {
		User user = HearthApi.GSON.fromJson(FileUtils.readFileToString(new User(uuid).getFile(dir), StandardCharsets.UTF_8), User.class);
		return user;
	}

}
