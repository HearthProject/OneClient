package com.hearthproject.oneclient.util.curse;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CursePack {
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    private String title;
    private String icon;
    private String averageDownloads;
    private String totalDownloads;
    private String lastUpdated;
    private String created;
    private String version;

    private Future<String> getUrl;

    public CursePack(Element element) {
        this.title = element.select(".title a").text().trim().replaceAll("[^a-zA-Z0-9\\s]", "");
        this.icon = element.select(".screenshot a").attr("href");
        this.averageDownloads = element.select(".average-downloads").text();
        this.totalDownloads = element.select(".download-total").text();
        this.lastUpdated = element.select(".updated").first().text();
        this.created = element.select(".updated").last().text();
        this.version = element.select(".version").text();
        getUrl = executor.submit(() -> {
            String base = CurseUtils.CURSE_BASE + element.select(".title a").attr("href");
            Document doc =  CurseUtils.getHtml(base,"");
            Elements link = doc.select(".curseforge a");
            return "https:" + link.attr("href");
        });
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(title + "\n");
        builder.append(icon + "\n");
        builder.append(averageDownloads + "\n");
        builder.append(totalDownloads + "\n");
        builder.append(lastUpdated + "\n");
        builder.append(created + "\n");
        builder.append(version + "\n");
        return builder.toString();
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }


    public String getUrl() {
        try {
            return getUrl.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return "";
    }
}