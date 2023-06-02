package com.example.accesscall;

import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.protobuf.Value;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1; //다른 앱 위에 그리기(팝업창)
    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }
    private static final int PERMISSIONS_REQUEST = 100;
    long animationDuration = 1000; // 1초

    public boolean vib_mode; // 알림 진동 설정 (true - o , false - x)
    public boolean use_set; // 사용 설정 (true - ON , false - OFF)

    // 판별 결과
    int isVP = 0;

    int i = 0; // 안심번호 Load 테스트용
    PhoneNumInfoAdapter adapter;

    // 현재 녹음중인지 여부
    boolean recording = false;

    // 큐 선언(필수)
    RequestQueue queue;

    // 인텐트 선언
    Intent intent;

    // STT Recognizer 선언
    SpeechRecognizer speechRecognizer;

    // sharepref
    String state;
    String vibState;

    ImageView iv_back;  //어플 배경
    ImageView iv_title, iv_set_use; // 어플 설정 On/Off 버튼 위 이미지, On/Off 버튼 배경
    Button btn_set_use; // 어플 설정 On/Off 버튼
    ImageButton btn_set_vibration;  // 진동 설정 버튼
    TextView tv_set_vibration, tv_status;   // On/Off 버튼 배경 텍스트, 진동 설정 텍스트, 어플 설정 텍스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //세로 고정
        instance = this;
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("Fetching FCM registration token failed");
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        System.out.println(token);
//                        Toast.makeText(MainActivity.this, "Your device registration token is " + token, Toast.LENGTH_SHORT).show();

                    }
                });

        iv_back = (ImageView) findViewById(R.id.iv_back); //어플 배경 이미지

        iv_title = (ImageView) findViewById(R.id.iv_title); //어플 사용 설정 버튼 위 이미지
        btn_set_use = (Button) findViewById(R.id.btn_set_use);  // 어플 사용 설정 버튼
        iv_set_use = (ImageView) findViewById(R.id.iv_set_use);    //어플 사용 설정 버튼 배경
        tv_status = (TextView) findViewById(R.id.tv_status);    //어플 사용 설정 상태 텍스트

        btn_set_vibration = (ImageButton) findViewById(R.id.btn_set_vibration); //진동 설정 버튼
        tv_set_vibration = (TextView) findViewById(R.id.tv_set_vibration);  //진동 설정 텍스트

        adapter = new PhoneNumInfoAdapter();

        AutoPermissions.Companion.loadAllPermissions(this, PERMISSIONS_REQUEST);
        getCallLog();

        // 진동 알림 설정 버튼 리스너
        btn_set_vibration.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String str_btn_vib = tv_set_vibration.getText().toString(); // 진동 알림 설정 텍스트 변경

                // 진동알림 ON -> OFF
                if (str_btn_vib.equals("ON")) {
                    vibState ="OFF";

                    // 진동 설정 버튼과 텍스트 변경
                    tv_set_vibration.setText("OFF");
                    btn_set_vibration.setImageResource(R.drawable.vibration_off);

                    // 버튼 클릭시 애니메이션
                    /*
                    ValueAnimator animator1 = ObjectAnimator.ofFloat(btn_set_vib, "translationX", 100f, 150f, 0f); // values 수정 필요
                    ValueAnimator animator3 = ObjectAnimator.ofFloat(btn_set_vib_txt, "translationX", 100f, 150f, 0f);
                    animator1.setDuration(animationDuration);
                    animator3.setDuration(animationDuration);
                    animator1.start();
                    animator3.start();
                    */

                    // 알림 진동 설정 끔
                    vib_mode = false;
                }
                // 진동알림 OFF -> ON
                else if (str_btn_vib.equals("OFF")) {
                    vibState ="ON";

                    // 진동 설정 버튼과 텍스트 변경
                    tv_set_vibration.setText("ON");
                    btn_set_vibration.setImageResource(R.drawable.vibration_on);

                    // 버튼 클릭시 애니메이션
                    /*
                    ValueAnimator animator2 = ObjectAnimator.ofFloat(btn_set_vib, "translationX", 100f, 150f, 0f);
                    ValueAnimator animator4 = ObjectAnimator.ofFloat(btn_set_vib_txt, "translationX", 100f, 150f, 0f);
                    animator2.setDuration(animationDuration);
                    animator4.setDuration(animationDuration);
                    animator2.start();
                    animator4.start();
                    */

                    // 알림 진동 설정 켬
                    vib_mode = true;

                    // 1. Vibrator 객체 생성
                    Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

                    // 2. 진동 구현: 1000ms동안 100 강도의 진동
                    vibrator.vibrate(VibrationEffect.createOneShot(1000,100));
                }
            }
        });

        // 어플 설정 버튼 리스너
        btn_set_use.setOnClickListener(view -> {
            String str_btn = btn_set_use.getText().toString(); // 어플 설정 버튼의 텍스트

            if (str_btn.equals("ON")) { // 클릭 -> 실시간 탐지 OFF
                // 껏다 켜도 상태 저장 용도
                state ="OFF";

                //어플 배경 이미지 변경
                iv_back.setBackgroundResource(R.drawable.roundbtn_back_list_off);
                AnimationDrawable animationDrawable_back = (AnimationDrawable) iv_back.getBackground();
                animationDrawable_back.setEnterFadeDuration(500);
                animationDrawable_back.setExitFadeDuration(500);
                animationDrawable_back.start();

                // 어플 설정 버튼 및 텍스트 변경
                iv_title.setBackgroundResource(R.drawable.roundbtn_list_off);
                AnimationDrawable animationDrawable = (AnimationDrawable) iv_title.getBackground();
                animationDrawable.setEnterFadeDuration(500);
                animationDrawable.setExitFadeDuration(500);
                animationDrawable.start();

                ObjectAnimator animator_btn = ObjectAnimator.ofFloat(btn_set_use, "translationX", 0);
                animator_btn.setDuration(animationDuration);
                animator_btn.start();
                btn_set_use.setText("OFF");

                tv_status.setText("보이스피싱 탐지 기능이 꺼졌습니다.");

                // 어플 사용 설정 OFF
                use_set = false;

            } else if (str_btn.equals("OFF")) { // 클릭 -> 실시간 탐지 ON
                // 껏다 켜도 상태 저장 용도
                state ="ON";

                // 팝업창 권한
                onCheckPermission();

                //어플 배경 이미지 변경
                iv_back.setBackgroundResource(R.drawable.roundbtn_back_list_on);
                AnimationDrawable animationDrawable_back = (AnimationDrawable) iv_back.getBackground();
                animationDrawable_back.setEnterFadeDuration(500);
                animationDrawable_back.setExitFadeDuration(500);
                animationDrawable_back.start();

                // 어플 설정 버튼 및 텍스트 변경
                iv_title.setBackgroundResource(R.drawable.roundbtn_list_on);
                AnimationDrawable animationDrawable = (AnimationDrawable) iv_title.getBackground();
                animationDrawable.setEnterFadeDuration(500);
                animationDrawable.setExitFadeDuration(500);
                animationDrawable.start();

                // 이동 거리 계산 (100dp를 픽셀로 변환)
                float distanceInDp = 100f;
                float scale = getResources().getDisplayMetrics().density;
                int distanceInPixels = (int) (distanceInDp * scale + 0.5f);

                ObjectAnimator animator_btn = ObjectAnimator.ofFloat(btn_set_use, "translationX", distanceInPixels);
                animator_btn.setDuration(animationDuration);
                animator_btn.start();
                btn_set_use.setText("ON");

                tv_status.setText("보이스피싱 탐지 중입니다.");

                // 어플 사용 설정 ON
                use_set = true;

                i++;
                getCallLog();
            }
        });

        // 인텐트 생성: 음성녹음 - RecognizerIntent 객체 생성
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        if(queue != null) {
            queue = Volley.newRequestQueue(getApplicationContext());
        } //RequestQueue 생성
    }

    private void saveState(){
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("state", state);
        editor.putString("vibState", vibState);
        editor.commit();
        Log.d("tag","저장됨"+state);
        Log.d("tag","저장됨"+vibState);
    }

    private void loadState(){
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        state = pref.getString("state",null);
        vibState = pref.getString("vibState", null);
        Log.d("tag","로드-"+state);
        Log.d("tag","로드-"+vibState);

        iv_title = (ImageView) findViewById(R.id.iv_title); //어플 사용 설정 버튼 위 이미지
        btn_set_use = (Button) findViewById(R.id.btn_set_use);  // 어플 사용 설정 버튼
        iv_set_use = (ImageView) findViewById(R.id.iv_set_use);    //어플 사용 설정 버튼 배경
        tv_status = (TextView) findViewById(R.id.tv_status);    //어플 사용 설정 상태 텍스트

        btn_set_vibration = (ImageButton) findViewById(R.id.btn_set_vibration); //진동 설정 버튼
        tv_set_vibration = (TextView) findViewById(R.id.tv_set_vibration);  //진동 설정 텍스트

        if(state != null){
            if(state.contains("ON")){   // 저장된 어플 사용 설정이 ON이라면
                // 팝업창 권한
                onCheckPermission();

                //어플 배경 이미지 변경
                iv_back.setBackgroundResource(R.drawable.roundbtn_back_on);

                // 어플 설정 버튼 및 텍스트 변경
                iv_title.setBackgroundResource(R.drawable.roundbtn_on);

                // 이동 거리 계산 (100dp를 픽셀로 변환)
                float distanceInDp = 100f;
                float scale = getResources().getDisplayMetrics().density;
                int distanceInPixels = (int) (distanceInDp * scale + 0.5f);

                ObjectAnimator animator_btn = ObjectAnimator.ofFloat(btn_set_use, "translationX", distanceInPixels);
                animator_btn.setDuration(animationDuration);
                animator_btn.start();
                btn_set_use.setText("ON");

                tv_status.setText("보이스피싱 탐지 중입니다.");

                // 어플 사용 설정 ON
                use_set = true;

                getCallLog();
            }
            else if(state.contains("OFF")){ // 저장된 어플 사용 설정이 OFF라면
                //어플 배경 이미지 변경
                iv_back.setBackgroundResource(R.drawable.roundbtn_back_off);

                // 어플 설정 버튼 및 텍스트 변경
                iv_title.setBackgroundResource(R.drawable.roundbtn_off);

                ObjectAnimator animator_btn = ObjectAnimator.ofFloat(btn_set_use, "translationX", 0);
                animator_btn.setDuration(animationDuration);
                animator_btn.start();
                btn_set_use.setText("OFF");

                tv_status.setText("보이스피싱 탐지 기능이 꺼졌습니다.");

                // 어플 사용 설정 OFF
                use_set = false;
            }
        }

        if(vibState != null){
            if(vibState.contains("ON")){
                // 진동 설정 버튼과 텍스트 변경
                tv_set_vibration.setText("ON");
                btn_set_vibration.setImageResource(R.drawable.vibration_on);

                // 알림 진동 설정 켬
                vib_mode = true;
            }
            else if(vibState.contains("OFF")){
                // 진동 설정 버튼과 텍스트 변경
                tv_set_vibration.setText("OFF");
                btn_set_vibration.setImageResource(R.drawable.vibration_off);

                // 알림 진동 설정 끔
                vib_mode = false;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("tag","onPause 호출됨");
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("tag","onResume 호출됨");
        loadState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("tag","onDestroy 호출됨");
        saveState();
    }

    /* 팝업창 관련 함수들.. */

    void onCheckPermission() {
        //팝업창 권한 처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                //startService(new Intent(MainActivity.this, AlertWindow.class));
            }
        } else {
            //startService(new Intent(MainActivity.this, AlertWindow.class));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //이 함수는 없어도 괜찮을듯? 모르겠음.. 주석 처리 후 테스트해보고 문제 없으면 폐기해도 OK
        //폐기할 때는 제일 첫 문단 변수도 제거할 것.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // TODO 동의를 얻지 못했을 경우의 처리

            }
            else {
                //startService(new Intent(MainActivity.this, AlertWindow.class));
            }
        }
    }

    /* 팝업창 관련 함수들.. END */

    // 최초 1회만 뜨기
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

    public void getCallLog(){
        adapter.newArray();
        StringBuffer buf = new StringBuffer();
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,null,null, null, null);
//        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null, null, null);
        cursor.moveToFirst();

        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int name = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);

        while (cursor.moveToNext()) {
            String callname = cursor.getString(name);
            String phoneNum = cursor.getString(number);
            phoneNum = PhoneNumberUtils.formatNumber(phoneNum);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            Date now = new Date();
            long diff = now.getTime() - callDayTime.getTime();
            TimeUnit time = TimeUnit.DAYS;
            if(callname == null){continue;}
            if(time.convert(diff, TimeUnit.MILLISECONDS) > 180){ continue; }
            adapter.addItem(new PhoneNumInfo(callname, phoneNum));
        }
        Log.d("getCallLog","getCallLog호출");

        for(int i = 0;i<adapter.items.size();i++){
            Log.d("item","item["+i+"]:"+adapter.items.get(i).getName()+", "+adapter.items.get(i).getPhoneNumber());
        }
//        cursor.close();
    }

    @Override
    public void onDenied(int i, @NonNull String[] strings) {

    }

    @Override
    public void onGranted(int i, @NonNull String[] strings) {

    }

    // 리스너
    // 사용자가 말을 멈추면 onEndOfSpeech() 호출
    // 에러가 발생했다면 onError()
    // 음성인식 결과가 제대로 나왔다면 onResults() 호출
    public RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
//            Toast.makeText(getApplicationContext(),"음성인식을 시작합니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 오류";
                    break;
                /*case SpeechRecognizer.ERROR_CLIENT:
                    message = "앱 오류";
                    break;*/
                case SpeechRecognizer.ERROR_CLIENT:
                    //message = "클라이언트 에러";
                    //speechRecognizer.stopListening()을 호출하면 발생하는 에러
                    return; //토스트 메세지 출력 X
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "권한이 없습니다.";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트워크 타임아웃";
                    break;
                /*case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "다시 시도해 주세요.";
                    break;*/
                case SpeechRecognizer.ERROR_NO_MATCH:
                    // message = "찾을 수 없음";
                    // 녹음을 오래하거나 speechRecognizer.stopListening()을 호출하면 발생하는 에러
                    // speechRecognizer 다시 생성해 녹음 재개
                    if (recording)
                        StartRecord();
                    return; //토스트 메세지 출력 X
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버 오류";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "대기시간 초과";
                    break;
                default:
                    message = "알 수 없는 오류 발생";
                    break;
            }
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
        }

        // 인식 결과가 준비됐을 때 호출
        // 기존 text 에 인식결과를 이어붙인 text 출력
        @Override
        public void onResults(Bundle results) {
            // 말을 하면 ArrayList에 단어를 넣습니다.
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            StringBuilder stringBuilder = new StringBuilder();

            for (String match : matches) {
                stringBuilder.append(match);
            }

            // 최종 STT 텍스트
            String finalNewText = stringBuilder.toString();

            // 텍스트 길이가 200 이상이 되면
            if (finalNewText.length() >= 20) {
                System.out.println("글자 충족");
                // 전송
                sendRequest(finalNewText);
                System.out.println("글자 보냄");
            }

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    // 녹음 시작
    public void StartRecord() {
        recording = true;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(listener);
        // 녹음 시작
        speechRecognizer.startListening(intent);
        System.out.println("STT 시작");
    }

    // 녹음 중지
    public void StopRecord() {
        recording = false;

        // 녹음 중지
        speechRecognizer.stopListening();
        System.out.println("변환 종료");
    }

    // STT 텍스트를 바탕으로 서버에 결과 전송
    public void sendRequest(String txt) {
        // url 주소
        String url = "http://49.50.172.239:8081/myAI/";
        // url 인코딩
        String encodetxt = "";
        try {
            encodetxt = URLEncoder.encode(txt, "UTF-8");
        }
        catch (Exception e){
            encodetxt = "URLerror";
        }
        url += "5/"+encodetxt+"/";
        System.out.println("전송 : "+url);
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        System.out.println("전송 : "+txt);
                        // parsing
                        String full = response;
                        String split[] = full.split(":");
                        String ans = split[1].substring(0, split[1].length() - 1);
                        isVP = Integer.parseInt(ans);
                        System.out.println("응답 받음");
                        System.out.println(isVP);
                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("전송 응답 에러 : " + error.toString());
                        //에러
                        Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        System.out.println("요청 준비");
        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        queue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        queue.add(request);
        System.out.println("요청 보냄.");
    };
}