package com.hearthproject.oneclient.util.curse;

public class CursePackImporter {

	public static void importPacks() {

		//TODO REIMPLEMENT THIS
		//		DirectoryChooser directoryChooser = new DirectoryChooser();
		//		File selectedDirectory = directoryChooser.showDialog(Main.stage);
		//		new Thread(() -> {
		//			if (selectedDirectory != null && selectedDirectory.exists()) {
		//				File instancesDir = new File(selectedDirectory, "Instances");
		//				if (instancesDir.exists() && instancesDir.listFiles() != null) {
		//					Arrays.stream(instancesDir.listFiles()).filter(File::isDirectory).forEach(file -> {
		//						File minecraftinstance = new File(file, "minecraftinstance.json");
		//						if (minecraftinstance.exists()) {
		//							try {
		//								JsonObject jsonObject = JsonUtil.GSON.fromJson(FileUtils.readFileToString(minecraftinstance, StandardCharsets.UTF_8), JsonObject.class);
		//								String name = jsonObject.get("name").getAsString();
		//								String mcVersion = jsonObject.getAsJsonObject("baseModLoader").get("MinecraftVersion").getAsString();
		//								Instance instance = new Instance();
		//								instance.setName(name);
		//								instance.getInfo().setVersion(mcVersion);
		//								instance.getInfo().setModLoader("Forge");
		//								instance.getInfo().setModLoaderVersion(jsonObject.getAsJsonObject("baseModLoader").get("Name").getAsString().replace("forge-", ""));
		//								FileUtils.copyDirectory(file, instance.getDirectory());
		//								MinecraftUtil.installMinecraft(instance);
		//								InstanceManager.addInstance(instance);
		//								OneClientLogging.logger.info("Import of " + file.getName() + " was successful!");
		//							} catch (Throwable e) {
		//								OneClientLogging.logger.error(e);
		//								OneClientLogging.logger.info("Import of " + file.getName() + " failed!");
		//							}
		//						}
		//					});
		//				} else {
		//					OneClientLogging.logUserError(new FileNotFoundException("Invalid curse install directory"), "Invalid curse install directory");
		//				}
		//			} else {
		//				return;
		//			}
		//			OneClientLogging.logger.info("Done!");
		//			MiscUtil.runLaterIfNeeded(() -> {
		//				ContentPanes.INSTANCES_PANE.refresh();
		//				ContentPanes.INSTANCES_PANE.getButton().fire();
		//			});
		//		}).start();
	}
}
