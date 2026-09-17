package com.vo.adertechaudioapp_v1.localAudio;

public class AudioModel {
    private final String title;
    private final String path;
    private final String duration;
    private final String folderName;
    private final String albumArtUri; // Поле для URI обложки

    public AudioModel(String title, String path, String duration, String folderName, String albumArtUri) {
        this.title = title;
        this.path = path;
        this.duration = duration;
        this.folderName = folderName;
        this.albumArtUri = albumArtUri;
    }

    public String getTitle() { return title; }

    public String getPath() { return path; }

    public String getDuration() { return duration; }

    public String getFolderName() { return folderName; }

    public String getAlbumArtUri() { return albumArtUri; }
}
