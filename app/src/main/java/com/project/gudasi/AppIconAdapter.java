package com.project.gudasi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppIconAdapter extends RecyclerView.Adapter<AppIconAdapter.ViewHolder> {

    private List<AppItem> appList;
    private int[] itemsPerRow = {4, 6, 5};
    private int totalItems;

    public AppIconAdapter(List<AppItem> appList) {
        this.appList = appList;
        totalItems = 0;
        for (int n : itemsPerRow) totalItems += n;
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // 무한루프 효과
    }

    private int getRowForPosition(int position) {
        int pos = position % totalItems;
        int sum = 0;
        for (int i = 0; i < itemsPerRow.length; i++) {
            sum += itemsPerRow[i];
            if (pos < sum) return i;
        }
        return 0;
    }

    private int getIndexInRow(int position) {
        int pos = position % totalItems;
        int sum = 0;
        for (int i = 0; i < itemsPerRow.length; i++) {
            int prevSum = sum;
            sum += itemsPerRow[i];
            if (pos < sum) return pos - prevSum;
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int row = getRowForPosition(position);
        int indexInRow = getIndexInRow(position);
        int globalIndex = (row * 10 + indexInRow) % appList.size();

        AppItem item = appList.get(globalIndex);
        holder.imgIcon.setImageResource(item.getIconRes());
        holder.tvName.setText(item.getName() + " [" + (row + 1) + "줄]");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_icon, parent, false);
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
