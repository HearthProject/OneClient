package com.hearthproject.oneclient.api.base;

import com.hearthproject.oneclient.util.files.FileUtil;

import java.io.File;

public class FileData {

    private String file;
    private String hash;

    public FileData() {
    }

    public FileData(File file) {
        this(file.getName(), FileUtil.createMD5Hash(file));
    }


    private FileData(String file, String hash) {
        this.file = file;
        this.hash = hash;
    }

    public String getFile() {
        return file;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean matches(File file) {
        if (this.file != null && this.hash != null)
            return this.file.equals(file.getName()) && this.hash.equals(FileUtil.createMD5Hash(file));
        return false;
    }

    public boolean matches(FileData other) {
        return this.hash.equals(other.hash) && this.file.equals(other.file);
    }
}
