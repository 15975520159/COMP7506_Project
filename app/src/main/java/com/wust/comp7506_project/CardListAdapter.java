package com.wust.comp7506_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ContactViewHolder> {
    private List<CardListContactInfo> cardListContactInfoList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CardListAdapter(List<CardListContactInfo> cardListContactInfoList) {
        this.cardListContactInfoList = cardListContactInfoList;
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView vLocation;
        protected TextView vId;
        protected TextView vSync;
        protected TextView vSoc;
        protected TextView vState;
        protected ImageView vIcon;
        private OnItemClickListener listener;

        public ContactViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            vLocation = itemView.findViewById(R.id.battery_location);
            vId = itemView.findViewById(R.id.battery_id);
            vSync = itemView.findViewById(R.id.last_sync);
            vSoc = itemView.findViewById(R.id.soc);
            vState = itemView.findViewById(R.id.battery_state);
            vIcon = itemView.findViewById(R.id.battery_icon);
            this.listener = listener;

            // 为 itemView 设置点击事件监听器
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // 触发点击事件，将点击的位置传递给外部的监听器
                    listener.onItemClick(position);
                }
            }
        }
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new ContactViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        CardListContactInfo CLCI = cardListContactInfoList.get(position);
        holder.vLocation.setText(CardListContactInfo.LOCATION_PREFIX + CLCI.battery_location);
        holder.vId.setText(CardListContactInfo.ID_PREFIX + CLCI.battery_id);
        holder.vSync.setText(CardListContactInfo.SYNC_PREFIX + CLCI.last_sync);
        holder.vState.setText(CardListContactInfo.STATE_PREFIX + CLCI.state);
        holder.vSoc.setText(String.valueOf((int) (double) CLCI.soc) + CardListContactInfo.SOC_PREFIX);
//        if (CLCI.soc >= 100.0) {
//            holder.vSoc.setText(String.valueOf((int) (double) CLCI.soc) + CardListContactInfo.SOC_PREFIX);
//        }
//        else if (CLCI.soc > 100 || CLCI.soc < 0) {
//            holder.vSoc.setText("ERROR");
//        }
//        else {
//            holder.vSoc.setText(String.format("%.2f", CLCI.soc) + CardListContactInfo.SOC_PREFIX);
//        }

        int batteryIconResId;
        if (CLCI.soc >= 80) {
            batteryIconResId = R.drawable.battery_80;
        } else if (CLCI.soc >= 60 && CLCI.soc < 80) {
            batteryIconResId = R.drawable.battery_60;
        } else if (CLCI.soc >= 40 && CLCI.soc < 60) {
            batteryIconResId = R.drawable.battery_40;
        } else if (CLCI.soc >= 20 && CLCI.soc < 40) {
            batteryIconResId = R.drawable.battery_20;
        } else if (CLCI.soc > 0 && CLCI.soc < 20) {
            batteryIconResId = R.drawable.battery_10;
        } else {
            batteryIconResId = R.drawable.battery_empty;
        }

        holder.vIcon.setImageResource(batteryIconResId);
    }

    @Override
    public int getItemCount() {
        return cardListContactInfoList.size();
    }
}
