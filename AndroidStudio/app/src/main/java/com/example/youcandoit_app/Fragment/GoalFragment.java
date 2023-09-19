package com.example.youcandoit_app.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.youcandoit_app.R;
import com.example.youcandoit_app.Task.PedometerGoalSelectTask;
import com.example.youcandoit_app.support.onBackPressedSupport;

import java.text.DecimalFormat;

public class GoalFragment extends Fragment implements onBackPressedSupport {

    ImageButton back;
    Button set;
    TextView step_counter, goal_view;
    ProgressBar pedometer_progress;
    View.OnClickListener cl;

    Fragment set_fragment;

    SharedPreferences pedometer_preferences, user_preferences;

    String id;
    int goal;
    // 숫자 포맷
    DecimalFormat decFormat = new DecimalFormat("###,###");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("GoalFragment", "실행");

        View view = inflater.inflate(R.layout.goal_fragment, container, false);

        back = view.findViewById(R.id.back);
        set = view.findViewById(R.id.goal_set);
        step_counter = view.findViewById(R.id.tv_step);
        goal_view = view.findViewById(R.id.goal_textView);
        pedometer_progress = view.findViewById(R.id.pedometer_progress);

        set_fragment = new GoalSetFragment();

        cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.back) {
                    onBackPressed();
                } else if(view.getId() == R.id.goal_set) {
                    getChildFragmentManager().beginTransaction().replace(R.id.goal_fragment_layout, set_fragment).commit();
                }
            }
        };
        back.setOnClickListener(cl);
        set.setOnClickListener(cl);

        user_preferences = getActivity().getSharedPreferences("login", MODE_PRIVATE);
        id = user_preferences.getString("id", null);

        pedometer_preferences = getActivity().getSharedPreferences("pedometer", MODE_PRIVATE);
        step_counter.setText(decFormat.format(pedometer_preferences.getInt("step", 0)) + " 걸음");

        getGoal();

        // Progress 설정
        pedometer_progress.setMax(goal);
        pedometer_progress.setProgress(pedometer_preferences.getInt("step", 0));
        goal_view.setText("목표 " + decFormat.format(goal));

        set.setText(decFormat.format(goal) + " 걸음");

        // ==================================== 만보기 관련 ====================================
        // Service에서 보내는 값을 받기 위한 선언
        BroadcastReceiver br = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("pedometer");
        getContext().registerReceiver(br, filter);
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

    /** 서버에서 목표값 받아오기 */
    public void getGoal() {
        try {
            PedometerGoalSelectTask goalTask = new PedometerGoalSelectTask();
            goal = goalTask.execute(id).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 뒤로가기 */
    @Override
    public void onBackPressed() {
        getParentFragmentManager().beginTransaction().remove(this).commit();
    }
}
