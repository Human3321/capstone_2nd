package com.example.accesscall;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telecom.Call;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccessivilityTest extends AccessibilityService {

    MainActivity sttManager = new MainActivity();
    private SpeechRecognizer speechRecognizer;
    private RecognitionListener recognitionListener;

    @Override
    public void onCreate() {
        super.onCreate();

        // SpeechRecognizer 초기화
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognitionListener = new RecognitionListener() {
            // STT 결과 및 이벤트 처리 로직 구현
            // onResults() 메서드 등을 사용하여 STT 결과를 가져올 수 있습니다.
            @Override
            public void onReadyForSpeech(Bundle params) {
                // STT 준비가 완료된 경우 호출되는 메서드
            }

            @Override
            public void onBeginningOfSpeech() {
                // 음성 인식 시작 시 호출되는 메서드
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // 음성 신호의 RMS 값이 변경될 때 호출되는 메서드
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // 오디오 버퍼를 수신할 때 호출되는 메서드
            }

            @Override
            public void onEndOfSpeech() {
                // 음성 인식 종료 시 호출되는 메서드
            }

            @Override
            public void onError(int error) {
                // 음성 인식 중 오류 발생 시 호출되는 메서드
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
                        if (sttManager.recording)
                            startSpeechRecognition();
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
                System.out.println("에러가 발생하였습니다. :" + message);
                Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                // 음성 인식 결과를 받았을 때 호출되는 메서드
                // 말을 하면 ArrayList에 단어를 넣습니다.
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                StringBuilder stringBuilder = new StringBuilder();

                for (String match : matches) {
                    stringBuilder.append(match);
                }
                // 최종 STT 텍스트

                String finalNewText = stringBuilder.toString();

                // 텍스트 길이가 200 이상이 되면
                if (finalNewText.length() >= 40) {
                    System.out.println("글자 충족");
                    // 전송
//                    sttManager.sendRequest(finalNewText);
                    System.out.println("글자 보냄");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // 부분적인 음성 인식 결과를 받았을 때 호출되는 메서드
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // 추가 이벤트를 수신할 때 호출되는 메서드
            }
        };
        speechRecognizer.setRecognitionListener(recognitionListener);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 접근성 이벤트 처리 코드를 추가합니다.
        // 통화 이벤트를 감지하여 마이크 접근 및 STT 실행
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && isCallActive()) {
            // 마이크 접근 및 STT 실행 코드
            startSpeechRecognition();
        }
    }

    private boolean isCallActive() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            return telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK;
        }
        return false;
    }

    private void startSpeechRecognition() {
        // 마이크 접근 및 STT 실행 로직
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        System.out.println("접근성 STT 시작");

        speechRecognizer.startListening(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // SpeechRecognizer 리소스 해제
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public void onInterrupt() {
        // 접근성 서비스가 중단되었을 때의 처리 코드를 추가합니다.
    }
}