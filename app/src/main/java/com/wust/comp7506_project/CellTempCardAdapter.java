package com.wust.comp7506_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CellTempCardAdapter extends RecyclerView.Adapter<CellTempCardAdapter.CardViewHolder> {
    private List<Float> tempList;  // 温度列表

    // 构造器，传入数据列表并过滤掉温度为 -40 的数据
    public CellTempCardAdapter(int tempmCount, List<Float> tempList) {
        // 过滤掉所有值为 -40 的项
        this.tempList = new ArrayList<>();
        for (float temp : tempList) {
            if (temp != -40) {
                this.tempList.add(temp);
            }
        }
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 使用 LayoutInflater 加载 cell_battery_card_view 布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_temp_card_view, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        float tempValue = tempList.get(position);

        // 绑定温度数据
        holder.tempTextView.setText(tempValue + "℃");

        // 绑定温度ID
        holder.tempIdTextView.setText("[" + (position + 1) + "]");
    }

    @Override
    public int getItemCount() {
        return tempList.size();  // 返回有效的温度数据的数量
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView tempTextView;
        TextView tempIdTextView;

        public CardViewHolder(View itemView) {
            super(itemView);
            // 获取 CardView 中的 TextView
            tempTextView = itemView.findViewById(R.id.temp);
            tempIdTextView = itemView.findViewById(R.id.temp_id);
        }
    }
}
