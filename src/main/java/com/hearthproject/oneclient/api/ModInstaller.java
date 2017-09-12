package com.hearthproject.oneclient.api;

public abstract class ModInstaller implements IInstallable {

	protected transient HeathInstance instance;

	public abstract void install(Instance instance);

}
