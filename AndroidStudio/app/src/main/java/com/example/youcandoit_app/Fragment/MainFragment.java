package com.example.youcandoit_app.Fragment;

import static android.content.Context.MODE_PRIVATE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youcandoit_app.Activity.CameraActivity;
import com.example.youcandoit_app.Adapter.DiyCertifyAdapter;
import com.example.youcandoit_app.Service.PedometerService;
import com.example.youcandoit_app.Task.DiyCertifyGroupTask;
import com.example.youcandoit_app.R;
import com.example.youcandoit_app.Task.PedometerGoalSelectTask;
import com.example.youcandoit_app.dto.GroupDto;

import java.text.DecimalFormat;
import java.util.List;

public class MainFragment extends Fragment {

    RecyclerView recyclerView;
    TextView step_counter;
    ProgressBar pedometer_progress;
    ConstraintLayout goal_layout;
    Button goal_btn;
    View.OnClickListener cl;
    Fragment goal_fragment, set_fragment;

    SharedPreferences pedometer_preferences, user_preferences;

    String id;
    // 인증이 필요한 그룹 리스트
    List<GroupDto> diyGroupList;
    // 만보기 목표
    int goal;

    // 숫자 포맷
    DecimalFormat decFormat = new DecimalFormat("###,###");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("MainFragment", "실행");

        View view = inflater.inflate(R.layout.main_fragment, container, false);

        recyclerView = view.findViewById(R.id.diyCertifyList);
        step_counter = view.findViewById(R.id.tv_step);
        pedometer_progress = view.findViewById(R.id.pedometer_progress);
        goal_layout = view.findViewById(R.id.goal_layout);
        goal_btn = view.findViewById(R.id.goal_button);

        user_preferences = getActivity().getSharedPreferences("login", MODE_PRIVATE);
        id = user_preferences.getString("id", null);

        goal_fragment = new GoalFragment();
        set_fragment = new GoalSetFragment();

        cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.goal_layout) {
                    getChildFragmentManager().beginTransaction().replace(R.id.main_fragment_layout, goal_fragment).commit();
                } else if(view.getId() == R.id.goal_button) {
                    getChildFragmentManager().beginTransaction().replace(R.id.main_fragment_layout, set_fragment).commit();
                }
            }
        };
        goal_layout.setOnClickListener(cl);
        goal_btn.setOnClickListener(cl);

        // 메인페이지에서 필요한 데이터 받아오기
        getServerData();

        // ==================================== 만보기 관련 ====================================
        // Service에서 보내는 값을 받기 위한 선언
        BroadcastReceiver br = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("pedometer");
        getContext().registerReceiver(br, filter);
        // ===================================================================================


        // ==================================== diy 인증 관련 ==================================
        // 아이템을 가로로 배치하기 위해 LinearLayout 사용
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        // 아이템 간격 설정
        RecyclerViewDecoration decoration = new RecyclerViewDecoration(30);
        recyclerView.addItemDecoration(decoration);

        DiyCertifyAdapter diyCertifyAdapter = new DiyCertifyAdapter(diyGroupList);
        // 커스텀 ClickListener 바디 구현
        // 실제로는 recyclerView 영역을 클릭하면 어댑터에 구현해놓은 clickListener가 실행
        // clickListener에서 포지션 값을 구해 다시 커스텀 ClickListener를 호출한다.
        diyCertifyAdapter.setOnItemClickListener(new DiyCertifyAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(int groupNumber) {
                Log.i("MainActivity.java", "그룹번호 받음 : " + groupNumber);
                Intent i = new Intent(getContext(), CameraActivity.class);
                i.putExtra("number", String.valueOf(groupNumber));
                startActivityForResult(i, 100);
            }
        });
        recyclerView.setAdapter(diyCertifyAdapter);
        // ===================================================================================

        // ==================================== 챌린지 달성 현황 관련 ==================================
        pedometer_preferences = getActivity().getSharedPreferences("pedometer", MODE_PRIVATE);
        step_counter.setText(decFormat.format(pedometer_preferences.getInt("step", 0)) + " 걸음");

        pedometer_progress.setMax(goal);
        pedometer_progress.setProgress(pedometer_preferences.getInt("step", 0));
        goal_btn.setText("목표 " + decFormat.format(goal) + " >");
        // ===================================================================================

        return view;
    }

    /** Service에서 요청을 보냈을 때 할 작업 */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 보낸 액션이 pedometer라면
            if(intent.getAction() == "pedometer") {
                step_counter.setText(decFormat.format(pedometer_preferences.getInt("step", 0)) + " 걸음");
                pedometer_progress.setProgress(pedometer_preferences.getInt("step", 0));
            }
        }
    }

    /** 메인페이지에 필요한 데이터 받아오기 */
    public void getServerData() {
        try {
            // 인증이 필요한 그룹
            DiyCertifyGroupTask diyTask = new DiyCertifyGroupTask();
            diyGroupList = diyTask.execute(id).get();
            // 만보기 목표
            PedometerGoalSelectTask goalTask = new PedometerGoalSelectTask();
            goal = goalTask.execute(id).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** RecyclerView 간격 조절 클래스 */
    public class RecyclerViewDecoration extends RecyclerView.ItemDecoration {
        private final int divWidth;

        public RecyclerViewDecoration(int divWidth)
        {
            this.divWidth = divWidth;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
        {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.right = divWidth;
        }
    }

    /** DIY 인증을 완료했다면 */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 100) {
            // fragment 새로고침
            Log.i("MainFragment", "인증완료");
            getFragmentManager().beginTransaction().detach(this).commit();
            getFragmentManager().beginTransaction().attach(this).commit();
        }
    }
}