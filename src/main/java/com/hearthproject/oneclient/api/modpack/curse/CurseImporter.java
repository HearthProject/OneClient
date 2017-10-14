package com.hearthproject.oneclient.api.modpack.curse;

import com.hearthproject.oneclient.api.base.Instance;
import com.hearthproject.oneclient.api.modpack.IImporter;

public class CurseImporter implements IImporter {

    private String name;
    private int projectID;

    public CurseImporter(int projectID) {
        this.projectID = projectID;
    }

    public CurseImporter(String name, int projectID) {
        this(projectID);
        this.name = name;
    }

    @Override
    public Instance create() {
        return new Instance(name, new CurseDownloader(projectID));
    }


}
