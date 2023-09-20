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
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author kongweiguang
 */
public final class OK {

    private static final OkHttpClient defaultC = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(10000, 15, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.MINUTES)
            .writeTimeout(15, TimeUnit.MINUTES)
            .readTimeout(15, TimeUnit.MINUTES)
            .build();


    private OkHttpClient C;
    private Request.Builder _builder;
    private RequestBody requestBody;
    private String contentType;
    private Charset _charset;

    private Map<String, Object> param;

    private boolean async = false;

    private Consumer<ResponseBody> success;
    private Consumer<IOException> fail;


    private OK(OkHttpClient c) {
        if (Objects.isNull(c)) {
            this.C = defaultC;
        } else {
            this.C = c;
        }
        _builder = new Request.Builder();
    }

    public static OK of() {
        return new OK(null);
    }

    /**
     * 发送请求
     *
     * @return r
     */
    public Res ok() {
        if (async) {
            this.C.newCall(_builder.build()).enqueue(new Callback() {
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
            try (Response execute = this.C.newCall(_builder.build()).execute()) {
                return Res.of(execute);
            } catch (Exception e) {
                return null;
            }
        }

    }

    public OK userAgent(String ua) {
        _builder.header("User-Agent", ua);
        return this;
    }

    public OK charset(String charset) {
        _charset = Charset.forName(charset);
        return this;
    }

    public OK headers(Map<String, String> headers) {

        if (headers != null) {
            headers.forEach((k, v) -> _builder.header(k, v));
        }

        return this;
    }


    public OK header(String k, String v) {
        if (k == null || v == null) {
            return this;
        }

        _builder.header(k, v);
        return this;
    }

    public OK addQuery(String k, String v) {
        param.put(k, v);
        return this;
    }

    public OK addQuery(Map<String, Object> querys) {
        if (querys != null) {
            param.putAll(querys);
        }
        return this;
    }

    public OK async() {
        async = true;
        return this;
    }

    public OK url(String url) {
        _builder.url(url);
        return this;
    }

    public OK get() {
        _builder.get();
        return this;
    }

    public OK post() {
//        _builder.post(new StreamBody("application/json",));
        return this;
    }

    public OK body(String json) {
        requestBody = RequestBody.create(json, null);
        return this;
    }

    public OK jsonBody(String json) {
        requestBody = RequestBody.create(json, MediaType.parse("application/json"));
        _builder.post(requestBody);
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


    public static class StreamBody extends RequestBody {
        private MediaType _contentType = null;
        private InputStream _inputStream = null;

        public StreamBody(String contentType, InputStream inputStream) {
            if (contentType != null) {
                _contentType = MediaType.parse(contentType);
            }

            _inputStream = inputStream;
        }

        @Override
        public MediaType contentType() {
            return _contentType;
        }

        @Override
        public long contentLength() throws IOException {
            return _inputStream.available();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Source source = null;

            try {
                source = Okio.source(_inputStream);
                sink.writeAll(source);
            } finally {
                Util.closeQuietly(source);
            }
        }
    }


}
