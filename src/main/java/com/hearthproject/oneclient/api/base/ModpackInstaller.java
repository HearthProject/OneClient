package com.hearthproject.oneclient.api.base;

import com.hearthproject.oneclient.api.modpack.IInstallable;

public class ModpackInstaller implements IInstallable {

    protected FileData data;
    protected PackType type;
    protected transient Instance instance;

    public ModpackInstaller(PackType type) {
        this.type = type;
    }

    public void install(Instance instance) {
    }

    public void update(Instance instance) {
    }

    @Override
    public PackType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.name();
    }

    public void setData(FileData data) {
        this.data = data;
    }

    public FileData getData() {
        return data;
    }
}
