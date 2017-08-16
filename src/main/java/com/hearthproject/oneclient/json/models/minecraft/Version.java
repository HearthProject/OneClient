package com.hearthproject.oneclient.json.models.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.util.OperatingSystem;

import java.io.File;
import java.util.List;
import java.util.Map;

//Based of the code from fabric-loom found here: https://github.com/FabricMC/fabric-loom
public class Version {
	public List<Library> libraries;
	public Map<String, Downloads> downloads;
	public AssetIndex assetIndex;
	public String mainClass, minecraftArguments;

	public class Downloads {
		public String url;
		public String sha1;
	}

	public class AssetIndex {
		public String id;
		public String sha1;
		public String url;
	}

	public class Library {
		public String name;
		public JsonObject natives;
		public JsonObject downloads;
		private Artifact artifact;
		public Rule[] rules;

		public String getURL() {
			String path;
			String[] parts = this.name.split(":", 3);
			path = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2] + getClassifier() + ".jar";
			return Constants.LIBRARIES_BASE + path;
		}

		public File getFile(File dir) {
			String[] parts = this.name.split(":", 3);
			return new File(dir, parts[0].replace(".", File.separator) + File.separator + parts[1] + File.separator + parts[2] + File.separator + parts[1] + "-" + parts[2] + getClassifier() + ".jar");
		}

		public String getFileName() {
			String[] parts = this.name.split(":", 3);
			return parts[1] + "-" + parts[2] + getClassifier() + ".jar";
		}

		public String getSha1() {
			if (this.downloads == null) {
				return "";
			} else if (this.downloads.getAsJsonObject("artifact") == null) {
				return "";
			} else if (this.downloads.getAsJsonObject("artifact").get("sha1") == null) {
				return "";
			} else {
				return this.downloads.getAsJsonObject("artifact").get("sha1").getAsString();
			}
		}

		public String getClassifier() {
			if (natives == null) {
				return "";
			} else {
				JsonElement element = natives.get(OperatingSystem.getOS().replace("${arch}", OperatingSystem.getArch()));
				if (element == null) {
					return "";
				}
				return "-" + element.getAsString().replace("\"", "");
			}
		}

		public boolean allowed() {
			if (this.rules == null || this.rules.length <= 0) {
				return true;
			}

			boolean success = false;
			for (Rule rule : this.rules) {
				if (rule.os != null && rule.os.name != null) {
					if (rule.os.name.equalsIgnoreCase(OperatingSystem.getOS())) {
						return rule.action.equalsIgnoreCase("allow");
					}
				} else {
					success = rule.action.equalsIgnoreCase("allow");
				}
			}
			return success;
		}

		public String getArtifactName() {
			if (artifact == null) {
				artifact = new Artifact(name);
			}
			if (natives != null) {
				JsonElement jsonElement = natives.get(OperatingSystem.getOS());
				if (jsonElement != null) {
					return artifact.getArtifact(jsonElement.getAsString());
				}
			}

			return artifact.getArtifact(artifact.classifier);
		}

		private class Artifact {
			private final String domain, name, version, classifier, ext;

			public Artifact(String name) {
				String[] splitedArtifact = name.split(":");
				int idx = splitedArtifact[splitedArtifact.length - 1].indexOf('@');
				if (idx != -1) {
					ext = splitedArtifact[splitedArtifact.length - 1].substring(idx + 1);
					splitedArtifact[splitedArtifact.length - 1] = splitedArtifact[splitedArtifact.length - 1].substring(0, idx);
				} else {
					ext = "jar";
				}
				this.domain = splitedArtifact[0];
				this.name = splitedArtifact[1];
				this.version = splitedArtifact[2];
				this.classifier = splitedArtifact.length > 3 ? splitedArtifact[3] : null;
			}

			public String getArtifact(String classifier) {
				String ret = domain + ":" + name + ":" + version;
				if (classifier != null && classifier.indexOf('$') > -1) {
					classifier = classifier.replace("${arch}", OperatingSystem.SYSTEM_ARCH);
				}
				if (classifier != null) {
					ret += ":" + classifier;
				}
				if (!"jar".equals(ext)) {
					ret += "@" + ext;
				}
				return ret;
			}

			public String getClassifier() {
				return classifier;
			}
		}
	}

	private class Rule {
		public String action;
		public OS os;

		private class OS {
			String name;
		}
	}
}
