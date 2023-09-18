package com.happy.chat.uitls;

import static com.happy.chat.constants.Constant.PERF_HTTP_MODULE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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

    @Autowired
    private PrometheusUtils prometheusUtil;

    private static final OkHttpClient DEFAULT_CLIENT = new OkHttpClient().newBuilder()
            .build();


    public Response postJson(String url, String json) throws Exception {
        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .build();

        Response response = DEFAULT_CLIENT.newCall(request).execute();
        //耗时监控
        if (response.isSuccessful()) {
            prometheusUtil.perf(PERF_HTTP_MODULE, "success");
        } else {
            prometheusUtil.perf(PERF_HTTP_MODULE, "failed_" + response.code());
        }
        return response;

    }
}

