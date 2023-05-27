package com.example.accesscall;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

//주석 처리된 코드는 구 팝업창

public class AlertWindow extends Service {
    //private MainActivity mainActivity;
    //private ServiceConnection connection;

    //서비스 바인더
    /*
    public class LocalBinder extends Binder {
        AlertWindow getService() {
            return AlertWindow.this;
        }
    }

    private final IBinder binder = new LocalBinder();
    */
    public static String isWarning = "1차 판별 결과";
    public static String Count = "신고 횟수";
    public static String Number = "전화 번호";

    ImageView img;
    TextView tv_result;
    TextView tv_number;
    ImageButton btn;

    WindowManager wm;
    View mView;

    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //return binder;
        return null;
    }

    /*
    //MainActivity와 바인딩
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bindToMainActivity();
        return super.onStartCommand(intent, flags, startId);
    }

    private void bindToMainActivity() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocalBinder binder = (LocalBinder) service;
                AlertWindow.this.mainActivity = binder.getService().getMainActivity();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                AlertWindow.this.mainActivity = null;
            }
        };

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    // MainActivity의 setOverlayVisibility() 메서드를 호출하여 가시성 변경
    public void setOverlayVisibility(int visibility) {
        if (mainActivity != null) {
            mainActivity.setOverlayVisibility(visibility);
        }
    }

    // MainActivity 반환
    public MainActivity getMainActivity() {
        return mainActivity;
    }
    */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Number = intent.getStringExtra(Number);
        isWarning = intent.getStringExtra(isWarning);
        Count = intent.getStringExtra(Count);

        tv_number.setText(Number);  //생성시 받아온 전화번호로 변경
        if (isWarning.equals("깨끗")) {   //신고 이력이 없는 깨끗한 전화번호일 경우
            tv_result.setText("신고 이력이 없는 번호입니다.");  //결과 텍스트 변경
            tv_result.setTextColor(Color.parseColor("#157AC6"));    //결과 텍스트 색상 변경
            img.setImageResource(R.drawable.checked);   //체크 이미지로 변경
        }
        else if (isWarning.equals("주의")) {  //신고 이력이 있는 전화번호일 경우
            tv_result.setText("주의! 신고 " + Count + "회 누적된 번호입니다.");  //결과 텍스트 변경
            tv_result.setTextColor(Color.parseColor("#9A1E1E"));  //결과 텍스트 색상 변경
            img.setImageResource(R.drawable.alarm);   //경고 이미지로 변경
        }
        else {

        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                30, 30, // X, Y 좌표
                TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        mView = inflate.inflate(R.layout.view_in_service, null);
        img = (ImageView) mView.findViewById(R.id.img);
        tv_result = (TextView) mView.findViewById(R.id.tv_result);
        tv_number = (TextView) mView.findViewById(R.id.tv_number);
        btn =  (ImageButton) mView.findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.setVisibility(View.INVISIBLE);
            }
        });
        wm.addView(mView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(wm != null) {
            if(mView != null) {
                wm.removeView(mView);
                mView = null;
            }
            wm = null;
        }

        //unbindService(connection);
    }
}