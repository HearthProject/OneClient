package com.hearthproject.oneclient.api;

public class ModInstaller implements IInstallable {

	protected PackType type;
	protected transient Instance instance;

	public ModInstaller(PackType type) {
		this.type = type;
	}

	public void install(Instance instance) {}

	public void update(Instance instance) {}

	@Override
	public PackType getType() {
		return type;
	}
}
