package com.cameraautodelete;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
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
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HomeActivity extends BaseActivity {

    Uri picUri;
    String mCurrentPhotoPath;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 107;
    private final static int IMAGE_RESULT = 200;
    MyImgListAdapter adapter;
    RecyclerView recyclerView;
    TextView text_empty;
    ImageView image_logs;
    ArrayList<CameraImgPojo> mList = new ArrayList<>();

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView image_back_arrow = findViewById(R.id.image_back_arrow);
        image_logs = findViewById(R.id.image_logs);
         text_empty = findViewById(R.id.text_empty);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        image_back_arrow.setVisibility(View.GONE);
        FloatingActionButton fab = findViewById(R.id.fab);
        TextView text_setting = (TextView)findViewById(R.id.text_setting);
        image_logs.setVisibility(View.VISIBLE);
        image_logs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, DeletedLogsActivity.class));
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //////
                permissions.add(CAMERA);
                permissions.add(WRITE_EXTERNAL_STORAGE);
                permissions.add(READ_EXTERNAL_STORAGE);
                permissionsToRequest = findUnAskedPermissions(permissions);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (permissionsToRequest.size() > 0){
                        requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
                    }else {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        // Ensure that there's a camera activity to handle the intent
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            try {
                                File photoFile =   createImageFile();
                                Uri photoURI = FileProvider.getUriForFile(HomeActivity.this, "com.cameraautodelete.provider", photoFile);
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                ActivityCompat.startActivityForResult(HomeActivity.this, takePictureIntent, IMAGE_RESULT, null);
                            } catch (Exception e) {
                                Log.e("zxfsdG", e.getMessage());
                            }
                        }
                    }
                }

//                startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
            }
        });


        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }

    public void open(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //IMAGE CAPTURE CODE
        startActivityForResult(intent, 0);
    }



    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
       /* File getImage = Environment.getExternalStorageDirectory();
//        String completePath = Environment.getExternalStorageDirectory() + "/" + "JPG_"+System.currentTimeMillis()+".jpg";

        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage, "JPG_"+System.currentTimeMillis()+".png"));
        }*/
       File mFile;
        try {
            mFile = createImageFile();
            outputFileUri = Uri.fromFile(mFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFileUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == Activity.RESULT_OK) {


            if (requestCode == IMAGE_RESULT) {
                Log.e("onActivityResult", "Path>>>>> "+mCurrentPhotoPath);
                Intent captureintent = new Intent(HomeActivity.this, CameraPreviewActivity.class);
                captureintent.putExtra("imgpath", mCurrentPhotoPath);
                startActivityForResult(captureintent, 12125);
            }else if(requestCode == 12125){
                refresh();
            }

        }

    }


    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;

        if (isCamera) return getCaptureImageOutputUri().getPath();
        else return getPathFromURI(data.getData());

    }

    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }

    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("pic_uri", picUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri");
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }





    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    void refresh(){
        ArrayList<CameraImgPojo> ansArrayListDummy = new ArrayList<>();
        ArrayList<CameraImgPojo> ansArrayList = new ArrayList<>();
        String mStoredImgsRes = PreferenceUtils.getInstance(this).getValue("imgs");
        if (!TextUtils.isEmpty(mStoredImgsRes)) {
            TypeToken<List<CameraImgPojo>> token = new TypeToken<List<CameraImgPojo>>() {
            };
            ansArrayList = new Gson().fromJson(mStoredImgsRes, token.getType());
            for (int i=0;i<ansArrayList.size();i++){
                if(!TextUtils.isEmpty(ansArrayList.get(i).mFilePath)){
                    File file = new File(ansArrayList.get(i).mFilePath);
                    if (file.exists()) {
                        ansArrayListDummy.add(ansArrayList.get(i));
                    }
                }
            }
            Log.e("REFRESHED_SIZE", ansArrayListDummy.size()+"");
        }
        mList = ansArrayListDummy;
        if(ansArrayListDummy!=null && ansArrayListDummy.size()>0){
            text_empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new MyImgListAdapter(this, mList);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }else {
            text_empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(HomeActivity.this);

        refresh();

    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(HomeActivity.this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.e("EVENTBUS", "Called");
        refresh();
    };
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent2 event) {
        Log.e("EVENTBUS2", "Called2");
        String filePath = event.imgPath;
        SaveToTheGalley(HomeActivity.this, filePath);
    };


    public static void SaveToTheGalley(Context context, String filePath)
    {

        File existingFile = new File(filePath);
        if(existingFile.exists()){
            String root = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
            ).toString();
            File myDir = new File(root + "/SmartDelete");
            myDir.mkdirs();
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmapImg = BitmapFactory.decodeFile(filePath,bmOptions);
            try {
                FileOutputStream out = new FileOutputStream(myDir);
                bitmapImg.compress(Bitmap.CompressFormat.JPEG, 90, out);

                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("#########","Saving.....");
            MediaScannerConnection.scanFile(context, new String[]{filePath.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        }

    }


}

