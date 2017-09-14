package com.hearthproject.oneclient.api;

public abstract class ModInstaller implements IInstallable {

	protected transient Instance instance;

	public abstract void install(Instance instance);

}
