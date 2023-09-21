package cn.kpp.ok;


import cn.kpp.ok.core.ReqBody;
import cn.kpp.ok.core.Res;
import cn.kpp.ok.mate.Const;
import cn.kpp.ok.mate.ContentType;
import cn.kpp.ok.mate.HeaderName;
import cn.kpp.ok.mate.Method;
import com.alibaba.fastjson2.JSON;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author kongweiguang
 */
public final class OK {

    private static final OkHttpClient default_c = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
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

    private boolean async = false;
    private Consumer<Res> success;
    private Consumer<IOException> fail;


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
        addQuery();
        addForm();
        addCookie();
        switchM();
        return res();
    }

    private void addQuery() {
        final HttpUrl.Builder urlBuilder = new HttpUrl.Builder();

        if (!this.query.isEmpty()) {
            this.query.forEach(urlBuilder::addEncodedQueryParameter);
        }

        if (Objects.nonNull(this.url)) {
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
            this.builder.addHeader(HeaderName.cookie.v(), cookie2Str(this.cookie));
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

    private Res res() {
        if (async) {
            this.C.newCall(this.builder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                    if (Objects.nonNull(fail)) {
                        fail.accept(e);
                    }
                    call.cancel();
                }

                @Override
                public void onResponse(@NotNull final Call call, @NotNull final Response response) throws IOException {
                    if (Objects.nonNull(success)) {
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

    private void switchM() {
        builder.method(
                method.name(),
                HttpMethod.permitsRequestBody(this.method.name()) ?
                        (Objects.nonNull(this.formBody) ? this.formBody : new ReqBody(this.contentType, reqBody.getBytes(this.charset))) :
                        null
        );

    }

    public OK userAgent(final String ua) {
        this.builder.header(HeaderName.user_agent.v(), ua);
        return this;
    }

    public OK charset(final String charset) {
        this.charset = Charset.forName(charset);
        return this;
    }

    public OK headers(final Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(this.builder::header);
        }

        return this;
    }

    public OK header(final String k, final String v) {
        if (k == null || v == null) {
            return this;
        }

        this.builder.header(k, v);
        return this;
    }

    public OK cookie(final String k, final String v) {
        if (k == null || v == null) {
            return this;
        }

        this.cookie.put(k, v);
        return this;

    }

    public OK cookie(final Map<String, String> cookies) {
        if (cookies != null) {
            this.cookie.putAll(cookies);
        }

        return this;
    }

    public OK query(final String k, final String v) {
        this.query.put(k, v);
        return this;
    }

    public OK query(final Map<String, String> querys) {
        if (querys != null) {
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

    public OK jsonBody(final String json) {
        this.contentType = ContentType.json.v();
        reqBody = json;
        return this;
    }

    public OK jsonBody(final Map<String, Object> json) {
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
}
