package com.example.storagegallary;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Image {

    private String imageLink;
    private String imagePath;
    private Long timeStamp;

    public Image(){
    }

    public Image(String imageLink,String imagePath){
        this.imageLink=imageLink;
        this.imagePath=imagePath;
        timeStamp=new Date().getTime();
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Exclude
    public String getFormattedTime(){
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/mm/dd");
        Date date=new Date();
        date.setTime(timeStamp);
        return sdf.format(date);
    }
}
