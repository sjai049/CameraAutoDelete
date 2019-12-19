package com.cameraautodelete;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PersistableBundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
@SuppressLint("NewApi")
public class MyJobService extends JobService {
    private static final String TAG = MyJobService.class.getSimpleName();
    boolean isWorking = false;
    boolean jobCancelled = false;

    // Called by the Android system when it's time to run the job
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.e(TAG, "Job started!");
        isWorking = true;
        // We need 'jobParameters' so we can call 'jobFinished'
        startWorkOnNewThread(jobParameters); // Services do NOT run on a separate thread

        return isWorking;
    }

    private void startWorkOnNewThread(final JobParameters jobParameters) {
        //Background processing
        new Thread(new Runnable() {
            public void run() {
                doWork(jobParameters);
            }
        }).start();
    }

    private void doWork(JobParameters jobParameters) {

        long picId = jobParameters.getExtras().getLong("PIC_ID");
        Log.e("SERVICE_RECIEVED", "Callled for delete----- "+picId);
        deletePicInLocal(this, picId);
        Log.e(TAG, "Job finished!");
        isWorking = false;
        boolean needsReschedule = false;
        jobFinished(jobParameters, needsReschedule);
    }

    // Called if the job was cancelled before being finished
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e(TAG, "Job cancelled before being completed.");
        jobCancelled = true;
        boolean needsReschedule = isWorking;
        jobFinished(jobParameters, needsReschedule);
        return needsReschedule;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void deletePicInLocal(Context context, long picId) {
        PreferenceUtils mPreferenceUtils = PreferenceUtils.getInstance(context);

//        if (PreferenceUtils.getInstance(context).getValue("autodelete").equals("true")) {
        long deletionTimeActual = System.currentTimeMillis();//+ deleteTimeLocal
        ArrayList<CameraImgPojo> ansArrayList = new ArrayList<CameraImgPojo>();
        ArrayList<CameraImgPojo> arrayListDummy = ansArrayList;
        String mStoredImgsRes = PreferenceUtils.getInstance(context).getValue("imgs");
        long timeTodelete = System.currentTimeMillis() - 1000;
        Log.e("DELETEDSTORAGE", "Paths>>>>> "+mStoredImgsRes);
        if (!TextUtils.isEmpty(mStoredImgsRes)) {
            TypeToken<ArrayList<CameraImgPojo>> token = new TypeToken<ArrayList<CameraImgPojo>>() {
            };
            ansArrayList = new Gson().fromJson(mStoredImgsRes, token.getType());
            if (ansArrayList != null && ansArrayList.size() > 0) {
                for (int i = 0; i < ansArrayList.size(); i++) {
                    Log.e("DELETED_ITERATION", ansArrayList.get(i).deleteTime+"  "+System.currentTimeMillis()+ " AUTO "+ansArrayList.get(i).isAutodelete);
                    Log.e("DELETED_ITERATION", ""+(ansArrayList.get(i).deleteTime == System.currentTimeMillis() || (ansArrayList.get(i).deleteTime < System.currentTimeMillis())));
                    if (ansArrayList.get(i).isAutodelete && (ansArrayList.get(i).id == picId)){//(ansArrayList.get(i).deleteTime == System.currentTimeMillis() || (ansArrayList.get(i).deleteTime < System.currentTimeMillis()))) {
                        File file = new File(ansArrayList.get(i).mFilePath);
                        if (file.exists()) {
//                                long mDeletionTimeActual = ansArrayList.get(i).deleteTime + Util.mTimeB4ToNotifyMinus;
                            /* if(ansArrayList.get(i).isSecondAlrm){
                                ansArrayList.get(i).isSecondAlrm = false;
                                ansArrayList.get(i).deleteTime = System.currentTimeMillis() + Util.mTimeB4ToNotifyMinus;
                                //send pushNotification for ask user for delete? & put to next Max
                                Log.e("DELETED_FILE_NOTIASK", "filepaths>>>>> "+ansArrayList.get(i).mFilePath+ "ARRLIST "+new Gson().toJson(ansArrayList));
                                //put the next alarm for nexttime
                                makeAnotherAutodelete(context, ansArrayList.get(i));
                                pushNotification(context, "Pic is will delete after "+ansArrayList.get(i).deleteTimeDisplay+" Do you want to delete it?","Smart delete Alert", ansArrayList.get(i), true);
                                arrayListDummy.add(ansArrayList.get(i));
                            }else { */
                                ansArrayList.get(i).isSecondAlrm = false;
                                Log.e("DELETED_FILE_JUSTNOTI", "filepaths>>>>> "+file.getAbsolutePath());
                                pushNotification(context, "Pic was captured on "+ansArrayList.get(i).currentDate+" "+ansArrayList.get(i).currentTime+" & got AUTO deleted after "+ansArrayList.get(i).deleteTimeDisplay,"Pic has been deleted successfully", ansArrayList.get(i), false);
                                file.delete();
                                storeDeletedLogs(context, ansArrayList.get(i));
//                                Log.e("REMOVED_DB", ""+ansArrayList.get(i).id);
//                                arrayListDummy.remove(i);
                            //}
                        }else {
//                                file.delete();
//                                Log.e("DELETED_FILE", "NoExists>>>>> "+file.getAbsoluteFile().exists()+"   "+file.getAbsolutePath());
                        }
                    }else {
                        arrayListDummy.add(ansArrayList.get(i));
                    }
                }

                //Save
                PreferenceUtils.getInstance(context).setValue("imgs", new Gson().toJson(arrayListDummy));
                Log.e("REMAINING_FILES", "Total "+arrayListDummy.size());
                EventBus.getDefault().post(new MessageEvent());
//                int deletedImgs = arrayListDummy.size() - arrayListDummy.size();
//                Toast.makeText(context, "Pic has been AUTO-deleted successfully", Toast.LENGTH_LONG).show();
            }
//            }
        }
    }

    void storeDeletedLogs(Context context, CameraImgPojo mCameraImgPojo){
        ArrayList<CameraImgPojo> deletedArrayList = new ArrayList<CameraImgPojo>();
        String mDeletedImgsRes = PreferenceUtils.getInstance(context).getValue("deleted_imgs");
        if(!TextUtils.isEmpty(mDeletedImgsRes)){
            TypeToken<ArrayList<CameraImgPojo>> token = new TypeToken<ArrayList<CameraImgPojo>>() {
            };
            deletedArrayList = new Gson().fromJson(mDeletedImgsRes, token.getType());
        }
        deletedArrayList.add(mCameraImgPojo);
        //covert array into string (for delete image)
        PreferenceUtils.getInstance(context).setValue("deleted_imgs", new Gson().toJson(deletedArrayList));
        Log.e("DELETED_SIZE", "ALARMDELETEDSIZE "+deletedArrayList.size());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void pushNotification(Context mContext,
                          String message, String title, CameraImgPojo cameraImgPojo, Boolean showBtns
    ) {

        Log.e("NOTIFICATION", "Showing push notification");
        Intent mIntent = new Intent(mContext, HomeActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int notificationID = (int) Math.random();
        Long[] longArray = new Long[1000];
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationID, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String channelId = "channel";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, channelId)
                .setSmallIcon(R.drawable.icon)
//            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setContentTitle(Html.fromHtml(title))
                .setContentText(Html.fromHtml(message)) // <<commented
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setAutoCancel(true)
                .setLights(Color.MAGENTA, 500, 500)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setVibrate(longArray)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(title));


//        } else {
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(message))); //<<commented
//        }
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(channelId, "Projects Related", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }
        Log.e("NOTIFICATION", "Success "+cameraImgPojo.mFilePath);

//        mBuilder.setContentIntent(pendingIntent);
//        mNotificationManager.notify(notificationID, mBuilder.build());


        String filePath = String.valueOf(cameraImgPojo.id);

        if(showBtns){
//            Intent yesbuttonIntent = new Intent(mContext, NotificationReceiver.class);
//            yesbuttonIntent.putExtra("filepath", filePath);
//            yesbuttonIntent.putExtra("action", "yes");
//            yesbuttonIntent.putExtra("notificationId", notificationID);
//            PendingIntent mYesIntent = PendingIntent.getBroadcast(mContext, 0, yesbuttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            Intent nobuttonIntent = new Intent(mContext, NotificationReceiver.class);
            nobuttonIntent.putExtra("camId", filePath);
            nobuttonIntent.putExtra("action", "no");
            nobuttonIntent.putExtra("notificationId", notificationID);
            PendingIntent mNoIntent = PendingIntent.getBroadcast(mContext, 0, nobuttonIntent, 0);
            mBuilder.setContentIntent(pendingIntent);
//            mBuilder.addAction(android.R.drawable.ic_menu_view, "YES", mYesIntent);
            mBuilder.addAction(android.R.drawable.ic_delete, "Don't delete", mNoIntent);
        }else {
            mBuilder.setContentIntent(pendingIntent);
        }


        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, mBuilder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void makeAnotherAutodelete(Context context, CameraImgPojo cameraImgPojo){
        long deletionTimeActual = System.currentTimeMillis() + Util.mTimeB4ToNotifyMinus;
        ArrayList<CameraImgPojo> ansArrayList = new ArrayList<CameraImgPojo>();
        String mStoredImgsRes = PreferenceUtils.getInstance(context).getValue("imgs");
        if (!TextUtils.isEmpty(mStoredImgsRes)) {
            TypeToken<List<CameraImgPojo>> token = new TypeToken<List<CameraImgPojo>>() {
            };
            ansArrayList = new Gson().fromJson(mStoredImgsRes, token.getType());
            for(int i =0;i<ansArrayList.size();i++){
                if(ansArrayList.get(i).mFilePath.equalsIgnoreCase(cameraImgPojo.mFilePath)){
                    ansArrayList.get(i).isAutodelete = cameraImgPojo.isAutodelete;
                    ansArrayList.get(i).deleteTime = deletionTimeActual;
                    ansArrayList.get(i).deleteTimeDisplay = cameraImgPojo.deleteTimeDisplay;
                    Log.e("RECORD_UPDATED", "at "+i);
                    break;
                }
            }
            //Save
            PreferenceUtils.getInstance(context).setValue("imgs", new Gson().toJson(ansArrayList));
        }

        //start
        /*int id = (int)System.currentTimeMillis();
        Intent intent = new Intent(context, MyBroadcastReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, id, intent, PendingIntent.FLAG_ONE_SHOT
        );
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                    *//*System.currentTimeMillis()
                            + (10 * 1000)*//*deletionTimeActual, pendingIntent
        );
        Log.e("ALARMSET", "ID is "+id);*/

        //Job Intent service
        PersistableBundle bundle = new PersistableBundle();
        bundle.putLong("PIC_ID", cameraImgPojo.id);
        int jobId = (int)System.currentTimeMillis();
        JobScheduler jobScheduler = (JobScheduler)context.getApplicationContext()
                .getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(context,
                MyJobService.class);
        JobInfo jobInfoObj = new JobInfo.Builder(jobId, componentName)
                .setExtras(bundle)
                .setPersisted(true)
                .setMinimumLatency(Util.mTimeB4ToNotifyMinus)
                .build();
        int successflag = jobScheduler.schedule(jobInfoObj);
        if(successflag == JobScheduler.RESULT_SUCCESS){
            Log.e("$$$$$$","Job scheduled successfully for "+Util.mTimeB4ToNotifyMinus);
        }
    }

}