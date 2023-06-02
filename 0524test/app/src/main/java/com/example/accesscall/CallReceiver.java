package com.example.accesscall;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CallReceiver extends BroadcastReceiver {
    private String previousState = "";
    public GettingPHP gPHP;

    // 보이스피싱용 url
    private String url = "http://118.67.132.20:8080/user/"; // 서버 IP 주소
    public String result = null;

    // 신고용 url
    String reportUrl = "http://118.67.132.20:8080/report/";

    // 신고용 전역 휴대폰번호
    String phoneNumtoReport;

    // 상대 휴대폰 번호
    String phone_number;

    // 안심번호 데이터 adapter
    PhoneNumInfoAdapter adapter = new PhoneNumInfoAdapter();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")

    @Override
    public void onReceive(Context context, Intent intent) {
        //구 팝업창 관련 처리. 다른 기능 완성 후에도 팝업창 생성에 문제 없으면 제거해도 OK
        //AlertWindow alertWindow = (AlertWindow) context;
        //alertWindow.setOverlayVisibility(View.VISIBLE);

        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (state != null && !state.equals(previousState)) {
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    handleRingingCall(context, intent);
                } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    handleActiveCall(context, intent);
                } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    handleIdleCall(context, intent);
                }
                previousState = state;
            }
        }
    }

    private void handleRingingCall(Context context, Intent intent) {
        String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (phone != null) {
            System.out.println("통화 수신 확인");
            // 수신 번호 가져옴
            phoneNumtoReport = phone;

            // 안심번호 판별을 위해 추가----
            phone_number = PhoneNumberUtils.formatNumber(phone);

            if(adapter.phoneNumCheck(phone_number)){
                //안심 번호 팝업창 생성
                Intent serviceIntent = new Intent(context, AlertWindow.class);
                serviceIntent.putExtra(AlertWindow.Number, phone_number);
                serviceIntent.putExtra(AlertWindow.isWarning, "안심");
                serviceIntent.putExtra(AlertWindow.Count, "0");
                context.startService(serviceIntent);

                // Handler 객체 생성
                Handler handler = new Handler();

                // 일정 시간 후에 서비스 중지 실행
                long delayMillis = 5000; // 5초 후에 서비스 중지
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 서비스 중지 코드 추가
                        context.stopService(serviceIntent);
                    }
                }, delayMillis);

                return;
            }

            // 서버에 수신 전화번호 보내서 결과 받아옴
            try {
                gPHP = new CallReceiver.GettingPHP();
                result = gPHP.execute(url + phone).get();
                if (result.length() >= 7) {
                    String full = result;
                    String split[] = full.split(":");
                    String s = split[1];
                    String s1[] = s.split("]");
                    //1차 판별 팝업창 생성
                    Intent serviceIntent = new Intent(context, AlertWindow.class);
                    serviceIntent.putExtra(AlertWindow.Number, phone_number);
                    serviceIntent.putExtra(AlertWindow.isWarning, "주의");
                    serviceIntent.putExtra(AlertWindow.Count, s1[0]);
                    context.startService(serviceIntent);

                    // Handler 객체 생성
                    Handler handler = new Handler();

                    // 일정 시간 후에 서비스 중지 실행
                    long delayMillis = 5000; // 5초 후에 서비스 중지
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 서비스 중지 코드 추가
                            context.stopService(serviceIntent);
                        }
                    }, delayMillis);
                } else {
                    //1차 판별 팝업창 생성
                    Intent serviceIntent = new Intent(context, AlertWindow.class);
                    serviceIntent.putExtra(AlertWindow.Number, phone_number);
                    serviceIntent.putExtra(AlertWindow.isWarning, "깨끗");
                    serviceIntent.putExtra(AlertWindow.Count, "0");
                    context.startService(serviceIntent);

                    // Handler 객체 생성
                    Handler handler = new Handler();

                    // 일정 시간 후에 서비스 중지 실행
                    long delayMillis = 5000; // 5초 후에 서비스 중지
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 서비스 중지 코드 추가
                            context.stopService(serviceIntent);
                        }
                    }, delayMillis);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleActiveCall(Context context,Intent intent) {

        String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (phone != null) {
            System.out.println("통화 중 확인");

            // Todo: 수신 시 동작 추가
            
            // Todo: 제대로 작동하는지 확인해봐야함!
            if(MainActivity.getInstance().isVP == 1){
                //Toast.makeText(context, "보이스피싱 의심 전화입니다 !", Toast.LENGTH_SHORT).show();

                //보이스 피싱 판별 팝업창 생성
                Intent serviceIntent = new Intent(context, AlertWindow.class);
                serviceIntent.putExtra(AlertWindow.Number, phone_number);
                serviceIntent.putExtra(AlertWindow.isWarning, "피싱");
                serviceIntent.putExtra(AlertWindow.Count, "0"); //2차 판별이므로 0
                context.startService(serviceIntent);

                // Handler 객체 생성
                Handler handler = new Handler();

                // 일정 시간 후에 서비스 중지 실행
                long delayMillis = 5000; // 5초 후에 서비스 중지
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 서비스 중지 코드 추가
                        context.stopService(serviceIntent);
                    }
                }, delayMillis);
            }
        }
    }

    private void handleIdleCall(Context context, Intent intent) {
        // 진동 설정
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        
        if (phone != null) {
            System.out.println("통화 종료 확인");
            phoneNumtoReport = phone;
            
            // Todo: 종료 동작 추가

            if (MainActivity.getInstance().isVP == 1) {
                // 서버에 수신 전화번호 신고
                gPHP = new CallReceiver.GettingPHP();
                gPHP.execute(reportUrl+phoneNumtoReport);
                //Toast.makeText(context, "보이스피싱 주의! 서버에 자동 신고되었습니다.", Toast.LENGTH_LONG).show();

                //보이스 피싱 판별 팝업창 생성
                Intent serviceIntent = new Intent(context, AlertWindow.class);
                serviceIntent.putExtra(AlertWindow.Number, phone_number);
                serviceIntent.putExtra(AlertWindow.isWarning, "신고");
                serviceIntent.putExtra(AlertWindow.Count, "0"); //2차 판별이므로 0
                context.startService(serviceIntent);

                // Handler 객체 생성
                Handler handler = new Handler();

                // 일정 시간 후에 서비스 중지 실행
                long delayMillis = 5000; // 5초 후에 서비스 중지
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 서비스 중지 코드 추가
                        context.stopService(serviceIntent);
                    }
                }, delayMillis);

                // 진동 설정
                vibrator.vibrate(1000);
            } else {

            }
        }
    }

    public class GettingPHP extends AsyncTask<String, Integer, String> {

        // php 에서 데이터 읽어옴
        @Override
        protected String doInBackground(String... params) {

            // json 타입의 데이터를 string 형태로 받아옴
            StringBuilder jsonHtml = new StringBuilder();
            try {
                // 서버 접속
                URL phpUrl = new URL(params[0]);
                //HttpClient httpClient = new DefaultHttpClient();
                HttpURLConnection conn = (HttpURLConnection) phpUrl.openConnection();

                if (conn != null) {
                    conn.setUseCaches(false);
                    // 서버 응답 상태
                    int con_state = conn.getResponseCode();

                    // 서버 접속 성공했으면
                    if (con_state == HttpURLConnection.HTTP_OK) {
                        //BufferReader로 결과값 읽어옴
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                        while (true) {
                            String line = br.readLine();
                            jsonHtml.append(line);

                            if (line == null)
                                break;
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 결과값을 string 형태로 반환
            return jsonHtml.toString();
        }
    }
}