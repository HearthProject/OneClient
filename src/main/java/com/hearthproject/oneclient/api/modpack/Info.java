package com.hearthproject.oneclient.api.modpack;


public class Info {
	private boolean temp;
	private transient String key;
	private Object info;

	public Info(String key, Object info) {
		this(key, info, true);
	}

	public Info(String key, Object info, boolean temp) {
		this.temp = temp;
		this.key = key;
		this.info = info;
	}

	public boolean isKept() {
		return !isTemp();
	}

	public boolean isTemp() {
		return temp;
	}

	public String getKey() {
		return key;
	}

	public Object getInfo() {
		return info;
	}
}