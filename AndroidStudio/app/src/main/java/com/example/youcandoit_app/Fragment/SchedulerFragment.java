package com.example.youcandoit_app.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youcandoit_app.Adapter.TodayScheduleAdapter;
import com.example.youcandoit_app.R;
import com.example.youcandoit_app.Task.TodayScheduleTask;
import com.example.youcandoit_app.dto.ScheduleDto;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class SchedulerFragment extends Fragment {

    TabLayout tabs;
    RecyclerView today_list_view;

    SharedPreferences user_preferences;

    String id;
    List<ScheduleDto> today_schedule_list;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scheduler_fragment, container, false);

        tabs = view.findViewById(R.id.tabs);
        today_list_view = view.findViewById(R.id.todayScheduleList);

        // 탭 추가
        tabs.addTab(tabs.newTab().setText("오늘의 일정"));
        tabs.addTab(tabs.newTab().setText("다가오는 일정"));
        tabs.addTab(tabs.newTab().setText("타임 테이블"));

        // 탭 상단 라운딩
        tabs.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight() + 200, 22);
            }
        });
        tabs.setClipToOutline(true);

        user_preferences = getActivity().getSharedPreferences("login", MODE_PRIVATE);
        id = user_preferences.getString("id", null);

        getServerData();

        // ==================================== 오늘의 일정 ==================================
        // 아이템 세로로 배치
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        today_list_view.setLayoutManager(linearLayoutManager);

        // 아이템 간격 설정
        RecyclerViewDecoration decoration = new RecyclerViewDecoration(5);
        today_list_view.addItemDecoration(decoration);

        TodayScheduleAdapter todayScheduleAdapter = new TodayScheduleAdapter(today_schedule_list);
        today_list_view.setAdapter(todayScheduleAdapter);
        // ==================================================================================

        return view;
    }

    /** 스케줄러페이지에 필요한 데이터 받아오기 */
    public void getServerData() {
        try {
            TodayScheduleTask todayTask = new TodayScheduleTask();
            today_schedule_list = todayTask.execute(id).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** RecyclerView 간격 조절 클래스 */
    public class RecyclerViewDecoration extends RecyclerView.ItemDecoration {
        private final int divHeight;

        public RecyclerViewDecoration(int divHeight)
        {
            this.divHeight = divHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
        {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.bottom = divHeight;
        }
    }
}