package com.myapplication.model;

import android.graphics.Bitmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by syang on 10/5/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkInfo implements Serializable {

    private String id = "";
    private String title = "";

    @JsonProperty("author")
    private String authorName = "";

    @JsonProperty("created_utc")
    private long createdUtc = 0;

    @JsonProperty("thumbnail")
    private String thumbNailUrl = "";

    @JsonProperty("num_comments")
    private int numberOfComments = 0;

    private String url = "";

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public long getCreatedUtc() {
        return createdUtc;
    }

    public String getThumbNailUrl() {
        return thumbNailUrl;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }
}
