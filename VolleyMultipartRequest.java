package com.example.accesscall;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.VolleyError;

public class VolleyMultipartRequest extends JsonObjectRequest {

    private final String boundary = "VolleyMultipartBoundary";
    private final String lineEnd = "\r\n";
    private final String twoHyphens = "--";

    private Map<String, String> mStringParams;

    private Response.Listener<JSONObject> mListener;
    private Response.ErrorListener mErrorListener;

    private Map<String, String> mHeaders;
    private Map<String, String> mMultipartParams;
    private Map<String, DataPart> mFileParams;


    public VolleyMultipartRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        mStringParams = new HashMap<>();
        mHeaders = new HashMap<>();
        mMultipartParams = new HashMap<>();
        mFileParams = new HashMap<>();
        mListener = listener;
        mErrorListener = errorListener;
    }

    public void addStringParam(String key, String value, String contentType) {
        mStringParams.put(key, value);
        mStringParams.put(key + ";type=", contentType);
    }

    public void addFile(String key, DataPart dataPart) {
        mFileParams.put(key, dataPart);
    }

    public void setHeaders(Map<String, String> headers) {
        mHeaders = headers;
    }
    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // Add string parameters
            if (!mStringParams.isEmpty()) {
                for (Map.Entry<String, String> entry : mStringParams.entrySet()) {
                    buildTextPart(dos, entry.getKey(), entry.getValue());
                }
            }

            // Add file parameters
            if (!mFileParams.isEmpty()) {
                for (Map.Entry<String, DataPart> entry : mFileParams.entrySet()) {
                    buildFilePart(dos, entry.getKey(), entry.getValue());
                }
            }

            // Mark the end of the request
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(parameterValue + lineEnd);
    }

    private void buildFilePart(DataOutputStream dataOutputStream, String parameterName, DataPart dataPart) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"; filename=\"" + dataPart.getFileName() + "\"" + lineEnd);
        dataOutputStream.writeBytes("Content-Type: " + dataPart.getType() + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataPart.getContent());
        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytesRead);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | org.json.JSONException e) {
            e.printStackTrace();
            return Response.error(new VolleyError(e));
        }
    }

    public static class DataPart {
        private String fileName;
        private byte[] data;
        private String mimeType;

        public DataPart(String fileName, byte[] data, String mimeType) {
            this.fileName = fileName;
            this.data = data;
            this.mimeType = mimeType;
            System.out.println("DataPart 생성 완료 태그 "
                    +" 파일 이름 "+getFileName()
                    +" data : "+getData().toString()
                    +" mimeType : "+getMimeType());
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getData() {
            return data;
        }

        public String getMimeType() {
            return mimeType;
        }

        public byte[] getContent() {
            return data;
        }

        public String getType() {
            // MIME 타입을 반환하는 로직 작성
            return "audio/m4a";
        }

    }
}
