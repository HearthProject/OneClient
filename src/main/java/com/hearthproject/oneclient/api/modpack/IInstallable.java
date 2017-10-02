package com.hearthproject.oneclient.api.modpack;

public interface IInstallable {
	void install(Instance instance);

	PackType getType();
}

