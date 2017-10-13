package com.hearthproject.oneclient.fx.contentpane;


import com.google.common.collect.Lists;
import com.hearthproject.oneclient.api.cmdb.Database;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class PageService extends Service<List<Database.Project>> {
    private int pageSize;
    private Supplier<List<Database.Project>> projects;

    public PageService(int pageSize, Supplier<List<Database.Project>> projects) {
        this.pageSize = pageSize;
        this.projects = projects;
        this.setExecutor(Executors.newSingleThreadExecutor());
    }

    @Override
    protected Task<List<Database.Project>> createTask() {
        return new PageTask(pageSize, projects.get());
    }


    public class PageTask extends Task<List<Database.Project>> {

        private int pageSize;
        private List<Database.Project> projects;

        public PageTask(int pageSize, List<Database.Project> projects) {
            this.pageSize = pageSize;
            this.projects = projects;
        }

        @Override
        protected List<Database.Project> call() throws Exception {
            List<Database.Project> add = Lists.newArrayList();
            for (int i = 0; i < pageSize; i++) {
                add.add(projects.remove(0));
            }
            return add;
        }
    }
}