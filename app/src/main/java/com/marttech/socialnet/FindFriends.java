package com.marttech.socialnet;

public class FindFriends {

    public String profileimage,fullname,status;

    public FindFriends(){

    }

    public FindFriends(String profileImage, String fullname, String status) {
        this.profileimage = profileImage;
        this.fullname = fullname;
        this.status = status;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
