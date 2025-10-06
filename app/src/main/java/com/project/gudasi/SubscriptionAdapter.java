package com.project.gudasi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private List<Subscription> subscriptionList;

    public SubscriptionAdapter(List<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription, parent, false);
        return new SubscriptionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        Subscription sub = subscriptionList.get(position);
        holder.name.setText(sub.getServiceName());
        holder.price.setText(sub.getRenewalPrice());

        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = originalFormat.parse(sub.getDate());
            SimpleDateFormat newFormat = new SimpleDateFormat("M월 d일", Locale.getDefault());
            holder.date.setText(newFormat.format(date));
        } catch (Exception e) {
            holder.date.setText(sub.getDate()); // 파싱 실패시 원래 날짜 표시
        }


        // 아이콘 추가
        String serviceName = sub.getServiceName().toLowerCase();
        if (serviceName.contains("netflix")) {
            holder.icon.setImageResource(R.drawable.ic_netflix);
        } else if (serviceName.contains("youtube")) {
            holder.icon.setImageResource(R.drawable.ic_youtube_music);
        } else if (serviceName.contains("spotify")) {
            holder.icon.setImageResource(R.drawable.ic_spotify);
        } else if (serviceName.contains("coupang")) {
            holder.icon.setImageResource(R.drawable.ic_coupang);
        } else if (serviceName.contains("disney")) {
            holder.icon.setImageResource(R.drawable.ic_disney_plus);
        }else if (serviceName.contains("tving")) {
            holder.icon.setImageResource(R.drawable.ic_tving);
        }else if (serviceName.contains("wavve")) {
            holder.icon.setImageResource(R.drawable.ic_wavve);
        }else if (serviceName.contains("watcha")) {
            holder.icon.setImageResource(R.drawable.ic_watcha);
        }else if (serviceName.contains("apple tv")) {
            holder.icon.setImageResource(R.drawable.ic_apple_tv);
        }else if (serviceName.contains("emoticon")) {
            holder.icon.setImageResource(R.drawable.ic_kakao_emoticon);
        }else if (serviceName.contains("bugs")) {
            holder.icon.setImageResource(R.drawable.ic_bugs);
        }else if (serviceName.contains("genie")) {
            holder.icon.setImageResource(R.drawable.ic_genie);
        }else if (serviceName.contains("flo")) {
            holder.icon.setImageResource(R.drawable.ic_flo);

        }
        else {
            holder.icon.setImageResource(R.drawable.ic_default_icon); // 기본 아이콘(없을 시 대체용임)
        }
    }

    @Override
    public int getItemCount() {
        return subscriptionList.size();
    }

    static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, price, date;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.subscription_icon);
            name = itemView.findViewById(R.id.subscription_name);
            price = itemView.findViewById(R.id.subscription_price);
            date = itemView.findViewById(R.id.subscription_date);
        }
    }

    public void updateData(List<Subscription> newList) {
        this.subscriptionList.clear();
        this.subscriptionList.addAll(newList);
        notifyDataSetChanged();
    }
}