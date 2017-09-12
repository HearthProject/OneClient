package com.hearthproject.oneclient.api;

public interface IInstallable {
	void install(Instance instance);

	PackType getType();
}

