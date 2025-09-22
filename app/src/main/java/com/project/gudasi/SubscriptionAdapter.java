package com.project.gudasi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private List<Subscription> subscriptionList;
    private Context context;

    public SubscriptionAdapter(List<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription, parent, false);
        return new SubscriptionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        Subscription sub = subscriptionList.get(position);
        holder.date.setText("🗓 " + sub.getDate());
        holder.service.setText("🛎 " + sub.getServiceName());
        holder.price.setText("💰 " + sub.getRenewalPrice());
    }

    @Override
    public int getItemCount() {
        return subscriptionList.size();
    }

    static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        TextView date, service, purchase, price;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateText);
            service = itemView.findViewById(R.id.serviceText);
            price = itemView.findViewById(R.id.priceText);
        }
    }

    public void updateData(List<Subscription> newList) {
        this.subscriptionList.clear();
        this.subscriptionList.addAll(newList);
        notifyDataSetChanged();
    }

}
