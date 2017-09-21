package com.hearthproject.oneclient.api;

public class ModpackInstaller implements IInstallable {

	protected PackType type;
	protected transient Instance instance;

	public ModpackInstaller(PackType type) {
		this.type = type;
	}

	public void install(Instance instance) {}

	public void update(Instance instance) {}

	@Override
	public PackType getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.name();
	}
}
