package com.cameraautodelete;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Camera;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CameraPreviewActivity extends BaseActivity {

    String imgpath = "";
    ImageView imgview;
    ImageView iamge_delete;
    ImageView image_back_arrow;
    TextView text_save;
    TextView text_setting;
    boolean isAutodelete = false;
    boolean isUpdateFlag = false;
    String[] deleteTimeList = {"Select Time", "1 Min", "5 Min", "10 Min", "15 Min", "20 Min", "25 Min", "1 Hour", "2 Hours", "4 Hours", "6 Hours", "12 Hours", "1 Day", "2 Days", "3 Days", "4 Days", "5 Days", "6 Days"
            , "1 Week"
            , "8 Days"
            , "9 Days"
            , "10 Days"
            , "11 Days"
            , "12 Days"
            , "13 Days"
            , "14 Days"
            , "15 Days"
    };
    String[] deleteTimeMillisList = {"0","60000", "300000","600000", "900000", "1200000", "1500000", "3600000", "7200000", "14400000", "21600000", "43200000", "86400000","172800000","259200000","345600000","432000000","518400000"
            ,"604800000"
            ,"691200000"
            ,"777600000"
            ,"864000000"
            ,"950400000"
            ,"1036800000"
            ,"1123200000"
            ,"1209600000"
            ,"1296000000"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        text_setting = (TextView) findViewById(R.id.text_setting);
        text_save = (TextView) findViewById(R.id.text_save);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        imgview = (ImageView)findViewById(R.id.imgview);
        image_back_arrow = (ImageView)findViewById(R.id.image_back_arrow);
        iamge_delete = (ImageView)findViewById(R.id.iamge_delete);

        text_setting.setVisibility(View.GONE);

        toolbar_title.setText("Preview");
        if(getIntent().hasExtra("imgpath")){
            imgpath = getIntent().getExtras().getString("imgpath");
            Glide.with(CameraPreviewActivity.this)
                    .load(Uri.fromFile(new File(imgpath)))
                    .skipMemoryCache(true)
                    .error(R.color.grey_text)
                    .placeholder(R.color.grey_text)
                    .into(imgview);

        }
        if(getIntent().hasExtra("flag")){
            if(getIntent().getStringExtra("flag").equalsIgnoreCase("update")){
                isUpdateFlag = true;
            }else {
                isUpdateFlag = false;
            }
        }
        if(getIntent().hasExtra("isAutodelete")){
            if(getIntent().getBooleanExtra("isAutodelete", false)){
                isAutodelete = true;
            }else {
                isAutodelete = false;
            }
        }



        image_back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        iamge_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(imgpath);
                if (file.exists()) {
                    Log.e("DELETED_FILE", "filepaths>>>>> "+file.getAbsolutePath());
                    file.delete();
                }
                ArrayList<CameraImgPojo> ansArrayList = new ArrayList<>();
                ArrayList<CameraImgPojo> ansArrayListDummy = new ArrayList<>();
                String mStoredImgsRes = PreferenceUtils.getInstance(CameraPreviewActivity.this).getValue("imgs");
                if (!TextUtils.isEmpty(mStoredImgsRes)) {
                    TypeToken<List<CameraImgPojo>> token = new TypeToken<List<CameraImgPojo>>() {
                    };
                    ansArrayList = new Gson().fromJson(mStoredImgsRes, token.getType());
                    if(ansArrayList!=null && ansArrayList.size()>0){
                        for (int i=0;i<ansArrayList.size();i++){
                            if(!ansArrayList.get(i).mFilePath.equalsIgnoreCase(imgpath)){
                                ansArrayListDummy.add(ansArrayList.get(i));
                            }
                        }
                        PreferenceUtils.getInstance(CameraPreviewActivity.this).setValue("imgs", new Gson().toJson(ansArrayListDummy));
                        Toast.makeText(CameraPreviewActivity.this, "Deleted sucessfully", Toast.LENGTH_LONG).show();
                    }
                }
                EventBus.getDefault().post(new MessageEvent());
                Intent intent = new Intent(CameraPreviewActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                setResult(RESULT_OK ,intent);
                startActivity(intent);
                finish();
            }
        });
        text_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingPopup();
            }
        });


    }

    void showSettingPopup(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_custom_setting_popup, null);
        dialogBuilder.setView(dialogView);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        CheckBox mCheckBox = (CheckBox) dialogView.findViewById(R.id.chkbox_autodelete);
        TextView text_submit = (TextView) dialogView.findViewById(R.id.text_submit);
        Spinner spiner_delete_time = (Spinner) dialogView.findViewById(R.id.spiner_delete_time);
        text_submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                storePicInLocal(imgpath);
                Intent intent = new Intent();
                setResult(RESULT_OK ,intent);
                finish();
            }
        });
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    isAutodelete = true;
                }else {
                    isAutodelete = false;
                }
            }
        });
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, deleteTimeList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiner_delete_time.setAdapter(arrayAdapter);
        spiner_delete_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PreferenceUtils.getInstance(CameraPreviewActivity.this).setValue("deleteTimeSelect", deleteTimeList[position]);
                PreferenceUtils.getInstance(CameraPreviewActivity.this).setValue("deleteTime", deleteTimeMillisList[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(isAutodelete){
            mCheckBox.setChecked(true);
        }else{
            mCheckBox.setChecked(false);
        }
        int selectedPos = getDeletePos();
        spiner_delete_time.setSelection(selectedPos);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2020){
            if(data!=null && data.getBooleanExtra("isautodelete", false)){
               isAutodelete = true;
            }else {
                isAutodelete = false;
            }
        }
    }

    void storeLogs(){

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void storePicInLocal(String filePath){
        long picId = System.currentTimeMillis();
        boolean secondAlrmNeeded = false;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat datef = new SimpleDateFormat("dd MMM yyyy");
        SimpleDateFormat timef = new SimpleDateFormat("hh:mm a");
        String date = datef.format(c.getTime());
        String time = timef.format(c.getTime());

        String isAutoDelete = PreferenceUtils.getInstance(this).getValue("autodelete");
        Log.e("IS_AUTODELETE", isAutoDelete);

//        if(isAutoDelete.equalsIgnoreCase("true")){
        String deleteTime = "60000";
        long deleteTimeLocal = 0;
        String deleteTimeDisplay = PreferenceUtils.getInstance(this).getValue("deleteTimeSelect");
        deleteTime = PreferenceUtils.getInstance(this).getValue("deleteTime");
        if(!TextUtils.isEmpty(deleteTime)){
            deleteTimeLocal = Long.parseLong(deleteTime);
        }else {
            deleteTime = "60000";
            deleteTimeLocal = Long.parseLong(deleteTime);
        }
        //minus the timeB4Notify so that we get alarm set before actual one if time is more that max
        if (deleteTimeLocal >= Util.mMaxTimeTosetNotifyDelete) {
            deleteTimeLocal = deleteTimeLocal - Util.mTimeB4ToNotifyMinus;
            secondAlrmNeeded = true;
            Log.e("SECOND_ALRM_NEEDED", ""+secondAlrmNeeded+" Seton "+deleteTimeLocal);
        }

        long deletionTimeActual = System.currentTimeMillis() + deleteTimeLocal;
        ArrayList<CameraImgPojo> ansArrayList = new ArrayList<CameraImgPojo>();
        String mStoredImgsRes = PreferenceUtils.getInstance(this).getValue("imgs");
        if (!TextUtils.isEmpty(mStoredImgsRes)) {
            TypeToken<List<CameraImgPojo>> token = new TypeToken<List<CameraImgPojo>>() {
            };
            ansArrayList = new Gson().fromJson(mStoredImgsRes, token.getType());

            if(!TextUtils.isEmpty(deleteTime) && TextUtils.isDigitsOnly(deleteTime)){
                if(!isUpdateFlag){
                    ansArrayList.add(new CameraImgPojo(picId, filePath, deletionTimeActual, isAutodelete, deleteTimeDisplay, date, time, secondAlrmNeeded));
                    Log.e("RECORD_ADDED", "on "+date+" -- "+time+"  PATH-- "+filePath);
                }else {
                    for(int i =0;i<ansArrayList.size();i++){
                        if(ansArrayList.get(i).mFilePath.equalsIgnoreCase(imgpath)){
                            ansArrayList.get(i).isAutodelete = isAutodelete;
                            ansArrayList.get(i).deleteTime = deletionTimeActual;
                            ansArrayList.get(i).deleteTimeDisplay = deleteTimeDisplay;
                            ansArrayList.get(i).id = picId;
                            Log.e("RECORD_UPDATED", "at "+i);
                            break;
                        }
                    }
                }
            }
        }else{
            deleteTimeLocal = Long.parseLong(deleteTime);
            deletionTimeActual = System.currentTimeMillis() + deleteTimeLocal;
            if(!isUpdateFlag){
                Log.e("RECORD_ADDED", "on "+date+" -- "+time+"  PATH-- "+filePath);
                ansArrayList.add(new CameraImgPojo(picId, filePath, deletionTimeActual, isAutodelete, deleteTimeDisplay, date, time, secondAlrmNeeded));
            }else {
                for(int i =0;i<ansArrayList.size();i++){
                    if(ansArrayList.get(i).mFilePath.equalsIgnoreCase(imgpath)){
                        ansArrayList.get(i).isAutodelete = isAutodelete;
                        Log.e("RECORD_UPDATED", "on "+date+" -- "+time);
                        break;
                    }
                }
            }
//            ansArrayList.add(new CameraImgPojo(filePath, deletionTimeActual, isAutodelete));
        }
        //start
        if(isAutodelete){
            int id = (int)System.currentTimeMillis();

            //Job Inent service-----ALARM
            PersistableBundle bundle = new PersistableBundle();
            bundle.putLong("PIC_ID", picId);

            int jobId = (int)System.currentTimeMillis();
            JobScheduler jobScheduler = (JobScheduler)getApplicationContext()
                    .getSystemService(JOB_SCHEDULER_SERVICE);
            ComponentName componentName = new ComponentName(CameraPreviewActivity.this,
                    MyJobService.class);
            JobInfo jobInfoObj = new JobInfo.Builder(jobId, componentName)
                    .setExtras(bundle)
                    .setPersisted(true)
                    .setMinimumLatency(deleteTimeLocal)
                    .build();
            int successflag = jobScheduler.schedule(jobInfoObj);
            if(successflag == JobScheduler.RESULT_SUCCESS){
                Log.e("$$$$$$","Job scheduled successfully for "+picId);
            }
        }
        //Save
        PreferenceUtils.getInstance(this).setValue("imgs", new Gson().toJson(ansArrayList));
        if(isAutodelete)
        Toast.makeText(this, "Pic will AUTO-delete after "+deleteTimeDisplay, Toast.LENGTH_LONG).show();

    }

    int getDeletePos(){
        int index = 0;
        for(int i=0;i<deleteTimeList.length;i++){
            if(deleteTimeList[i].equalsIgnoreCase(PreferenceUtils.getInstance(CameraPreviewActivity.this).getValue("deleteTimeSelect"))){
                return i;
            }
        }
        return index;
    }


}
