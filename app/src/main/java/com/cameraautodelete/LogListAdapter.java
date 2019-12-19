package com.cameraautodelete;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;


public class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.ViewHolder>{
    private ArrayList<CameraImgPojo> listdata;
    Context mContext;

    // RecyclerView recyclerView;
    public LogListAdapter(Context context, ArrayList<CameraImgPojo> listdata) {
        this.listdata = listdata;
        mContext = context;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.item_deleted_logs, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CameraImgPojo obj = listdata.get(position);
        String[] parts = obj.mFilePath.split("/");
        final String fileName = parts[parts.length-1];
        String title = fileName+" has been deleted";
        String subTitle = "It was captured on"+obj.currentDate +" "+obj.currentTime+" and deleted after "+obj.deleteTimeDisplay;
        holder.text_title.setText(title);
        holder.text_sub_title.setText(subTitle);

    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView text_title, text_sub_title;

        public ViewHolder(View itemView) {
            super(itemView);
            this.text_title = (TextView) itemView.findViewById(R.id.text_title);
            this.text_sub_title = (TextView) itemView.findViewById(R.id.text_sub_title);
        }
    }
}
