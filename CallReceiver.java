package com.example.forquick;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CallReceiver extends BroadcastReceiver {
    String phonestate;
    public static final String TAG_phoneState = "PHONE STATE";
    String url = "http://13.124.192.194:54103/user/"; // 서버 IP 주소

    // 신고용 url
    String reportUrl = "http://13.124.192.194:54103/report/";
    // 신고용 전역 휴대폰번호
    String phoneNumtoReport;

    public GettingPHP gPHP;
    String result = "";

    MainActivity instance;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")

    @Override
    public void onReceive(Context context, Intent intent) {
        
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
            MainActivity.getInstance().isVP = 0;
            // 수신 번호 가져옴
            phoneNumtoReport = phone;

            String phone_number = PhoneNumberUtils.formatNumber(phone);

            if(instance.getInstance().phoneNumCheck(phone_number)){
                return;
            }
        }
    }

    private FileObserver fileObserver;
    String directoryPath = "/storage/emulated/0/Recordings/Call";
    String filePath = null;

    private void startFileObservation() {
        fileObserver = new FileObserver(directoryPath) {
            @Override
            public void onEvent(int event, String path) {
                if (event == FileObserver.CREATE) {
                    // 새 파일이 생성 처리 로직
                    filePath = directoryPath + "/" + path;
                    // filePath를 처리하는 코드를 추가합니다.
                    System.out.println(filePath);
                }
            }
        };
        fileObserver.startWatching(); // 감시 시작
        System.out.println("파일 감시 시작");
    }

    private void handleActiveCall(Context context,Intent intent) {

        String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (phone != null) {
            System.out.println("통화 중 확인");
            String phone_number = PhoneNumberUtils.formatNumber(phone);


            // 파일 감시 시작
            startFileObservation();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(MainActivity.getInstance().isVP == 1){
                        //Toast.makeText(context, "보이스피싱 의심 전화입니다 !", Toast.LENGTH_SHORT).show();
                        System.out.println("VPIS == 1 태그");
                        
                        Toast.makeText(context, "보이스피싱", Toast.LENGTH_LONG).show();
                        System.out.println("VPIS == 1 팝업 태그");
                        
                        System.out.println("Success 종료 태그");
                    }
                }         
            }, 60000); // 60초 지연
        }
    }

    private void handleIdleCall(Context context, Intent intent) {
        // 진동 설정
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        
        if (phone != null) {
            System.out.println("통화 종료 확인");
            String phone_number = PhoneNumberUtils.formatNumber(phone);
            phoneNumtoReport = phone;
            
            // Todo: 종료 동작 추가

            if (MainActivity.getInstance().isVP == 1) {
                // 서버에 수신 전화번호 신고
                gPHP = new CallReceiver.GettingPHP();
                gPHP.execute(reportUrl+phoneNumtoReport);
            } else {

            }
        }
    }

    class GettingPHP extends AsyncTask<String, String, String> {

        protected void onPreExecute(){
        }

        // php 에서 데이터 읽어옴
        @Override
        protected String doInBackground(String... params) { // params : 전화번호
            Log.d("1conn_1", "1 ok");

            StringBuilder jsonHtml = new StringBuilder();
            try {
                URL phpUrl = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) phpUrl.openConnection();
                Log.d("2conn_2", "2 ok");
                Log.d("conn_state", String.valueOf(conn));


                if (conn != null) {
                    Log.d("3conn_3", "3 ok");
                    int con_state = conn.getResponseCode();
                    Log.d("conn_r_code", String.valueOf(con_state));

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.d("conn_eccc", "연결 ok");
                        Log.d("c_url", String.valueOf(conn.getURL()));
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                        while (true) {
                            String line = br.readLine();
                            jsonHtml.append(line);
                            publishProgress(br.readLine());

                            Log.d("line_value", line);
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

            return jsonHtml.toString();
        }

    }
}
