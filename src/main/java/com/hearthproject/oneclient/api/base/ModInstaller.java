package com.hearthproject.oneclient.api.base;

import com.hearthproject.oneclient.api.modpack.IInstallable;
import com.hearthproject.oneclient.fx.nodes.ModTile;

public abstract class ModInstaller implements IInstallable {
    private FileData data;
    private PackType type;
    private String name;
    private transient String process;

    public ModInstaller(PackType type, FileData data) {
        this.type = type;
        this.data = data;
    }

    public abstract ModTile createTile(Instance instance);

    public abstract void install(Instance instance);

    public abstract void update(Instance instance);

    public abstract String getVersion();

    public void setProcess(String process) {
        this.process = process;
    }

    public void setType(PackType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setData(FileData data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getProcess() {
        return process;
    }

    @Override
    public PackType getType() {
        return type;
    }

    public FileData getData() {
        return data;
    }


}
