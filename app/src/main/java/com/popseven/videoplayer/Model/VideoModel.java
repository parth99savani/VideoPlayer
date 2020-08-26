package com.popseven.videoplayer.Model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class VideoModel implements Parcelable {

    private long id;
    private Uri data;
    private String title, duration;

    public VideoModel(long id, Uri data, String title, String duration) {
        this.id = id;
        this.data = data;
        this.title = title;
        this.duration = duration;
    }

    protected VideoModel(Parcel in) {
        id = in.readLong();
        data = in.readParcelable(Uri.class.getClassLoader());
        title = in.readString();
        duration = in.readString();
    }

    public static final Creator<VideoModel> CREATOR = new Creator<VideoModel>() {
        @Override
        public VideoModel createFromParcel(Parcel in) {
            return new VideoModel(in);
        }

        @Override
        public VideoModel[] newArray(int size) {
            return new VideoModel[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Uri getData() {
        return data;
    }

    public void setData(Uri data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeParcelable(data, i);
        parcel.writeString(title);
        parcel.writeString(duration);
    }
}
