package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.api.base.Instance;

public class CurseModPane extends ProjectPane {
    private Instance instance;

    public CurseModPane(Instance instance) {
        super("Curse Mods", "", "mod");
        this.instance = instance;
    }

    @Override
    public void bind() {
//        BindUtil.bindMapping(projects, tiles.getItems(), project -> );
    }
}
