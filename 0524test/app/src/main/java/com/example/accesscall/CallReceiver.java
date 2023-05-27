package com.example.accesscall;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class CallReceiver extends BroadcastReceiver {

    String phonestate;
    public GettingPHP gPHP;

    public static final String TAG_phoneState = "PHONE STATE";
    // 보이스피싱용 url
    private String url = "http://118.67.132.20:8080/user/"; // 서버 IP 주소
    public String result = null;

    // 신고용 url
    String reportUrl = "http://118.67.132.20:8080/report/";

    // 신고용 전역 휴대폰번호
    String phoneNumtoReport;

    // 안심번호 데이터 adapter
    PhoneNumInfoAdapter adapter = new PhoneNumInfoAdapter();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")

    MainActivity sttManager = new MainActivity();

    @Override
    public void onReceive(Context context, Intent intent) {
        //구 팝업창 관련 처리. 다른 기능 완성 후에도 팝업창 생성에 문제 없으면 제거해도 OK
        //AlertWindow alertWindow = (AlertWindow) context;
        //alertWindow.setOverlayVisibility(View.VISIBLE);

        // 진동 설정
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                // 현재 폰 상태 가져옴
                String state = extras.getString(TelephonyManager.EXTRA_STATE);

                // 중복 호출 방지
                if (state.equals(phonestate)) {
                    return;
                } else {
                    phonestate = state;
                }

                // [벨 울리는 중]
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    if (MainActivity.use_set == true) {
                        String phone;
                        if (intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) != null) {
                            // 수신 번호 가져옴
                            phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                            phoneNumtoReport = phone;

                            // 안심번호 판별을 위해 추가----
                            String phone_number = PhoneNumberUtils.formatNumber(phone);

                            if(adapter.phoneNumCheck(phone_number)){
                                Toast.makeText(context, "안심 번호입니다.", Toast.LENGTH_LONG).show();
                                //return;
                            }
                            //----

                            // 서버에 수신 전화번호 보내서 결과 받아옴
                            try {
                                gPHP = new GettingPHP();
                                result = gPHP.execute(url + phone).get();
                                if (result.length() >= 7) {
                                    String full = result;
                                    String split[] = full.split(":");
                                    String s = split[1];
                                    String s1[] = s.split("]");
                                    //Toast.makeText(context, "주의! 신고 {" + s1[0] + "회 누적된 번호입니다.", Toast.LENGTH_LONG).show();

                                    //1차 판별 팝업창 생성
                                    Intent serviceIntent = new Intent(context, AlertWindow.class);
                                    serviceIntent.putExtra(AlertWindow.Number, phone_number);
                                    serviceIntent.putExtra(AlertWindow.isWarning, "주의");
                                    serviceIntent.putExtra(AlertWindow.Count, s1[0]);
                                    context.startService(serviceIntent);
                                } else {
                                    //Toast.makeText(context, "깨끗", Toast.LENGTH_LONG).show();

                                    //1차 판별 팝업창 생성
                                    Intent serviceIntent = new Intent(context, AlertWindow.class);
                                    serviceIntent.putExtra(AlertWindow.Number, phone_number);
                                    serviceIntent.putExtra(AlertWindow.isWarning, "깨끗");
                                    serviceIntent.putExtra(AlertWindow.Count, "0");
                                    context.startService(serviceIntent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // [통화 중]
                else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    if (intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) != null) {
                        // 안심번호 판별을 위해 추가----
                        String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        String phone_number = PhoneNumberUtils.formatNumber(phone);

                        // 안심번호 여부 확인
                        if(adapter.phoneNumCheck(phone_number)){
                            Toast.makeText(context, "안심 번호입니다.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        MainActivity activity = MainActivity.getInstance();
                        activity.StartRecord();

                        // Todo: 제대로 작동하는지 확인해봐야함!
                        if(sttManager.isVP == 1){
                            Toast.makeText(context, "보이스피싱 의심 전화입니다 !", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                // [통화종료]
                else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    if (intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) != null) {
                        String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        phoneNumtoReport = phone;

                        System.out.println("통화 종료 확인");

                        sttManager.StopRecord();

                        // 안심번호 판별을 위해 추가----
                        String phone_number = PhoneNumberUtils.formatNumber(phone);

                        if(adapter.phoneNumCheck(phone_number)){
                            Toast.makeText(context, "안심 번호입니다.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //----

                        if (sttManager.isVP == 1) {
                            // 서버에 수신 전화번호 신고
                            gPHP = new GettingPHP();
                            gPHP.execute(reportUrl+phoneNumtoReport);
                            Toast.makeText(context, "보이스피싱 주의! 서버에 자동 신고되었습니다.", Toast.LENGTH_LONG).show();
                            // 진동 설정
                            vibrator.vibrate(1000);
                        } else {

                        }
                    }
                }
            }
        }
    }

    // 서버 연동
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
