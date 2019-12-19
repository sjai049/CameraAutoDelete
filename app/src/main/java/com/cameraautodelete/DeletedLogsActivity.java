package com.cameraautodelete;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
public class DeletedLogsActivity extends BaseActivity {

    Uri picUri;
    String mCurrentPhotoPath;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 107;
    private final static int IMAGE_RESULT = 200;
    LogListAdapter adapter;
    RecyclerView recyclerView;
    TextView text_empty;
    ImageView image_logs;
    ArrayList<CameraImgPojo> mList = new ArrayList<>();

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        ImageView image_back_arrow = findViewById(R.id.image_back_arrow);
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        image_logs = findViewById(R.id.image_logs);
         text_empty = findViewById(R.id.text_empty);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        toolbar_title.setText("Deleted Files");

        image_back_arrow.setVisibility(View.VISIBLE);
        image_back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        refresh();

    }


    void refresh(){
        ArrayList<CameraImgPojo> ansArrayListDummy = new ArrayList<>();
        ArrayList<CameraImgPojo> ansArrayList = new ArrayList<>();
        String mStoredImgsRes = PreferenceUtils.getInstance(this).getValue("deleted_imgs");
        if (!TextUtils.isEmpty(mStoredImgsRes)) {
            TypeToken<List<CameraImgPojo>> token = new TypeToken<List<CameraImgPojo>>() {
            };
            ansArrayList = new Gson().fromJson(mStoredImgsRes, token.getType());
            for(int i =0;i<ansArrayList.size();i++){
                long deletionTime = ansArrayList.get(i).deleteTime;
                long monthAddedTime = deletionTime + 2592000000l;
                if(monthAddedTime < System.currentTimeMillis()){

                }else {
                    ansArrayListDummy.add(ansArrayList.get(i));
                }
            }
            Log.e("REFRESHED_DELETE_SIZE", ansArrayList.size()+"");
        }
        Collections.reverse(ansArrayListDummy);
        mList = ansArrayListDummy;
        if(ansArrayListDummy!=null && ansArrayListDummy.size()>0){
            text_empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new LogListAdapter(this, mList);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            recyclerView.addItemDecoration(
                    new DividerItemDecoration(this, mLayoutManager.getOrientation()));
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }else {
            text_empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

    }

}

