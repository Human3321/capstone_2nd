package com.example.accesscall;

import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 100;
    long animationDuration = 1000; // 1초

    public static boolean vib_mode; // 알림 진동 설정 (true - o , false - x)
    public static boolean use_set; // 사용 설정 (true - ON , false - OFF)

    // 수신에 사용할 IP 주소
    static String IP = "3.36.54.30";
    // 포트 번호
    static int Port = 56927;

    // 판별 결과
    static int isVP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_set_use = (Button) findViewById(R.id.btn_set_use); // 어플 사용 설정 버튼
        Button btn_set_vib = (Button) findViewById(R.id.btn_set_vibration); // 진동 알림 설정 버튼
        Button btn_set_vib_txt = (Button) findViewById(R.id.btn_set_vibration_txt); // 진동 알림 설정 버튼 껍데기

        // 진동 알림 설정 버튼 리스너
        btn_set_vib.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String str_btn_vib = btn_set_vib.getText().toString(); // 진동 알림 설정 버튼의 텍스트 변경

                // 진동알림 ON -> OFF
                if (str_btn_vib.equals("ON")) {
                    btn_set_vib.setText("OFF");

                    // 버튼 클릭시 애니메이션
                    ValueAnimator animator1 = ObjectAnimator.ofFloat(btn_set_vib, "translationX", 100f, 150f, 0f); // values 수정 필요
                    ValueAnimator animator3 = ObjectAnimator.ofFloat(btn_set_vib_txt, "translationX", 100f, 150f, 0f);
                    animator1.setDuration(animationDuration);
                    animator3.setDuration(animationDuration);
                    animator1.start();
                    animator3.start();

                    // 알림 진동 설정 끔
                    vib_mode = false;

                }
                // 진동알림 OFF -> ON
                else if (str_btn_vib.equals("OFF")) {
                    btn_set_vib.setText("ON");

                    // 버튼 클릭시 애니메이션
                    ValueAnimator animator2 = ObjectAnimator.ofFloat(btn_set_vib, "translationX", 100f, 150f, 0f);
                    ValueAnimator animator4 = ObjectAnimator.ofFloat(btn_set_vib_txt, "translationX", 100f, 150f, 0f);
                    animator2.setDuration(animationDuration);
                    animator4.setDuration(animationDuration);
                    animator2.start();
                    animator4.start();

                    // 알림 진동 설정 켬
                    vib_mode = true;
                }
            }
        });

        // 어플 설정 버튼 리스너
        btn_set_use.setOnClickListener(view -> {

            String str_btn = btn_set_use.getText().toString(); // 어플 설정 버튼의 텍스트
            TextView txt = findViewById(R.id.textView); // 어플 설정 버튼 밑 텍스트뷰

            if (str_btn.equals("ON")) { // 클릭 -> 실시간 탐지 OFF

                // 버튼 및 텍스트 뷰의 텍스트 변경
                btn_set_use.setText("OFF");
                txt.setText("보이스피싱 탐지 기능이 꺼졌습니다.");

                // 어플 사용 설정 OFF
                use_set = false;

            } else if (str_btn.equals("OFF")) { // 클릭 -> 실시간 탐지 ON

                // + 휴대폰 권한 받아오기
                onCheckPermission();

                // 버튼 및 텍스트 뷰의 텍스트 변경
                btn_set_use.setText("ON");
                txt.setText("보이스피싱 탐지중입니다.");

                // 어플 사용 설정 ON
                use_set = true;

            }
        });

    }

    // 어플 사용설정 최초 ON 에 한해서 권한을 받아옴
    public void onCheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_PHONE_STATE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CALL_LOG)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, SYSTEM_ALERT_WINDOW)) {
                Toast.makeText(this, "어플 사용을 위해서는 권한 설정이 필요합니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{READ_PHONE_STATE, READ_CALL_LOG, SYSTEM_ALERT_WINDOW}, PERMISSIONS_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{READ_PHONE_STATE, READ_CALL_LOG, SYSTEM_ALERT_WINDOW}, PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "어플 실행을 위한 권한이 설정 되었습니다.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "어플 실행을 위한 권한이 취소 되었습니다.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}