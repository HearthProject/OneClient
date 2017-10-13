package com.hearthproject.oneclient.api.modpack.curse;

import com.hearthproject.oneclient.api.cmdb.Database;
import com.hearthproject.oneclient.api.modpack.IImporter;
import com.hearthproject.oneclient.api.modpack.Info;
import com.hearthproject.oneclient.api.modpack.Instance;

public class CurseImporter implements IImporter {

    private Database.Project project;

    public CurseImporter(int id) {
        this.project = Curse.DATABASE.getProject(id);
    }

    public CurseImporter(Database.Project project) {
        this.project = project;
    }

    @Override
    public Instance create() {
        String name = project.getTitle();

        if (name == null)
            return null;

        Instance instance = new Instance(name, project.getSite(), new CurseInstaller(project),
                new Info("popularity", project.getPopularity()),
                new Info("authors", project.getAuthors()),
//			new Info("categories", categories),
                new Info("downloads", project.getDownloads()),
                new Info("gameVersions", project.getVersions()),
                new Info("summary", project.getDesc()),
                new Info("icon-url", project.getIconURL())
        );
        return instance;
    }


}
