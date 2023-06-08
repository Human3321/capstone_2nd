package com.example.accesscall;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

public class AlertWindow extends Service {
    public static String isWarning = "1차 판별 결과";
    public static String Count = "신고 횟수";
    public static String Number = "전화 번호";

    WindowManager.LayoutParams params;

    ImageView img;
    TextView tv_result;
    TextView tv_number;
    ImageButton btn;
    CardView cv;

    WindowManager wm;
    View mView;

    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Number = intent.getStringExtra(Number);
        isWarning = intent.getStringExtra(isWarning);
        Count = intent.getStringExtra(Count);

        tv_number.setText(Number);  //생성시 받아온 전화번호로 변경
        if (isWarning.equals("안심")) {   //안심번호일 경우
            tv_result.setText("안심 번호입니다.");  //결과 텍스트 변경
            tv_result.setTextColor(Color.parseColor("#157AC6"));    //결과 텍스트 색상 변경
            cv.setCardBackgroundColor(Color.parseColor("#96DCFB")); //카드뷰 색상 변경
            img.setImageResource(R.drawable.list_checked);   //체크 리스트 이미지로 변경
        }
        else if (isWarning.equals("깨끗")) {  //신고 이력이 없는 깨끗한 전화번호일 경우
            tv_result.setText("신고 이력이 없는 번호입니다.");  //결과 텍스트 변경
            tv_result.setTextColor(Color.parseColor("#16B569"));    //결과 텍스트 색상 변경
            cv.setCardBackgroundColor(Color.parseColor("#D5E8DF")); //카드뷰 색상 변경
            img.setImageResource(R.drawable.checked);   //체크 이미지로 변경
        }
        else if (isWarning.equals("주의")) {  //신고 이력이 있는 전화번호일 경우
            tv_result.setText("주의! 신고 " + Count + "회 누적된 번호입니다.");  //결과 텍스트 변경
            tv_result.setTextColor(Color.parseColor("#F39D2F"));  //결과 텍스트 색상 변경
            cv.setCardBackgroundColor(Color.parseColor("#FDF0AF")); //카드뷰 색상 변경
            img.setImageResource(R.drawable.warning);   //주의 이미지로 변경
        }
        else if (isWarning.equals("피싱")) {  //2차 판별 피싱 내용이 감지되었다면
            tv_result.setText("경고! 보이스피싱으로 탐지되었습니다.");  //결과 텍스트 변경
            tv_result.setTextColor(Color.parseColor("#9A1E1E"));  //결과 텍스트 색상 변경
            cv.setCardBackgroundColor(Color.parseColor("#EAC5C5")); //카드뷰 색상 변경
            img.setImageResource(R.drawable.alarm);   //경고 이미지로 변경
        }
        else if (isWarning.equals("신고")) {  //2차 판별 피싱 내용이 감지되었다면
            tv_result.setText("신고 완료!\n보이스피싱으로 서버에 자동 신고되었습니다.");  //결과 텍스트 변경
            tv_result.setTextColor(Color.parseColor("#9A1E1E"));  //결과 텍스트 색상 변경
            tv_result.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);  //결과 텍스트 사이즈 변경
            cv.setCardBackgroundColor(Color.parseColor("#EAC5C5")); //카드뷰 색상 변경
            img.setImageResource(R.drawable.alarm);   //경고 이미지로 변경
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                30, 30, // X, Y 좌표
                TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        params.y = -150;
        mView = inflate.inflate(R.layout.view_in_service, null);
        img = (ImageView) mView.findViewById(R.id.img);
        tv_result = (TextView) mView.findViewById(R.id.tv_result);
        tv_number = (TextView) mView.findViewById(R.id.tv_number);
        btn = (ImageButton) mView.findViewById(R.id.btn);
        cv = (CardView) mView.findViewById(R.id.cv);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.setVisibility(View.INVISIBLE);
            }
        });
        wm.addView(mView, params);

        setDraggable();
    }

    void setDraggable() {
        mView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:   //처음 팝업창을 눌렀을 때
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP: //팝업창을 누른걸 땠을 때
                        return true;
                    case MotionEvent.ACTION_MOVE:   //팝업창을 누르고 움직였을 때
                        params.x = initialX + (int)(event.getRawX() - initialTouchX);
                        params.y = initialY + (int)(event.getRawY() - initialTouchY);

                        if (mView != null)
                            wm.updateViewLayout(mView, params);
                        return true;
                }

                return false;
            }
        });
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
    }


}