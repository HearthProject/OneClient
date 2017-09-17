package com.hearthproject.oneclient.api;

public enum PackType {
	MANUAL,
	HEARTH,
	CURSE,
	MULTIMC;

	public static final PackType[] VALUES = values();

	public static PackType byName(String name) {
		for (PackType value : VALUES) {
			if (value.name().equalsIgnoreCase(name))
				return value;
		}
		return MANUAL;
	}
}
