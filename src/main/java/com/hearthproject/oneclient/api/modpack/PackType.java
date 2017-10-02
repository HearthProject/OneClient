package com.hearthproject.oneclient.api.modpack;

public enum PackType {
	MANUAL,
	HEARTH,
	CURSE,
	MULTIMC, TWITCH;

	public static final PackType[] VALUES = values();

	public static PackType byName(String name) {
		for (PackType value : VALUES) {
			if (value.name().equalsIgnoreCase(name))
				return value;
		}
		return MANUAL;
	}
}
