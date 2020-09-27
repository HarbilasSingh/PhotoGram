package com.example.socialnetwork;

public class MyMessages {

    private String Full_Name, Status, profileImage;

    public MyMessages(){

    }

    public MyMessages(String full_Name, String status, String profileImage) {
        Full_Name = full_Name;
        Status = status;
        this.profileImage = profileImage;
    }

    public String getFull_Name() {
        return Full_Name;
    }

    public void setFull_Name(String full_Name) {
        Full_Name = full_Name;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
