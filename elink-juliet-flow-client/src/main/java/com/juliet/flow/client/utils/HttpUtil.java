package com.juliet.flow.client.utils;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    private static OkHttpClient httpsClient = new OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())//配置
            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())//配置
            .build();

    public static String postJson(String url, String json) throws IOException {
        return postJson(url, json, null);
    }

    public static String postJson(String url, String json, Map<String, String> header) throws IOException {
        if (json == null) {
            json = "";
        }
        RequestBody body = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder();

        if (header != null) {
            for (String key : header.keySet()) {
                builder.addHeader(key, header.get(key));
            }
        }

        Request request = builder
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        }
        throw new IOException("Unexpected code " + response);
    }

    public static String putJson(String url, String json, Map<String, String> header) throws IOException {
        Request.Builder builder = new Request.Builder();

        if (header != null) {
            for (String key : header.keySet()) {
                builder.addHeader(key, header.get(key));
            }
        }

        RequestBody body = RequestBody.create(JSON, json);

        Request request = builder
                .url(url)
                .put(body)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        }
        throw new IOException("Unexpected code " + response);
    }

    public static String deleteJson(String url, String json, Map<String, String> header) throws IOException {
        Request.Builder builder = new Request.Builder();

        if (header != null) {
            for (String key : header.keySet()) {
                builder.addHeader(key, header.get(key));
            }
        }

        RequestBody body = null;
        if (json != null) {
            body = RequestBody.create(JSON, json);
        }

        Request request = builder
                .url(url)
                .delete(body)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        }
        throw new IOException("Unexpected code " + response);
    }

    public static String postForm(String url, Map<String, String> form) throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        if (form != null) {
            for (String key : form.keySet()) {
                builder.add(key, form.get(key));
            }
        }

        RequestBody body = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        }
        throw new IOException("Unexpected code " + response);
    }

    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        }
        throw new IOException("Unexpected code " + response);
    }

    public static String getWithHeader(String url, Map<String, String> header) {

        Request.Builder builder = new Request.Builder();

        if (header != null) {
            for (String key : header.keySet()) {
                builder.addHeader(key, header.get(key));
            }
        }

        Request request = builder
                .url(url)
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    public static String getHttpsWithHeader(String url, Map<String, String> header) {

        Request.Builder builder = new Request.Builder();

        if (header != null) {
            for (String key : header.keySet()) {
                builder.addHeader(key, header.get(key));
            }
        }

        Request request = builder
                .url(url)
                .get()
                .build();
        try {
            Response response = httpsClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    public static String get(String url, Map<String, String> params) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            return "";
        }

        HttpUrl.Builder builder = httpUrl.newBuilder();
        if (!CollectionUtils.isEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        try {
            return executeGet(builder);
        } catch (IOException e) {
            LOGGER.error("[HttpUtil.get] occur exception,url is {}, params are {},e is", url, params, e);
            return "";
        }
    }

    public static String getWithMap(String url, Map<String, Object> params) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            return "";
        }

        HttpUrl.Builder builder = httpUrl.newBuilder();
        if (!CollectionUtils.isEmpty(params)) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                builder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        try {
            return executeGet(builder);
        } catch (IOException e) {
            LOGGER.error("[HttpUtil.get] occur exception,url is {}, params are {},e is", url, params, e);
            return "";
        }
    }

    private static String executeGet(HttpUrl.Builder builder) throws IOException {
        Request request = new Request.Builder()
                .url(builder.build())
                .get()
                .build();
        LOGGER.info("[HttpUtil.get] request params are {}", request.toString());
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.error("[HttpUtil.get] return unexpected result,message is {}", response.message());
            return "";
        }
        return response.body().string();
    }

}
