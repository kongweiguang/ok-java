package io.github.kongweiguang.ok.core;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public interface BaseHttp<R extends BaseHttp<R>> {
    OkHttpClient default_c = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(TimeoutInterceptor.of)
            .build();

}
