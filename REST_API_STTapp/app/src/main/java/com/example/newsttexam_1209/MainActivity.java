package com.example.newsttexam_1209;

import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

//import java.net.URLEncoder;

/**
 *
 * Activity:
 * 앱 안의 단일 화면.
 * Activity 의 새 Instance 를 시작하기 위해 Intent 를 startActivity()로 전달할 수 있다.
 * 이때 Intent 는 시작할 Activity 를 설명하고, 필수 데이터를 모두 담는다.
 *
 *
 * STT 결과가 250자 이상이면 서버로 송신하는 코드.
 * */
public class MainActivity extends AppCompatActivity {

    // 인텐트 선언
    Intent intent;

    // STT Recognizer 선언
    SpeechRecognizer speechRecognizer;

    // UI 요소 선언
    Button sttBtn;
    EditText recodingtxtView;
    EditText resultView;

    // 권한 여부 판단을 위한 변수 선언
    final int PERMISSION = 1;

    // 현재 녹음중인지 여부
    boolean recording = false;


    // 큐 선언
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 빌드 버전에 따라 권한 요청
        CheckPermission();

        // UI 설정
        recodingtxtView = findViewById(R.id.contentsTextView);
        resultView = findViewById(R.id.contentsTextView2);
        sttBtn = findViewById(R.id.sttStart);

        // 인텐트 생성: 음성녹음 - RecognizerIntent 객체 생성
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        // 버튼 클릭 동작 리스너
        sttBtn.setOnClickListener(v -> {
            if (!recording) {   //녹음 시작
                StartRecord();
                Toast.makeText(getApplicationContext(), "지금부터 음성으로 기록합니다.", Toast.LENGTH_SHORT).show();
            } else {  //이미 녹음 중이면 녹음 중지
                StopRecord();
                // Volley의 요청을 취소하고, 요청 큐를 비워주는 코드
                onDestroy();
            }
        });

        if(queue != null) {
            queue = Volley.newRequestQueue(getApplicationContext());
        } //RequestQueue 생성

    }

    /**
     * SpeechRecognizer에 RecognitionListener를 할당
     * -> RecognitionListener가 recognition 관련 이벤트를 수신
     * startListening(), stopListening() 호출 전에 setRecognitionListener()가 선행되어야 함
     * <p>
     * SpeechRecognizer와 RecognitionListener의 동작은 메인 스레드에서 이루어져야 함
     */
    // 녹음 시작
    void StartRecord() {
        recording = true;

        // UX
        sttBtn.setText("텍스트 변환 중지");

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(listener);
        // 녹음 시작
        speechRecognizer.startListening(intent);
//        Toast.makeText(getApplicationContext(), "음성 기록을 시작합니다.", Toast.LENGTH_SHORT).show();
    }

    // 녹음 중지
    void StopRecord() {
        recording = false;

        // UX
        sttBtn.setText("텍스트 변환 시작");

        // 녹음 중지
        speechRecognizer.stopListening();
        Toast.makeText(getApplicationContext(), "음성 기록을 중지합니다.", Toast.LENGTH_SHORT).show();
    }

    // 퍼미션 체크
    void CheckPermission() {
        // 안드로이드 버전이 6.0 이상인 경우
        if (Build.VERSION.SDK_INT >= 23) {
            // 인터넷이나 녹음 권한이 없으면 권한 요청
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                        Manifest.permission.RECORD_AUDIO}, PERMISSION);
            }
        }
    }

    // 리스너
    // 사용자가 말을 멈추면 onEndOfSpeech() 호출
    // 에러가 발생했다면 onError()
    // 음성인식 결과가 제대로 나왔다면 onResults() 호출
    private RecognitionListener listener = new RecognitionListener() {
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
            // 말을 하면 ArrayList 에 단어를 넣고 textView 에 단어를 이어줍니다.
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String originText = recodingtxtView.getText().toString();  //기존 text

            String newText = "";
            for (int i = 0; i < matches.size(); i++) {
                // 여기서 텍스트뷰에 표시랑 동시에 보내주면?
                newText += matches.get(i);
//                textView.setText(matches.get(i));
            }

            recodingtxtView.setText(originText + newText + " ");    //기존의 text에 인식 결과를 이어붙임
            String finalNewText = recodingtxtView.getText().toString();

            // Todo: 적절한 길이 찾기. 250은 너무 긴 것 같아서 일단 줄여둠
            // 텍스트 길이가 200 이상이 되면
            if (recodingtxtView.length() > 200) {
                System.out.println("글자 충족");
                sendRequest(finalNewText);
                System.out.println("글자 보냄");
                // 텍스트 초기화(이미 보냈으니)
                recodingtxtView.setText("");
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
//                tr.interrupt();
            }
            // 녹음버튼을 누를 때까지 계속 녹음해야 하므로 녹음 재개
            speechRecognizer.startListening(intent);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    public void sendRequest(String txt) {
//        String num=URLEncoder.encode("지리산 호랑이", "UTF-8");
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
////                         str -> int 변경
//                         result = Integer.parseInt(ans);
                        //응답
                        resultView.setText(ans);
                        System.out.println("응답 받음");
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



//class ExampleThread implements Runnable {
//    private String name;
//    private boolean exitThread;
//    Thread thread;
//
//    ExampleThread(String name) {
//        this.name = name;
//        thread = new Thread(this, name);
//        System.out.println("Created Thread: " + thread);
//        exitThread = false;
//        thread.start();
//    }
//
//    @Override
//    public void run() {
//
//        while (!exitThread) {
//            System.out.println(name + " is running");
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        System.out.println(name + " has been Stopped.");
//    }
//
//
//    public void println(String data) {
//        recodingtxtView.setText(data +"\n");
//    }
//}

//    public void stopThread() {
//        exitThread = true;
//    }
//}