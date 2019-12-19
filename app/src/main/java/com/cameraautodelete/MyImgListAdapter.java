package com.cameraautodelete;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;


public class MyImgListAdapter extends RecyclerView.Adapter<MyImgListAdapter.ViewHolder>{
    private ArrayList<CameraImgPojo> listdata;
    Context mContext;

    // RecyclerView recyclerView;
    public MyImgListAdapter(Context context, ArrayList<CameraImgPojo> listdata) {
        this.listdata = listdata;
        mContext = context;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.item_img, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CameraImgPojo obj = listdata.get(position);
        Glide.with(mContext)
                .load(Uri.fromFile(new File(obj.mFilePath)))
                .skipMemoryCache(true)
                .error(R.color.gray)
                .placeholder(R.color.gray)
                .into(holder.img);

//        Bitmap selectedImage = BitmapFactory.decodeFile(obj.mFilePath);
//        holder.img.setImageBitmap(selectedImage);
        Log.e("PATHHHHHHH", obj.mFilePath+ " AUTO "+obj.isAutodelete);
        File imgFile = new  File(obj.mFilePath);
       /* if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            holder.img.setImageBitmap(myBitmap);

        };*/
       holder.text_capture_time.setText(obj.currentDate+" "+obj.currentTime);
       if(obj.isAutodelete){
           holder.text_delete_time.setText(obj.deleteTimeDisplay);
           holder.text_delete_time.setVisibility(View.VISIBLE);
       }else {
           holder.text_delete_time.setVisibility(View.GONE);
       }

       holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent captureintent = new Intent(mContext, CameraPreviewActivity.class);
               captureintent.putExtra("imgpath", obj.mFilePath);
               captureintent.putExtra("flag", "update");
               captureintent.putExtra("isAutodelete", obj.isAutodelete);
               mContext.startActivity(captureintent);
           }
       });
       holder.image_share.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               shareImage(mContext, new File(obj.mFilePath));
           }
       });
    }
 // it will open all sharable app
    void shareImage(Context context, File filePath) {
        Uri photoURI = FileProvider.getUriForFile(context, "com.cameraautodelete.provider", filePath);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, photoURI);
        context.startActivity(Intent.createChooser(intent,"Share with..."));
    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img, image_share;
        public TextView text_delete_time, text_capture_time;

        public ViewHolder(View itemView) {
            super(itemView);
            this.img = (ImageView) itemView.findViewById(R.id.img);
            this.image_share = (ImageView) itemView.findViewById(R.id.image_share);
            this.text_delete_time = (TextView) itemView.findViewById(R.id.text_delete_time);
            this.text_capture_time = (TextView) itemView.findViewById(R.id.text_capture_time);
        }
    }
}
