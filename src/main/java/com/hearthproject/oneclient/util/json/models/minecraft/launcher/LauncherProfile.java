package com.hearthproject.oneclient.util.json.models.minecraft.launcher;

import java.util.Map;

public class LauncherProfile {

	public String selectedProfile, clientToken, selectedUser;

	public Map<String, Profile> profiles;

	public Map<String, AuthenticationDatabase> authenticationDatabase;

	public static class Profile {

		public Profile(String name, String lastVersionId) {
			this.name = name;
			this.lastVersionId = lastVersionId;
		}

		public Profile() {
		}

		public String name;
		public String lastVersionId;
	}

	public class AuthenticationDatabase {
		String displayName, accessToken, userid, uuid, username;
	}
}
