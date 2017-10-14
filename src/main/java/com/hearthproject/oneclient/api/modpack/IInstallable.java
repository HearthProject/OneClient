package com.hearthproject.oneclient.api.modpack;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.base.PackType;

public interface IInstallable {
	void install(Instance instance);

	PackType getType();
}

