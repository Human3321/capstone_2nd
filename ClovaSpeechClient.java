package com.example.accesscall;

import android.util.Log;
import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.toolbox.HttpHeaderParser;
import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClovaSpeechClient {

    // Clova Speech secret key
    private static final String SECRET = "ec68bdf9fba74e65974a8ca5732d86d9"; // Clova Speech secret key
    private static final String INVOKE_URL = "https://clovaspeech-gw.ncloud.com/external/v1/5296/ff966b1eff052fd37622d1442e6612c02c06621268c19c3d55af000128465866"; // Clova Speech invoke URL

    private RequestQueue requestQueue;
    private Gson gson;

    private static final Map<String, String> HEADERS = new HashMap<String, String>() {
        {
            put("Accept", "application/json");
            put("X-CLOVASPEECH-API-KEY", SECRET);
        }
    };

    public static class Boosting {
        private String words;

        public String getWords() {
            return words;
        }

        public void setWords(String words) {
            this.words = words;
        }
    }

    public static class Diarization {
        private Boolean enable = Boolean.FALSE;
        private Integer speakerCountMin;
        private Integer speakerCountMax;

        public Boolean getEnable() {
            return enable;
        }

        public void setEnable(Boolean enable) {
            this.enable = enable;
        }

        public Integer getSpeakerCountMin() {
            return speakerCountMin;
        }

        public void setSpeakerCountMin(Integer speakerCountMin) {
            this.speakerCountMin = speakerCountMin;
        }

        public Integer getSpeakerCountMax() {
            return speakerCountMax;
        }

        public void setSpeakerCountMax(Integer speakerCountMax) {
            this.speakerCountMax = speakerCountMax;
        }
    }

    public static class NestRequestEntity {
        private String language = "ko-KR";
        //completion optional, sync/async
        private String completion = "sync";
        //optional, used to receive the analyzed results
        private String callback;
        //optional, any data
        private Map<String, Object> userdata;
        private Boolean wordAlignment = Boolean.TRUE;
        private Boolean fullText = Boolean.TRUE;
        //boosting object array
        private List<Boosting> boostings;
        //comma separated words
        private String forbiddens;
        private Diarization diarization = new Diarization();

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getCompletion() {
            return completion;
        }

        public void setCompletion(String completion) {
            this.completion = completion;
        }

        public String getCallback() {
            return callback;
        }

        public Boolean getWordAlignment() {
            return wordAlignment;
        }

        public void setWordAlignment(Boolean wordAlignment) {
            this.wordAlignment = wordAlignment;
        }

        public Boolean getFullText() {
            return fullText;
        }

        public void setFullText(Boolean fullText) {
            this.fullText = fullText;
        }

        public void setCallback(String callback) {
            this.callback = callback;
        }

        public Map<String, Object> getUserdata() {
            return userdata;
        }

        public void setUserdata(Map<String, Object> userdata) {
            this.userdata = userdata;
        }

        public String getForbiddens() {
            return forbiddens;
        }

        public void setForbiddens(String forbiddens) {
            this.forbiddens = forbiddens;
        }

        public List<Boosting> getBoostings() {
            return boostings;
        }

        public void setBoostings(List<Boosting> boostings) {
            this.boostings = boostings;
        }

        public Diarization getDiarization() {
            return diarization;
        }

        public void setDiarization(Diarization diarization) {
            this.diarization = diarization;
        }
    }

    public ClovaSpeechClient(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
        gson = new Gson();
        this.gson = new Gson();
    }

    /**
     * recognize media using Object Storage
     *
     * @param dataKey           required, the Object Storage key
     * @param nestRequestEntity optional
//     * @param listener          response listener
     */
    public void objectStorage(String dataKey, NestRequestEntity nestRequestEntity, Context context) {
        String endpoint = INVOKE_URL + "/recognizer/object-storage";
        System.out.println("objectStorage endpoint 태그: " + endpoint);
        JSONObject body = new JSONObject();
        System.out.println("JSONObject body 생성 태그");
        try {
            body.put("dataKey", dataKey);
            body.put("language", nestRequestEntity.getLanguage());
            body.put("completion", nestRequestEntity.getCompletion());
            body.put("callback", nestRequestEntity.getCallback());
            body.put("userdata", nestRequestEntity.getUserdata());
            body.put("wordAlignment", nestRequestEntity.getWordAlignment());
            body.put("fullText", nestRequestEntity.getFullText());
            body.put("forbiddens", nestRequestEntity.getForbiddens());
            body.put("boostings", new Gson().toJson(nestRequestEntity.getBoostings()));
            body.put("diarization", new Gson().toJson(nestRequestEntity.getDiarization()));
        } catch (Exception e) {
            System.out.println("JSONObject body put 에러 태그" + e);
        }
        System.out.println("JSONObject body put 완료 태그"
                + " dataKey : " + dataKey
                + " language : " + nestRequestEntity.getLanguage()
                + " completion : " + nestRequestEntity.getCompletion()
                + " callback : " + nestRequestEntity.getCallback()
                + " userdata : " + nestRequestEntity.getUserdata()
                + " wordAlignment : " + nestRequestEntity.getWordAlignment()
                + " fullText : " + nestRequestEntity.getFullText()
                + " forbiddens : " + nestRequestEntity.getForbiddens()
                + " boostings : " + nestRequestEntity.getBoostings()
                + " diarization : " + nestRequestEntity.getDiarization()
        );

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                endpoint,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String result = response.toString();
                        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                        // JSON 응답 처리
                        System.out.println("ClovaSpeechClient 태그 "+ response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("ClovaSpeechClient objectStorage Error 태그: 에러 " + error.getMessage()
                                +" url : "+endpoint);
                        System.out.println("ClovaSpeechClient objectStorage Error 태그: 바디 "+body.toString());
                    }
                }) {


            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-CLOVASPEECH-API-KEY", SECRET);
                System.out.println("ClovaSpeechClient headers 태그");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new VolleyError(e));
                }
            }

            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

        };

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        System.out.println("ClovaSpeechClient requestQueue 태그");
        requestQueue.add(request);
        System.out.println("ClovaSpeechClient requestQueue.add 태그");
    }

    /**
     * recognize media using a file
     *
     * @param file              required, the media file
     * @param nestRequestEntity optional
     */
    public void upload(File file, NestRequestEntity nestRequestEntity, Context context, final UploadCallback callback) {
        String endpoint = INVOKE_URL + "/recognizer/upload";

        VolleyMultipartRequest  request = new VolleyMultipartRequest (
                Request.Method.POST,
                endpoint, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //
                            String test = response.toString();
                            String tst = new String(test.getBytes("ISO-8859-1"), "UTF-8");
                            System.out.println("태그"+tst);
                            String textEdited = response.optString("text");
                            System.out.println("ClovaSpeechClient 응답 성공 태그 "+response.toString());
                            String decodedResponse = new String(textEdited.getBytes("ISO-8859-1"), "UTF-8");
                            callback.onSuccess(decodedResponse);
                        } catch (Exception e) {
                            System.out.println("Json 파싱+디버깅 실패"+ e);
                            callback.onError(e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        System.out.println("ClovaSpeechClient upload Error 태그: " + error.getMessage());
                        System.out.println("ClovaSpeechClient upload Error 태그: url " + endpoint);
                    }
                });
        // Set headers
        Map<String, String> headers = new HashMap<>();
        headers.put("X-CLOVASPEECH-API-KEY", SECRET);
        request.setHeaders(headers);
        System.out.println("ClovaSpeechClient headers 태그");
        System.out.println("ClovaSpeechClient headers 태그"+headers.get("X-CLOVASPEECH-API-KEY"));
        System.out.println("enabla 태그"+nestRequestEntity.getDiarization().getEnable().toString());
        // Set request body parameters
        request.addStringParam("params", gson.toJson(nestRequestEntity), "application/json");
        System.out.println("ClovaSpeechClient addStringParam 완료 태그");
        request.addFile("media", new VolleyMultipartRequest.DataPart(file.getName(), getFileDataFromPath(file.getAbsolutePath()), "audio/m4a"));
        System.out.println("addFile 완료 태그 ");

        // Add the request to the RequestQueue
        //request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        requestQueue = Volley.newRequestQueue(context); // requestQueue 초기화 필수
        System.out.println("ClovaSpeechClient requestQueue 태그");
        requestQueue.add(request);
        System.out.println("ClovaSpeechClient requestQueue.add 태그");
    }

    private byte[] getFileDataFromPath(String filePath) {
        File file = new File(filePath);
        System.out.println("파일 경로 태그 "+filePath);
        byte[] fileData = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            try {
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    bos.write(buf, 0, readNum);
                }
                fileData = bos.toByteArray();
                bos.close();
                fis.close();
                System.out.println("getFileDataFromPath 종료 태그 ");
            } catch (IOException ex) {
                System.out.println("파일IO 에러 태그 "+ex);
            }
        } catch (Exception e) {
            System.out.println("파일 에러 태그 "+e);
        }
        return fileData;
    }

    // STT 응답 콜백 인터페이스
    public interface UploadCallback {
        void onSuccess(String decodedResponse);
        void onError(String errorMessage);
    }

}
