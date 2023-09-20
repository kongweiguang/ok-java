package cn.kpp.core;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author kongweiguang
 */
public final class OK {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(10000, 15, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.MINUTES)
            .writeTimeout(15, TimeUnit.MINUTES)
            .readTimeout(15, TimeUnit.MINUTES)
            .build();


    private static Request.Builder _builder;
    private static RequestBody requestBody;
    private static String contentType;
    private boolean async = false;

    private Consumer<ResponseBody> success;
    private Consumer<IOException> fail;


    private OK() {
        _builder = new Request.Builder();

    }

    public static OK of() {
        return new OK();
    }

    /**
     * 发送请求
     *
     * @return r
     */
    public String send() throws Exception {
        if (async) {
            CLIENT.newCall(_builder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                    fail.accept(e);
                }

                @Override
                public void onResponse(@NotNull final Call call, @NotNull final Response response) throws IOException {
                    final ResponseBody body = response.body();
                    success.accept(body);
                }
            });
            return null;
        } else {
            try (Response execute = CLIENT.newCall(_builder.build()).execute()) {
                return execute.body().string();
            } catch (Exception e) {
                throw new Exception(e);
            }
        }

    }


    public OK async() {
        async = true;
        return this;
    }

    /**
     * 构建请求
     *
     * @return 构建器
     */
    public OK url(String url) {
        _builder.url(url);
        return this;
    }

    public OK get() {
        _builder.get();
        return this;
    }

    public OK post() {
        _builder.post(new Body());
        return this;
    }

    public OK body(String json) {
        requestBody = RequestBody.create(json, MediaType.parse("application/json"));
        return this;
    }

    public OK jsonBody(String json) {
        requestBody = RequestBody.create(json, MediaType.parse("application/json"));
        return this;
    }


    public OK success(Consumer<ResponseBody> success) {
        this.success = success;
        return this;
    }


    public OK fail(Consumer<IOException> fail) {
        this.fail = fail;
        return this;
    }


    private static final class Body extends RequestBody {
        @Override
        public MediaType contentType() {
            return MediaType.parse("");
        }

        @Override
        public void writeTo(final BufferedSink bs) throws IOException {
        }
    }

}
