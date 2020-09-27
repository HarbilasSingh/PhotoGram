package com.example.socialnetwork;

public class FindFriends {
    private String profileImage, Status, Full_Name;

    public FindFriends(){

    }

    public FindFriends(String profileImage, String status, String full_Name) {
        this.profileImage = profileImage;
        Status = status;
        Full_Name = full_Name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getStatus() {
        return Status;
    }
;
    public void setStatus(String status) {
        Status = status;
    }

    public String getFull_Name() {
        return Full_Name;
    }

    public void setFull_Name(String full_Name) {
        Full_Name = full_Name;
    }
}
