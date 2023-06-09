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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")

    @Override
    public void onReceive(Context context, Intent intent) {


        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        Log.d(TAG_phoneState,"onReceive()");

        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {

            //TelecomManager telephonyManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

            Bundle extras = intent.getExtras();

            if (extras != null) {

                // 현재 폰 상태 가져옴
                String state = extras.getString(TelephonyManager.EXTRA_STATE);
                Log.d("phone_state", state);

                // 중복 호출 방지
                if (state.equals(phonestate)) {
                    return;
                } else {
                    phonestate = state;
                }

                // [벨 울리는 중]
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

                    if(MainActivity.use_set == true) {
                        String phone;

                        if (intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) != null) {
                            // 수신 번호 가져옴
                            phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

                            Log.d("qqq", "통화벨 울리는중");
                            Log.d("phone_number", "수신 전화번호: " + phone);


                            try {
                                // 서버에 수신 전화번호 보내서 결과 받아옴
                                gPHP = new GettingPHP();
                                result = gPHP.execute(url + phone).get();
                                Log.d("res_11", result);

                                if (result.length() >= 7) {
                                    String full = result;
                                    String split[] = full.split(":");
                                    String s = split[1];
                                    String s1[] = s.split("]");
                                    Toast.makeText(context, "주의! 신고 {" + s1[0] + "회 누적된 번호입니다.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "깨끗", Toast.LENGTH_LONG).show();
                                }

                            } catch (Exception e) {
                                Log.d("error_e", String.valueOf(e));
                                e.printStackTrace();
                            }
                            Log.d("res_22", result);

                        }
                    }

                }
                // [통화 중]
                else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    Log.d("qqq", "통화중");
                    // Todo: 소켓 통신 받아오기
                    // -> 앱 실행시 스레드로 해결
                }
                // [통화종료]
                else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    Log.d("qqq", "통화종료 혹은 통화벨 종료");
                    // Todo: 팝업창 띄워서 신고 기능 구현하기
//                    // 일단 팝업창 띄워보려고 함
//                    MainActivity ma = new MainActivity();
//                    ma.showDialog();

                    // Todo: 자동 신고
                    // 받아온 판별 결과가 1이라면
                    if(MainActivity.isVP == 1){
                        // 서버에 수신 전화번호 신고
                        gPHP = new GettingPHP();
                        gPHP.execute(reportUrl+phoneNumtoReport);
                    }

                }
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
