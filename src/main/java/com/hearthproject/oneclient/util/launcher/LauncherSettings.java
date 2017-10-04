package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.util.files.JavaUtil;
import javafx.beans.property.SimpleStringProperty;

public class LauncherSettings {
	public boolean left_align_window_buttons = false;
	public boolean show_log_window = true;
	public boolean close_launcher_with_minecraft = false;

	public boolean tracking = true;

	public int minecraftMinMemory = 3072, minecraftMaxMemory = 3072;

	public String arguments = "-XX:+UseG1GC -Dsun.rmi.dgc.server.gcInterval=2147483646 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";

	public String wrapperCommand = "";

	public SimpleStringProperty javaPath = new SimpleStringProperty(JavaUtil.getDefault().path);

	public String getJavaPath() {
		return javaPath.get();
	}

	public SimpleStringProperty javaPathProperty() {
		return javaPath;
	}

	public void setJavaPath(String javaPath) {
		this.javaPath.set(javaPath);
	}
}
