package com.example.socialnetwork;

public class Comments {

    private String  ComUserImage, ComUserName, Comment, ComTime, ComDate;

    public Comments(){

    }

    public Comments(String comDate, String comTime, String comUserImage, String comUserName, String comment) {
        ComDate = comDate;
        ComTime = comTime;
        ComUserImage = comUserImage;
        ComUserName = comUserName;
        Comment = comment;
    }

    public String getComDate() {
        return ComDate;
    }

    public void setComDate(String comDate) {
        ComDate = comDate;
    }

    public String getComTime() {
        return ComTime;
    }

    public void setComTime(String comTime) {
        ComTime = comTime;
    }

    public String getComUserImage() {
        return ComUserImage;
    }

    public void setComUserImage(String comUserImage) {
        ComUserImage = comUserImage;
    }

    public String getComUserName() {
        return ComUserName;
    }

    public void setComUserName(String comUserName) {
        ComUserName = comUserName;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }
}
