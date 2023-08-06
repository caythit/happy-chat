package com.happy.chat.uitls;

import static com.happy.chat.uitls.PrometheusUtils.perf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Lazy
@Component
public class OkHttpUtils {

    private final String prometheusName = "http";
    private final String prometheusHelp = "http_req";

    @Autowired
    private CollectorRegistry httpRegistry;

    private static final OkHttpClient DEFAULT_CLIENT = new OkHttpClient().newBuilder()
            .build();


    public Response postJson(String url, String json) {
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
            Response response = DEFAULT_CLIENT.newCall(request).execute();
            //耗时监控
            if (response.isSuccessful()) {
                perf(httpRegistry, prometheusName, prometheusHelp, "http_reqeust_succss");
            } else {
                perf(httpRegistry, prometheusName, prometheusHelp, "http_reqeust_failed");
            }
            return response;

        } catch (Exception e) {
            log.error("okhttp exception", e);
            perf(httpRegistry, prometheusName, prometheusHelp, "http_reqeust_exception");
            return null;
        }
    }
}

