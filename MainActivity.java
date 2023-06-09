package com.example.forquick;

import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.text.html.ImageView;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {

    private static final int PERMISSIONS_REQUEST = 100;
    long animationDuration = 1000; // 1초

    private static MainActivity instance;
    public static MainActivity getInstance() {
        return instance;
    }

    private static final int PERMISSIONS_REQUEST = 100;

    public static boolean vib_mode; // 알림 진동 설정 (true - o , false - x)
    
    public static boolean use_set; // 사용 설정 (true - ON , false - OFF)

    // 수신에 사용할 IP 주소
    String IP = "54.180.140.78";
    // 포트 번호
    int Port = 59082;

    // 판별 결과
    static int isVP = 0;

    ArrayList<PhoneNumInfo> items;

    int i;
    
   // 큐 선언(필수)
   RequestQueue queue;


    // sharepref
    String state;

    String vibState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_set_use = (Button) findViewById(R.id.btn_set_use); // 어플 사용 설정 버튼
        Button btn_set_vib = (Button) findViewById(R.id.btn_set_vibration); // 진동 알림 설정 버튼
        Button btn_set_vib_txt = (Button) findViewById(R.id.btn_set_vibration_txt); // 진동 알림 설정 버튼 껍데기

        AutoPermissions.Companion.loadAllPermissions(this, PERMISSIONS_REQUEST);
        getCallLog();

        // 진동 알림 설정 버튼 리스너
        btn_set_vib.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String str_btn_vib = btn_set_vib.getText().toString(); // 진동 알림 설정 버튼의 텍스트 변경

                // 진동알림 ON -> OFF
                if(str_btn_vib.equals("ON")){
                    vibState ="OFF";

                    // 진동 설정 버튼과 텍스트 변경
                    tv_set_vibration.setText("OFF");
                    btn_set_vibration.setImageResource(R.drawable.vibration_off);


                    // 버튼 클릭시 애니메이션
                    ValueAnimator animator1 = ObjectAnimator.ofFloat(btn_set_vib, "translationX", 100f,150f,0f); // values 수정 필요
                    ValueAnimator animator3 = ObjectAnimator.ofFloat(btn_set_vib_txt, "translationX",100f,150f,0f);
                    animator1.setDuration(animationDuration);
                    animator3.setDuration(animationDuration);
                    animator1.start();
                    animator3.start();

                    // 알림 진동 설정 끔
                    vib_mode = false;

                }
                // 진동알림 OFF -> ON
                else if(str_btn_vib.equals("OFF")){
                    vibState ="ON";

                    // 진동 설정 버튼과 텍스트 변경
                    tv_set_vibration.setText("ON");
                    btn_set_vibration.setImageResource(R.drawable.vibration_on);

                    // 버튼 클릭시 애니메이션
                    ValueAnimator animator2 = ObjectAnimator.ofFloat(btn_set_vib, "translationX", 100f,150f,0f);
                    ValueAnimator animator4 = ObjectAnimator.ofFloat(btn_set_vib_txt, "translationX",100f,150f,0f);
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
        btn_set_use.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String str_btn = btn_set_use.getText().toString(); // 어플 설정 버튼의 텍스트
                TextView txt = findViewById(R.id.textView); // 어플 설정 버튼 밑 텍스트뷰

                if(str_btn.equals("ON")){ // 클릭 -> 실시간 탐지 OFF

                    state ="OFF";

                    // 버튼 및 텍스트 뷰의 텍스트 변경
                    btn_set_use.setText("OFF");
                    txt.setText("실시간 탐지가 꺼졌습니다.");

                    // 버튼 색 변경 (parseColor 때문에 어플 강제종료 돼서 주석처리 해놓음)
                    /*btn_front.setColor(Integer.parseInt("#B8860B")); // 색상 값 넣으면 오류 뜸 ..
                    btn_back.setColor(Integer.parseInt("#B8860B"));
                    btn_set_use.setBackground(btn_front);
                    btn_set_use_back.setBackground(btn_back);*/







                    btn_set_use.setText("OFF");

                    // 어플 사용 설정 OFF
                    use_set = false;

                }
                else if(str_btn.equals("OFF")){ // 클릭 -> 실시간 탐지 ON

                    state ="ON";

                    // + 휴대폰 권한 받아오기
                    onCheckPermission();

                    // 버튼 및 텍스트 뷰의 텍스트 변경
                    btn_set_use.setText("ON");
                    txt.setText("실시간 탐지 중입니다.");

                    // 버튼 색 변경 (parseColor 때문에 어플 강제종료 돼서 주석처리 해놓음)
                    /*btn_front.setColor(Integer.parseInt("#FF3C97")); // 색상 값 넣으면 오류 뜸 ..
                    btn_back.setColor(Integer.parseInt("#FF3C97"));
                    btn_set_use.setBackground(btn_front);
                    btn_set_use_back.setBackground(btn_back);*/









                    btn_set_use.setText("ON");

                    // 어플 사용 설정 ON
                    use_set = true;

                    i++;
                    getCallLog();

                }
            }
        });
        
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

    // 준범 - SharedPreference

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

    // 어플 최초 실행 시 권한을 받아옴
    public void onCheckPermission(){
        if(ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, READ_PHONE_STATE) && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CALL_LOG)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, SYSTEM_ALERT_WINDOW)){
                Toast.makeText(this, "어플 사용을 위해서는 권한 설정이 필요합니다.", Toast.LENGTH_SHORT).show();

                ActivityCompat.requestPermissions(this,new String[]{READ_PHONE_STATE, READ_CALL_LOG,SYSTEM_ALERT_WINDOW}, PERMISSIONS_REQUEST);
            } else{
                ActivityCompat.requestPermissions(this,new String[]{READ_PHONE_STATE, READ_CALL_LOG,SYSTEM_ALERT_WINDOW}, PERMISSIONS_REQUEST);
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

    
    public void addItem(PhoneNumInfo item){
        if(!items.contains(item)){
            items.add(item);
        }
    }

    public boolean phoneNumCheck(String phoneNum){
        boolean flag = false;

        for(int i = 0; i < items.size(); i++){
            PhoneNumInfo info = items.get(i);

            Log.d("check","item:"+info.getName()+"/"+info.getPhoneNumber()+"phoneNum:"+phoneNum);

            if(info.getPhoneNumber().equals(phoneNum)){
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    public void getCallLog(){
        items = new ArrayList<PhoneNumInfo>();
        
        StringBuffer buf = new StringBuffer();
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,null,null, null, null);

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

            if(callname == null){
                continue;
            }

            if(time.convert(diff, TimeUnit.MILLISECONDS) > 180){
                continue;
            }

            addItem(new PhoneNumInfo(callname, phoneNum));
        }

        Log.d("getCallLog","getCallLog호출");

        for(int i = 0;i<items.size();i++){
            Log.d("item","item["+i+"]:"+items.get(i).getName()+", "+items.get(i).getPhoneNumber());
        }
    }

    @Override
    public void onDenied(int i, @NonNull String[] strings) {

    }

    @Override
    public void onGranted(int i, @NonNull String[] strings) {

    }

    int initialTimeoutMs = 5000; // 초기 타임아웃 값 (5초)
    int maxNumRetries = 3; // 최대 재시도 횟수
    float backoffMultiplier = 1.0f; // 재시도 간격 배수
    RetryPolicy retryPolicy = new DefaultRetryPolicy(initialTimeoutMs, maxNumRetries, backoffMultiplier);

    // STT 텍스트를 바탕으로 서버에 결과 전송
    public void sendRequest(String txt, 
                            final MainActivity.SendCallback callback) {
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
                        try {
                            System.out.println("전송 : " + txt);
                            // parsing
                            System.out.println(response);
                            JSONObject jsonObject = new JSONObject(response);
                            Double per = jsonObject.getDouble("percent");
                            String percent = String.format("%.2f", per);
                            isVP = jsonObject.getInt("answer");
                            System.out.println("응답 받음 태그");
                            System.out.println("결과/확률 태그 : "+isVP+" / "+percent+"%");
                            callback.onSuccess();
                        }
                        catch (Exception e){
                            Double percent = 0.0;
                            isVP = 0;
                            System.out.println("응답 에러 태그");
                            callback.onError(e.getMessage());
                        }
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
        request.setRetryPolicy(retryPolicy); // RetryPolicy 설정
        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        queue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        queue.add(request);
        System.out.println("요청 보냄.");
    };

    public interface SendCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}
