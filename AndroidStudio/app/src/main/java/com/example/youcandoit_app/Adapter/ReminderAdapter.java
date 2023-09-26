package com.example.youcandoit_app.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youcandoit_app.R;

import java.util.List;

public class ReminderAdapter  extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
    private List<String> reminderList;

    /** 뷰홀더 클래스 */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
        }
    }


    public ReminderAdapter(List<String> reminderList) {
        this.reminderList = reminderList;
    }

    /** 뷰홀더 객체 생성, 초기화 */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    /** 뷰홀더의 내용을 채운다. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String test = reminderList.get(position);
        holder.content.setText(test);
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

}