package com.cameraautodelete;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationId = intent.getIntExtra("notificationId", 0);
        long picIdRecieved = 0l;
        String action = "";
//        if(intent.hasExtra("filepath") && intent.hasExtra("action")){
        String camId = intent.getStringExtra("camId");
        picIdRecieved = Long.parseLong(camId);
            action = intent.getStringExtra("action");
//        }
        Log.e("NOTIFICAION_ACTION","Recieveddddddd "+picIdRecieved+" ---- "+action);
        String mStoredImgsRes = PreferenceUtils.getInstance(context).getValue("imgs");
        ArrayList<CameraImgPojo> ansArrayList = new ArrayList<CameraImgPojo>();
        if (!TextUtils.isEmpty(mStoredImgsRes)) {
            TypeToken<List<CameraImgPojo>> token = new TypeToken<List<CameraImgPojo>>() {
            };
            ansArrayList = new Gson().fromJson(mStoredImgsRes, token.getType());
            Log.e("LIST_AVAILABLE", "List "+ansArrayList.toString()+" - ID "+picIdRecieved);
            // To retreive the data from database; if id is match then break
            for(int i =0;i<ansArrayList.size();i++){
                if(ansArrayList.get(i).id == picIdRecieved && new File(ansArrayList.get(i).mFilePath).exists()){
                        ansArrayList.get(i).isAutodelete = false;
                        Log.e("AUTODELETE_CANCELLED", "");
                        //Save
                        //save to gallery
                        updateInDb(ansArrayList.get(i).id, context);
                        EventBus.getDefault().post(new MessageEvent2(ansArrayList.get(i).mFilePath));
                    break;
                }
            }
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);

    }

    int updateInDb(long id, Context context){
        String mStoredImgsRes = PreferenceUtils.getInstance(context).getValue("imgs");
        ArrayList<CameraImgPojo> ansArrayList = new ArrayList<CameraImgPojo>();
        if (!TextUtils.isEmpty(mStoredImgsRes)) {
            TypeToken<List<CameraImgPojo>> token = new TypeToken<List<CameraImgPojo>>() {
            };
            ansArrayList = new Gson().fromJson(mStoredImgsRes, token.getType());
            for (int i=0;i<ansArrayList.size();i++){
                if(id == ansArrayList.get(i).id){
                    ansArrayList.get(i).isAutodelete = false;
                    return i;
                }
            }
        }
        PreferenceUtils.getInstance(context).setValue("imgs", new Gson().toJson(ansArrayList));
        return -1;
    }

}