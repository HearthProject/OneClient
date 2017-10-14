package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.modpack.curse.CurseDownloader;
import com.hearthproject.oneclient.api.modpack.curse.CurseImporter;
import com.hearthproject.oneclient.fx.nodes.CurseModpackTile;
import com.hearthproject.oneclient.util.BindUtil;

public class CursePackPane extends ProjectPane {
    public CursePackPane() {
        super("Get Modpacks", "modpacks.png", "modpack");
    }

    @Override
    public void bind() {
        BindUtil.bindMapping(projects, tiles.getItems(), project -> {
            Instance instance = new CurseImporter(project.getId()).create();
            return new CurseModpackTile(instance, (CurseDownloader) instance.getInstaller());
        });
    }
}
