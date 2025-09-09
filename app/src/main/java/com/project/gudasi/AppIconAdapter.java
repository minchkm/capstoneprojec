package com.project.gudasi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppIconAdapter extends RecyclerView.Adapter<AppIconAdapter.ViewHolder> {

    private List<AppItem> appList;

    public AppIconAdapter(List<AppItem> appList) {
        this.appList = appList;
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // 무한 루프 효과
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // position을 appList.size()로 나눈 나머지로 고르게 배치
        int index = position % appList.size();
        AppItem item = appList.get(index);

        holder.imgIcon.setImageResource(item.getIconRes());
        holder.tvName.setText(item.getName());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_icon, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgAppIcon);
            tvName = itemView.findViewById(R.id.tvAppName);
        }
    }
}

