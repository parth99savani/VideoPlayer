package com.popseven.videoplayer.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class VideoFolderModel{

    private String strFolder;
    private ArrayList<VideoModel> videopathList;

    public String getStrFolder() {
        return strFolder;
    }

    public void setStrFolder(String strFolder) {
        this.strFolder = strFolder;
    }

    public ArrayList<VideoModel> getVideopathList() {
        return videopathList;
    }

    public void setVideopathList(ArrayList<VideoModel> videopathList) {
        this.videopathList = videopathList;
    }
}
