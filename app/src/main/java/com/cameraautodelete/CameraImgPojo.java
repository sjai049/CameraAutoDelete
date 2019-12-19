package com.cameraautodelete;

public class CameraImgPojo {

    long id;
    String mFilePath = "";
    String deleteTimeDisplay = "";
    long deleteTime = 0;
    boolean isAutodelete  =false;
    String currentDate  ="";
    String currentTime  ="";
    boolean isSecondAlrm = false;

    public CameraImgPojo(long id, String mFilePath, long deleteTime, boolean isAutodelete, String deleteTimeDisplay,
                         String currentDate, String currentTime, boolean isSecondAlrm) {
        this.id = id;
        this.currentDate = currentDate;
        this.currentTime = currentTime;
        this.deleteTimeDisplay = deleteTimeDisplay;
        this.mFilePath = mFilePath;
        this.deleteTime = deleteTime;
        this.isAutodelete = isAutodelete;
        this.isSecondAlrm = isSecondAlrm;

    }
}
