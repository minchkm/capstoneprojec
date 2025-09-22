package com.project.gudasi;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private List<Subscription> paymentList;

    public PaymentAdapter(List<Subscription> paymentList) {
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new PaymentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Subscription s = paymentList.get(position);
        holder.text1.setText(s.getServiceName());
        holder.text2.setText(s.getRenewalPrice() + " | 시작일: " + s.getDate());

        // 글자색 흰색으로
        holder.text1.setTextColor(Color.WHITE);
        holder.text2.setTextColor(Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public void updateData(List<Subscription> newList) {
        paymentList = newList;
        notifyDataSetChanged();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
