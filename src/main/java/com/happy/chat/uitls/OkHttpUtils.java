package com.happy.chat.uitls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//CHECKSTYLE:OFF
public class OkHttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(OkHttpUtils.class);

    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");


    // CSOFF: MagicNumberCheck

    private static final OkHttpClient DEFAULT_CLIENT = new OkHttpClient().newBuilder()
            .build();

    // CSON: MagicNumberCheck

    public static void postJson(String url, String json) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization",
                        "Basic QUNmNDI2NmMxNWUzZTMyZGY1ZDZlMTNjZjc1NWRiMWJiNzo5NWFjM2VhY2EyMjQ5Y2EwM2QzZjNiM2E1ZmUyZDQyOA==")
                .build();
        try {
            Response response = client.newCall(request).execute();
            stopwatch.stop();
            //耗时监控
            if (response.isSuccessful()) {
                return;
            } else {
            }

        } catch (Exception e) {
            stopwatch.stop();

        }
    }
}

