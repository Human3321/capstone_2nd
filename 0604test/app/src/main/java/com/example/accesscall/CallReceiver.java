package com.example.accesscall;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallReceiver extends BroadcastReceiver {

    private String previousState = "";
    public GettingPHP gPHP;

    // 보이스피싱용 url
    private String url = "http://118.67.132.20:8080/services1/user/"; // 서버 IP 주소
    public String result = null;

    // 신고용 url
    String reportUrl = "http://118.67.132.20:8080/services1/report/";

    // 신고용 전역 휴대폰번호
    String phoneNumtoReport;

    //팝업창 Intent
    boolean trigger = false;

    // 안심번호 데이터 adapter
    PhoneNumInfoAdapter adapter = new PhoneNumInfoAdapter();

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

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (phone != null) {
            System.out.println("통화 수신 확인");
            MainActivity.getInstance().isVP = 0;
            // 수신 번호 가져옴
            phoneNumtoReport = phone;

            // 안심번호 판별을 위해 추가----
            String phone_number = PhoneNumberUtils.formatNumber(phone);

            if(adapter.phoneNumCheck(phone_number)){
                //안심 번호 팝업창 생성
                Intent serviceIntent = new Intent(context, AlertWindow.class);
                serviceIntent.putExtra(AlertWindow.Number, phone_number);
                serviceIntent.putExtra(AlertWindow.isWarning, "안심");
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


                // 진동 0.5초
                vibrator.vibrate(VibrationEffect.createOneShot(500,100));

                return;
            }

            // Todo: 지금 여기 동작 안 하는 듯 , url 이랑 7자 이상 및 파싱도 확인
            // 서버에 수신 전화번호 보내서 결과 받아옴
            try {
                gPHP = new CallReceiver.GettingPHP();
                result = gPHP.execute(url + phone).get();
                if (result.length() >= 7) {
                    String full = result;
                    String split[] = full.split(":");
                    String s = split[1];
                    String restr = s.replaceAll("[^0-9]","");
                    //String s1[] = s.split("]");
                    //String s2[] = s1[0].split("}");
                    //1차 판별 팝업창 생성
                    Intent serviceIntent = new Intent(context, AlertWindow.class);
                    serviceIntent.putExtra(AlertWindow.Number, phone_number);
                    serviceIntent.putExtra(AlertWindow.isWarning, "주의");
                    serviceIntent.putExtra(AlertWindow.Percent, "0");
                    serviceIntent.putExtra(AlertWindow.Count, restr);
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

                    // 진동 1초
                    vibrator.vibrate(VibrationEffect.createOneShot(1000,100));
                } else {
                    //1차 판별 팝업창 생성
                    Intent serviceIntent = new Intent(context, AlertWindow.class);
                    serviceIntent.putExtra(AlertWindow.Number, phone_number);
                    serviceIntent.putExtra(AlertWindow.isWarning, "깨끗");
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

                    // 진동 0.5초
                    vibrator.vibrate(VibrationEffect.createOneShot(500,100));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private FileObserver fileObserver;
    // String internalStorageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    String directoryPath = "/storage/emulated/0/Recordings/Call";
    String filePath = null;

    private void startFileObservation() {
        fileObserver = new FileObserver(directoryPath) {
            @Override
            public void onEvent(int event, String path) {
                if (event == FileObserver.CREATE) {
                    // 새 파일이 생성된 경우 처리 로직을 여기에 작성합니다.
                    filePath = directoryPath + "/" + path;

                    // filePath를 처리하는 코드를 추가합니다.
                    System.out.println(filePath);
                }
            }
        };
        fileObserver.startWatching(); // 감시 시작
        System.out.println("파일 감시 시작");
    }

    void setTrue() {
        trigger = true;
    }

    private void handleActiveCall(Context context,Intent intent) {

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (phone != null) {
            System.out.println("통화 중 확인");
            String phone_number = PhoneNumberUtils.formatNumber(phone);

            Intent serviceIntent = new Intent(context, AlertWindow.class);
            serviceIntent.putExtra(AlertWindow.Number, phone_number);
            serviceIntent.putExtra(AlertWindow.isWarning, "백그라운드");
            context.startService(serviceIntent);

            System.out.println("백그라운드 1 실행 완료");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    context.stopService(serviceIntent);
                    System.out.println("백그라운드 1 종료");

                    Intent serviceIntent = new Intent(context, AlertWindow.class);
                    serviceIntent.putExtra(AlertWindow.Number, phone_number);
                    serviceIntent.putExtra(AlertWindow.isWarning, "백그라운드");
                    context.startService(serviceIntent);

                    System.out.println("백그라운드 2 실행");
                }
            }, 30000);

            // 파일 감시 시작
            startFileObservation();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //ToDO : 여러번 받아와도 문제 없는지 확인
                    ClovaSpeechClient clovaSpeechClient = new ClovaSpeechClient(MainActivity.getInstance().getApplicationContext());
                    System.out.println("Claova 객체 생성 태그");
                    ClovaSpeechClient.NestRequestEntity requestEntity = new ClovaSpeechClient.NestRequestEntity();
                    System.out.println("requestEntity 생성 태그");
                    // String relativePath = "Recordings/Voice Recorder/A.m4a";

                    clovaSpeechClient.upload(new File(filePath), requestEntity, MainActivity.getInstance().getApplicationContext(), new ClovaSpeechClient.UploadCallback() {
                        @Override
                        public void onSuccess(String decodedResponse) {
                            // 성공적인 응답 처리
                            System.out.println("ClovaSpeechClient 응답 성공 태그 "+ decodedResponse);
                            /*
                            MainActivity.getInstance().sendRequest(decodedResponse, new MainActivity.SendCallback() {
                                @Override
                                public void onSuccess() {
                                    // Todo: 제대로 작동하는지 확인해봐야함!
                                    if(MainActivity.getInstance().isVP == 1){
                                        //Toast.makeText(context, "보이스피싱 의심 전화입니다!", Toast.LENGTH_SHORT).show();
                                        */
                                        System.out.println("VPIS == 1 태그");

                                        setTrue();

                                        System.out.println("Success 종료 태그");
                                        /*
                                    }
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    System.out.println("callback onError 통신 오류 태그: " + errorMessage);
                                }
                            });
                            */
                            System.out.println("글자 서버로 전송 태그 : "+decodedResponse);
                        }
                        @Override
                        public void onError(String errorMessage) {
                            // 오류 처리
                            System.out.println("ClovaSpeechClient 오류 태그: " + errorMessage);
                        }
                    });
                }
            }, 50000); // 60초 지연

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (trigger) {
                        //백그라운드 팝업창 제거
                        context.stopService(serviceIntent);
                        System.out.println("백그라운드 2 종료");

                        //보이스 피싱 판별 팝업창 생성
                        Intent serviceIntent = new Intent(context, AlertWindow.class);
                        serviceIntent.putExtra(AlertWindow.Number, phone_number);
                        serviceIntent.putExtra(AlertWindow.isWarning, "피싱");
                        serviceIntent.putExtra(AlertWindow.Percent, "99.99");
                        //serviceIntent.putExtra(AlertWindow.Percent, MainActivity.getInstance().percent);
                        context.startService(serviceIntent);

                        System.out.println("팝업 피싱 실행");

                        // Handler 객체 생성
                        Handler handler = new Handler();

                        // 일정 시간 후에 서비스 중지 실행
                        long delayMillis = 5000; // 5초 후에 서비스 중지
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 서비스 중지 코드 추가
                                context.stopService(serviceIntent);
                                System.out.println("팝업 피싱 종료");
                            }
                        }, delayMillis);

                        // 진동 1초
                        vibrator.vibrate(VibrationEffect.createOneShot(1000,100));
                    }
                    else {
                        System.out.println("Not True...");
                    }
                }
            }, 55000);
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
                //Toast.makeText(context, "보이스피싱으로 판별되어 서버에 자동 신고되었습니다.", Toast.LENGTH_LONG).show();

                //보이스 피싱 판별 팝업창 생성
                Intent serviceIntent = new Intent(context, AlertWindow.class);
                serviceIntent.putExtra(AlertWindow.Number, phone_number);
                serviceIntent.putExtra(AlertWindow.isWarning, "신고");
                serviceIntent.putExtra(AlertWindow.Percent, "0");   //신고이므로 0
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

                // 진동 1초
                vibrator.vibrate(VibrationEffect.createOneShot(1000,100));
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