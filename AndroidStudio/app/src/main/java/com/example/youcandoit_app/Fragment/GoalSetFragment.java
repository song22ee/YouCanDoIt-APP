package com.example.youcandoit_app.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.youcandoit_app.R;
import com.example.youcandoit_app.Task.PedometerGoalUpdateTask;
import com.example.youcandoit_app.support.onBackPressedSupport;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoalSetFragment extends Fragment implements onBackPressedSupport {

    ConstraintLayout set_layout;
    RadioGroup radioGroup;
    Button set_goal;

    View.OnClickListener cl;
    RadioButton direct_input;
    RadioGroup.OnCheckedChangeListener ccl;

    SharedPreferences user_preferences;
    String id;

    int goal = 10000;
    // 직접 설정한 목표(다시 설정할 때 사용하기 위함)
    int direct_goal= -1;
    // Dialog에서 취소를 눌렀을 때 돌아갈 라디오버튼
    int cancelId = R.id.set_10000;

    // 숫자 포맷
    DecimalFormat decFormat = new DecimalFormat("###,###");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("GoalSetFragment", "실행");

        View view = inflater.inflate(R.layout.goal_set_fragment, container, false);

        set_layout = view.findViewById(R.id.set_layout);
        radioGroup = view.findViewById(R.id.radioGroup);
        set_goal = view.findViewById(R.id.set_goal);
        direct_input = view.findViewById(R.id.direct_input);

        cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.set_layout:
                        onBackPressed();
                        break;
                    case R.id.set_goal:
                        setGoal();
                        break;
                    case R.id.direct_input:
                        setDirectInput();
                        break;
                }
            }
        };
        set_layout.setOnClickListener(cl);
        set_goal.setOnClickListener(cl);
        direct_input.setOnClickListener(cl);

        ccl = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.set_5000:
                        goal = 5000;
                        cancelId = R.id.set_5000;
                        Log.i("goal_set", "value : 5000");
                        break;
                    case R.id.set_10000:
                        goal = 10000;
                        cancelId = R.id.set_10000;
                        Log.i("goal_set", "value : 10000");
                        break;
                    case R.id.set_15000:
                        goal = 15000;
                        cancelId = R.id.set_15000;
                        Log.i("goal_set", "value : 15000");
                        break;
                    case R.id.set_20000:
                        goal = 20000;
                        cancelId = R.id.set_20000;
                        Log.i("goal_set", "value : 20000");
                        break;
                }
            }
        };
        radioGroup.setOnCheckedChangeListener(ccl);

        user_preferences = getActivity().getSharedPreferences("login", MODE_PRIVATE);
        id = user_preferences.getString("id", null);

        return view;
    }

    /** 목표 직접 설정 */
    public void setDirectInput() {
        EditText editText = new EditText(getContext());
        if(direct_goal != -1) {
            editText.setText(direct_goal + "");
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        dialog.setTitle("일일 걸음 목표 설정");
        dialog.setView(editText);
        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String text = editText.getText().toString();
                if(!text.equals("")) {
                    try {
                        direct_goal = Integer.parseInt(editText.getText().toString());
                        goal = direct_goal;
                        direct_input.setText("직접 설정 (" + decFormat.format(direct_goal) + " 걸음)");
                        cancelId = R.id.direct_input;
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "숫자만 입력해주세요.", Toast.LENGTH_SHORT).show();
                        radioGroup.check(cancelId);
                    }
                } else {
                    Toast.makeText(getContext(), "값을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    radioGroup.check(cancelId);
                }
            }
        });
        dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                radioGroup.check(cancelId);
            }
        });
        dialog.show();
    }

    /** 목표 설정 */
    public void setGoal() {
        try {
            PedometerGoalUpdateTask goalTask = new PedometerGoalUpdateTask();
            goalTask.execute(id, String.valueOf(goal));

            // 부모 프래그먼트를 구해서 새로고침
            Fragment parent = getParentFragment();
            Fragment gParent = parent.getParentFragment();
            parent.getFragmentManager().beginTransaction().detach(parent).commit();
            parent.getFragmentManager().beginTransaction().attach(parent).commit();
            if(gParent != null) {
                gParent.getFragmentManager().beginTransaction().detach(gParent).commit();
                gParent.getFragmentManager().beginTransaction().attach(gParent).commit();
            }

            onBackPressed();
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
