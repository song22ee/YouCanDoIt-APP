package com.example.youcandoit_app.support;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionSupport {
    private Context context;
    private Activity activity;

    // 앱 사용시 필요한 권한 목록
    private String[] permissions = {
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS
    };

    // 허가 받을 권한 목록
    private List<String> permissionList;

    public PermissionSupport(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    /** 허용 받아야 될 권한이 있는지 확인 */
    public boolean checkPermission() {
        int result;
        permissionList = new ArrayList<String>();

        // 필요한 권한 목록 forEach
        for(String pm : permissions) {
            result = ContextCompat.checkSelfPermission(context, pm);

            // 만약 허용 받지 않은 권한이라면
            if(result != PackageManager.PERMISSION_GRANTED) {
                // 허용 받을 권한 목록에 추가
                permissionList.add(pm);
            }
        }

        // 허용받아야될 권한이 있다면
        if(!permissionList.isEmpty()) {
            return true;
        }
        return false;
    }

    /** 권한 허용 요청 */
    public void requestPermission() {
        ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), 1023);
    }
}
