package com.example.youcandoit_app.Adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youcandoit_app.R;
import com.example.youcandoit_app.Task.ScheduleSuccessTask;
import com.example.youcandoit_app.dto.ScheduleDto;

import java.util.List;

public class TodayScheduleAdapter extends RecyclerView.Adapter<TodayScheduleAdapter.ViewHolder> {
    private List<ScheduleDto> dtoList;

    /** 뷰홀더 클래스 */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView start_time, end_time, all_day, schedule_title;
        CheckBox checkBox;
        View highlighter;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            start_time = itemView.findViewById(R.id.start_time);
            end_time = itemView.findViewById(R.id.end_time);
            all_day = itemView.findViewById(R.id.all_day);
            schedule_title = itemView.findViewById(R.id.schedule_title);
            checkBox = itemView.findViewById(R.id.checkBox);
            highlighter = itemView.findViewById(R.id.highlighter);
        }
    }

    public TodayScheduleAdapter(List<ScheduleDto> dtoList) {
        this.dtoList = dtoList;
    }

    /** 뷰홀더 객체 생성, 초기화 */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scheduler_today_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = viewHolder.getAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    ScheduleDto dto = dtoList.get(position);
                    ScheduleSuccessTask task = new ScheduleSuccessTask();
                    if(dto.getSchedule_success().equals("0")) {
                        task.execute(String.valueOf(dto.getSchedule_number()), "1");
                        dto.setSchedule_success("1");

                        viewHolder.checkBox.setChecked(true);
                        viewHolder.highlighter.setVisibility(View.VISIBLE);
                    } else {
                        task.execute(String.valueOf(dto.getSchedule_number()), "0");
                        dto.setSchedule_success("0");

                        viewHolder.checkBox.setChecked(false);
                        viewHolder.highlighter.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        return viewHolder;
    }

    /** 뷰홀더의 내용을 채운다. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleDto dto = dtoList.get(position);
        String start = dto.getSchedule_startdate().substring(11, 16);
        String end = dto.getSchedule_enddate().substring(11, 16);

        holder.schedule_title.setText(dto.getSchedule_title());
        // 하루종일이면 - 표시
        if(start.equals("00:00") && end.equals("23:59")) {
            holder.start_time.setVisibility(View.INVISIBLE);
            holder.end_time.setVisibility(View.INVISIBLE);
            holder.all_day.setVisibility(View.VISIBLE);
        } else {
            holder.start_time.setText(start);
            holder.end_time.setText(end);
        }

        // 형광펜 색 설정
        String color = "";
        if(position % 3 == 0) {
            color = "#66FFB1B1";
        } else if (position % 3 == 1) {
            color = "#66CAB1FF";
        } else {
            color = "#66B1FFFA";
        }
        holder.highlighter.setBackgroundColor(Color.parseColor(color));

        // 완료된 일정이면 체크표시, 형광펜 표시
        if(dto.getSchedule_success().equals("1")) {
            holder.checkBox.setChecked(true);
            holder.highlighter.setVisibility(View.VISIBLE);
        }

        // 형광펜 길이 설정
        // 화면에 그려져야 view의 길이를 알 수 있기 때문에 addOnGlobalLayoutListener 사용
        holder.schedule_title.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = holder.schedule_title.getMeasuredWidth();
                Log.i("길이", "뭐지.." + width);
                ViewGroup.LayoutParams layoutParams = holder.highlighter.getLayoutParams();
                layoutParams.width = width;
                holder.highlighter.setLayoutParams(layoutParams);

                // Listener 죽이기
                holder.schedule_title.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dtoList.size();
    }

}
