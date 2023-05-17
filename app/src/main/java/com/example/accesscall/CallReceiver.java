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
    String reportUrl = "http://13.124.192.194:54103/report/";

    // 신고용 전역 휴대폰번호
    String phoneNumtoReport;

    // 안심번호 데이터 adapter
    PhoneNumInfoAdapter adapter = new PhoneNumInfoAdapter();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")

    @Override
    public void onReceive(Context context, Intent intent) {
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
                                return;
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
                                    Toast.makeText(context, "주의! 신고 {" + s1[0] + "회 누적된 번호입니다.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "깨끗", Toast.LENGTH_LONG).show();
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

                        if(adapter.phoneNumCheck(phone_number)){
                            Toast.makeText(context, "안심 번호입니다.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //----

                        Toast.makeText(context, "보이스피싱 의심 전화입니다 !", Toast.LENGTH_LONG).show();

                        // 소켓 통신 스레드
                        Thread t = new Thread(() -> {
                            Socket clientSocket = new Socket();
                            InetSocketAddress ipep = new InetSocketAddress(MainActivity.IP, MainActivity.Port);

                            try {
                                clientSocket.connect(ipep);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // 소켓이 접속이 완료되면 inputstream과 outputstream을 받는다.
                            try (InputStream receiver = clientSocket.getInputStream();) {
                                byte[] datalength = new byte[4];
                                // 데이터 길이를 받는다.
                                receiver.read(datalength, 0, 4);

                                // ByteBuffer를 통해 little 엔디언 형식으로 데이터 길이를 구한다.
                                ByteBuffer b = ByteBuffer.wrap(datalength);
                                b.order(ByteOrder.LITTLE_ENDIAN);
                                int length = b.getInt();

                                // 데이터를 받을 버퍼를 선언한다.
                                byte [] data = new byte[length];
                                // 데이터를 받는다.
                                receiver.read(data, 0, length);

                                // byte형식의 데이터를 string형식으로 변환한다.
                                String msg = new String(data, "UTF-8");
                                // 스트링 변환 이후 int로 변환(= 최종 값)
                                int msg1 = parseInt(msg);

                                MainActivity.isVP = msg1;
                                System.out.println(msg1);
                            }
                            catch (Throwable e) {
                                e.printStackTrace();
                            }
                        });

                        t.start();
                        // Thread.State state = t.getState()
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(MainActivity.isVP == 1){
                            Toast.makeText(context, "보이스피싱 의심 전화입니다 !", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                // [통화종료]
                else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    if (intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) != null) {
                        String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        phoneNumtoReport = phone;

                        // 안심번호 판별을 위해 추가----
                        String phone_number = PhoneNumberUtils.formatNumber(phone);

                        if(adapter.phoneNumCheck(phone_number)){
                            Toast.makeText(context, "안심 번호입니다.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //----

                        if (MainActivity.isVP == 1) {
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
