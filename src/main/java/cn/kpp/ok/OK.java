package cn.kpp.ok;


import cn.kpp.ok.core.ReqBody;
import cn.kpp.ok.core.Res;
import cn.kpp.ok.core.Timeout;
import cn.kpp.ok.core.TimeoutInterceptor;
import cn.kpp.ok.mate.Const;
import cn.kpp.ok.mate.ContentType;
import cn.kpp.ok.mate.Header;
import cn.kpp.ok.mate.Method;
import cn.kpp.ok.mate.ReqType;
import com.alibaba.fastjson2.JSON;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okhttp3.internal.http.HttpMethod;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

/**
 * 基于okhttp封装的http请求工具
 *
 * @author kongweiguang
 */
public final class OK {

    private static final OkHttpClient default_c = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(TimeoutInterceptor.of)
            .build();


    private final OkHttpClient C;
    private final Request.Builder builder;
    private String scheme = Const.http;
    private String host = Const.localhost;
    private int port;
    private String url;
    private String reqBody;
    private RequestBody formBody;
    private String contentType;
    private Charset charset = StandardCharsets.UTF_8;
    private Method method = Method.GET;

    private final Map<String, String> form = new HashMap<>();
    private final Map<String, String> query = new HashMap<>();
    private final Map<String, String> cookie = new HashMap<>();
    private final List<String> paths = new ArrayList<>();

    //async
    private boolean async = false;
    private Consumer<Res> success;
    private Consumer<IOException> fail;

    //ws
    private WebSocketListener listener;

    private ReqType typeEnum = ReqType.http;


    private OkHttpClient client() {
        return this.C;
    }

    private Request.Builder builder() {
        return this.builder;
    }

    private OK(OkHttpClient c) {
        this.C = c;
        this.builder = new Request.Builder();
    }

    public static OK of() {
        return new OK(default_c);
    }

    public static OK of(OkHttpClient c) {
        return new OK(c);
    }

    /**
     * 发送请求
     *
     * @return Res {@code  Res}
     */
    public Res ok() {
        bf();
        switch (typeEnum) {
            case http:
                return http0();
            case ws:
                ws0();
                break;
            case sse:
                sse();
                break;
        }
        return null;
    }

    private void bf() {
        addQuery();
        addForm();
        addCookie();
        switchM();
    }

    private void addQuery() {
        final HttpUrl.Builder urlBuilder = new HttpUrl.Builder();

        if (!this.query.isEmpty()) {
            this.query.forEach(urlBuilder::addEncodedQueryParameter);
        }

        if (nonNull(this.url)) {
            this.builder.url(this.url);
        } else {

            this.paths.forEach(urlBuilder::addPathSegments);

            urlBuilder.scheme(this.scheme);
            urlBuilder.host(this.host);
            urlBuilder.port(this.port);

            this.builder.url(urlBuilder.build());
        }
    }

    private void addCookie() {
        if (!this.cookie.isEmpty()) {
            this.builder.addHeader(Header.cookie.v(), cookie2Str(this.cookie));
        }
    }

    private static String cookie2Str(Map<String, String> cookies) {
        StringBuilder sb = new StringBuilder();

        cookies.forEach((k, v) -> sb.append(k).append('=').append(v).append("; "));

        return sb.toString();
    }


    private void addForm() {
        if (!this.form.isEmpty()) {
            final FormBody.Builder b = new FormBody.Builder();
            this.form.forEach(b::addEncoded);
            this.formBody = b.build();
        }
    }

    private void switchM() {
        RequestBody rb = null;

        if (HttpMethod.permitsRequestBody(this.method.name())) {
            if (nonNull(this.formBody)) {
                rb = this.formBody;
            } else {
                new ReqBody(this.contentType, reqBody.getBytes(this.charset));
            }
        }

        builder.method(method.name(), rb);
    }

    private Res http0() {
        if (async) {
            this.C.newCall(this.builder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(final Call call, final IOException e) {
                    if (nonNull(fail)) {
                        fail.accept(e);
                    }
                    call.cancel();
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    if (nonNull(success)) {
                        success.accept(Res.of(response));
                    }
                    call.cancel();
                }
            });

            return null;
        } else {
            try (Response execute = this.C.newCall(this.builder.build()).execute()) {
                return Res.of(execute);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public OK timeout(int timeoutSeconds) {
        if (timeoutSeconds > 0) {
            this.builder.tag(Timeout.class, new Timeout(timeoutSeconds));
        }

        return this;
    }

    public OK timeout(int connectTimeoutSeconds, int writeTimeoutSeconds, int readTimeoutSeconds) {
        if (connectTimeoutSeconds > 0) {
            this.builder.tag(Timeout.class, new Timeout(connectTimeoutSeconds, writeTimeoutSeconds, readTimeoutSeconds));
        }

        return this;
    }

    public OK userAgent(final String ua) {
        this.builder.header(Header.user_agent.v(), ua);
        return this;
    }

    public OK charset(final String charset) {
        this.charset = Charset.forName(charset);
        return this;
    }

    public OK headers(final Map<String, String> headers) {
        if (nonNull(headers)) {
            headers.forEach(this.builder::header);
        }

        return this;
    }

    public OK header(final String k, final String v) {
        if (nonNull(k) || nonNull(v)) {
            return this;
        }

        this.builder.header(k, v);
        return this;
    }

    public OK cookie(final String k, final String v) {
        if (nonNull(k) || nonNull(v)) {
            return this;
        }

        this.cookie.put(k, v);
        return this;

    }

    public OK cookie(final Map<String, String> cookies) {
        if (nonNull(cookies)) {
            this.cookie.putAll(cookies);
        }

        return this;
    }

    public OK query(final String k, final String v) {
        this.query.put(k, v);
        return this;
    }

    public OK query(final Map<String, String> querys) {
        if (nonNull(querys)) {
            this.query.putAll(querys);
        }

        return this;
    }

    public OK async() {
        this.async = true;
        return this;
    }

    public OK scheme(final String scheme) {
        this.scheme = scheme;
        return this;
    }

    public OK host(final String host) {
        this.host = host;
        return this;
    }

    public OK port(final int port) {
        this.port = port;
        return this;
    }

    public OK url(final String url) {
        this.url = url;
        return this;
    }

    public OK get() {
        this.method = Method.GET;
        return this;
    }

    public OK post() {
        this.method = Method.POST;
        return this;
    }

    public OK delete() {
        this.method = Method.DELETE;
        return this;
    }

    public OK put() {
        this.method = Method.PUT;
        return this;
    }

    public OK patch() {
        this.method = Method.PATCH;
        return this;
    }

    public OK method(final Method method) {
        this.method = method;
        return this;
    }

    public OK path(final String path) {
        this.paths.add(path);
        return this;
    }


    public OK form(final Map<String, String> form) {
        this.form.putAll(form);
        return this;
    }

    public OK raw(final String raw) {
        reqBody = raw;
        return this;
    }

    public OK json(final String json) {
        this.contentType = ContentType.json.v();
        reqBody = json;
        return this;
    }

    public OK json(final Map<String, Object> json) {
        this.contentType = ContentType.json.v();
        reqBody = JSON.toJSONString(json);
        return this;
    }


    public OK success(final Consumer<Res> success) {
        this.success = success;
        return this;
    }


    public OK fail(final Consumer<IOException> fail) {
        this.fail = fail;
        return this;
    }


    //--------------------------------------get

    public String scheme() {
        return scheme;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String url() {
        return url;
    }

    public String reqBody() {
        return reqBody;
    }

    public String contentType() {
        return contentType;
    }

    public Charset charset() {
        return charset;
    }

    public Method method() {
        return method;
    }

    public Map<String, String> form() {
        return form;
    }

    public Map<String, String> query() {
        return query;
    }

    public Map<String, String> cookie() {
        return cookie;
    }

    public List<String> paths() {
        return paths;
    }


    //ws
    public OK listener(WebSocketListener listener) {
        this.listener = listener;
        return this;
    }

    public OK ws() {
        this.typeEnum = ReqType.ws;
        return this;
    }

    private void ws0() {
        this.client().newWebSocket(this.builder.build(), this.listener);
    }

    //todo
    //sse
    public OK sse() {
        return this;
    }

}
