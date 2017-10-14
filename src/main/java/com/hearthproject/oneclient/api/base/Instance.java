package com.hearthproject.oneclient.api.base;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.api.modpack.manual.ManualModInstaller;
import com.hearthproject.oneclient.json.JsonUtil;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.files.FileUtil;
import com.hearthproject.oneclient.util.files.ImageUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.MinecraftUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.hearthproject.oneclient.util.MiscUtil.checkCancel;

public class Instance {

    public String name;
    public String packVersion;
    public String gameVersion;
    public String forgeVersion;
    public ObservableList<ModInstaller> mods = FXCollections.observableArrayList();

    public transient SimpleBooleanProperty installing;
    public ModpackInstaller installer;

    public Instance(String name, ModpackInstaller installer) {
        this();
        this.name = name;
        this.installer = installer;
    }

    public Instance() {
        this.forgeVersion = "";
        installing = new SimpleBooleanProperty(false);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPackVersion(String packVersion) {
        this.packVersion = packVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public String getPackVersion() {
        return packVersion;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public String getForgeVersion() {
        return forgeVersion;
    }

    public void setForgeVersion(String forgeVersion) {
        this.forgeVersion = forgeVersion;
    }

    public File getDirectory() {
        return new File(Constants.INSTANCEDIR, getName());
    }

    public File getDataFile() {
        return new File(getDirectory(), "instance.json");
    }

    public File getModDirectory() {
        return FileUtil.findDirectory(getDirectory(), "mods");
    }

    public File getConfigDirectory() {
        return FileUtil.findDirectory(getDirectory(), "config");
    }

    public String getIcon() {
        File file = getIconFile();
        if (!file.exists()) {
            ImageUtil.createIcon(MiscUtil.parseLetters(getName()), file);
        }
        return file.toString();
    }

    public File getIconFile() {
        return new File(getDirectory(), "icon.png");
    }

    public ObservableList<ModInstaller> getMods() {
        return mods;
    }

    public boolean hasMod(String name) {
        return getMods().stream().anyMatch(m -> m.getName().equals(name));
    }

    public void install() {
        OneClientLogging.info("{}: Installing with {}", getName(), installer.toString());
        setInstalling(true);

        FileUtil.createDirectory(getDirectory());
        if (getDirectory().exists())
            InstanceManager.addInstance(this);
        if (checkCancel())
            return;
        if (installer != null)
            installer.install(this);
        if (checkCancel())
            return;
        try {
            MinecraftUtil.installMinecraft(this);
            if (checkCancel())
                return;
        } catch (Throwable throwable) {
            OneClientLogging.error(throwable);
        }
        save();
        setInstalling(false);

    }

    public void delete() {
        try {
            FileUtils.deleteDirectory(getDirectory());
            getDirectory().delete();
            InstanceManager.removeInstance(this);
        } catch (IOException e) {
            OneClientLogging.error(e);
        }
    }

    public void update() {
        if (installer != null)
            installer.update(this);
    }

    public void save() {
        JsonUtil.save(new File(getDirectory(), "instance.json"), toString());
    }

    @Override
    public String toString() {
        return JsonUtil.GSON.toJson(this);
    }

    public ModpackInstaller getInstaller() {
        return installer;
    }

    private static final FileFilter MOD_FILTER = FileFilterUtils.or(FileFilterUtils.suffixFileFilter(".jar.disabled"), FileFilterUtils.suffixFileFilter(".zip.disabled"), FileFilterUtils.suffixFileFilter(".jar"), FileFilterUtils.suffixFileFilter(".zip"));
    //walks mod directory and creates Mod objects for any not found
    //Remove Mod entries for files that are no longer available.
    public void verifyMods() {
        File modDir = getModDirectory();
        File[] mods = modDir.listFiles(MOD_FILTER);
        List<ModInstaller> newMods = Lists.newArrayList();
        if (mods == null || mods.length == 0) {
            this.mods.clear();
            return;
        }
        if (this.mods != null) {
            List<File> files = Lists.newArrayList(mods);
            List<ModInstaller> removal = Lists.newArrayList();
            for (ModInstaller mod : this.mods) {
                Collection<File> sorted = Collections2.filter(files, f -> {
                    if (f != null && mod != null && mod.getData() != null) {
                        return mod.getData().matches(f);
                    }
                    return false;
                });
                boolean match = false;
                for (File file : sorted) {
                    if (mod.equals(file))
                        match = true;
                }
                if (!match) {
                    removal.add(mod);
                }
            }
            this.mods.removeAll(removal);

            files.forEach(file -> {
                boolean match = false;
                Collection<ModInstaller> sorted = Collections2.filter(this.mods, m -> {
                    if (m != null) {
                        return m.getData().matches(file);
                    }
                    return false;
                });
                for (ModInstaller mod : sorted) {
                    if (mod.getData().matches(file)) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    ModInstaller mod = new ManualModInstaller(new FileData(file));
                    newMods.add(mod);
                }
            });
            this.mods.addAll(newMods);
        }
        this.save();
    }

    public boolean isInstalling() {
        return installing.get();
    }

    public SimpleBooleanProperty installingProperty() {
        return installing;
    }

    public void setInstalling(boolean installing) {
        this.installing.set(installing);
    }

    public String createLaunchScript() {
        StringBuilder builder = new StringBuilder();

        String mainClass = "mainClass" + "net.minecraft.launcherwrapper.Launcher" + "\n";
        builder.append(mainClass);

        //TODO applet class (wrapper thing?)
        //TODO unfinished
        return "";
    }

}