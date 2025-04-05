package com.wust.comp7506_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class CellBatteryCardAdapter extends RecyclerView.Adapter<CellBatteryCardAdapter.CardViewHolder> {
    private int cellNumber;
    private List<Integer> vList;
    private int maxVValue;
    private int minVValue;

    public CellBatteryCardAdapter(int cellNumber, List<Integer> socList) {
        this.cellNumber = cellNumber;
        this.vList = socList;

        if (!vList.isEmpty()) {
            maxVValue = Collections.max(vList);
            minVValue = Collections.min(vList);
        }
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 使用 LayoutInflater 加载 cell_battery_card_view 布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_battery_card_view, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        int vValue = vList.get(position);
        holder.cellVTextView.setText(vValue + "mV");
        holder.cellIdTextView.setText("Cell " + (position + 1));

        if (vValue == maxVValue) {
            holder.batteryStatusIcon.setImageResource(R.drawable.battery_red); // 最大值 -> 红色
        } else if (vValue == minVValue) {
            holder.batteryStatusIcon.setImageResource(R.drawable.battery_green); // 最小值 -> 绿色
        } else {
            holder.batteryStatusIcon.setImageResource(R.drawable.battery_black); // 其他 -> 黑色
        }
    }

    @Override
    public int getItemCount() {
        return cellNumber;  // 返回电池的数量
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        // 假设每个 CardView 中有两个 TextView：cell_v 和 cell_id
        TextView cellVTextView;
        TextView cellIdTextView;
        ImageView batteryStatusIcon;

        public CardViewHolder(View itemView) {
            super(itemView);
            // 获取 CardView 中的 TextView
            cellVTextView = itemView.findViewById(R.id.temp);  // 假设 TextView 的 ID 是 cell_v
            cellIdTextView = itemView.findViewById(R.id.temp_id);  // 假设 TextView 的 ID 是 cell_id
            batteryStatusIcon = itemView.findViewById(R.id.cellBattery_icon);
        }
    }
}

