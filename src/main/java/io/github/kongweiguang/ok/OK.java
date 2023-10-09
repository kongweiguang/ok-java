package io.github.kongweiguang.ok;


import com.alibaba.fastjson2.JSON;
import io.github.kongweiguang.ok.core.Const;
import io.github.kongweiguang.ok.core.ContentType;
import io.github.kongweiguang.ok.core.Header;
import io.github.kongweiguang.ok.core.Method;
import io.github.kongweiguang.ok.core.MultiValueMap;
import io.github.kongweiguang.ok.core.ReqBody;
import io.github.kongweiguang.ok.core.ReqType;
import io.github.kongweiguang.ok.core.Res;
import io.github.kongweiguang.ok.core.Retry;
import io.github.kongweiguang.ok.core.Timeout;
import io.github.kongweiguang.ok.core.TimeoutInterceptor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okhttp3.internal.http.HttpMethod;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static java.util.Objects.isNull;
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
    private final MultiValueMap<String, String> query = new MultiValueMap<>();
    private final Map<String, String> cookie = new HashMap<>();
    private final List<String> paths = new ArrayList<>();

    private boolean multipart = false;
    private final MultipartBody.Builder mul = new MultipartBody.Builder();

    //async
    private boolean async = false;
    private Consumer<Res> success;
    private Consumer<IOException> fail;

    //retry
    private boolean retry = false;
    private int max;
    private Duration delay = Duration.ofSeconds(1);
    private BiPredicate<Res, Throwable> predicate;

    //ws
    private WebSocketListener listener;

    private ReqType typeEnum = ReqType.http;


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
        if (req()) {
            if (retry()) {
                return Retry.predicate(this::http0, this.predicate)
                        .maxAttempts(max())
                        .delay(delay())
                        .execute()
                        .get()
                        .orElse(null);
            } else {
                return http0();
            }
        }
        return null;
    }

    private boolean req() {
        switch (typeEnum()) {
            case http:
                return true;
            case ws:
                ws0();
                break;
            case sse:
                break;
        }
        return false;
    }

    private void bf() {
        addQuery();
        addForm();
        addCookie();
        switchM();
    }

    private void addQuery() {
        final HttpUrl.Builder urlBuilder = new HttpUrl.Builder();

        if (!query().map().isEmpty()) {
            query().map().forEach((k, v) ->
                    v.forEach(e -> urlBuilder.addEncodedQueryParameter(k, e))
            );
        }

        if (nonNull(url())) {
            builder().url(url());
        } else {
            paths().forEach(urlBuilder::addPathSegments);

            urlBuilder.scheme(scheme());
            urlBuilder.host(host());
            urlBuilder.port(port());

            builder().url(urlBuilder.build());
        }
    }

    private void addCookie() {
        if (!cookie().isEmpty()) {
            builder().addHeader(Header.cookie.v(), cookie2Str(cookie()));
        }
    }

    private static String cookie2Str(Map<String, String> cookies) {
        StringBuilder sb = new StringBuilder();

        cookies.forEach((k, v) -> sb.append(k).append('=').append(v).append("; "));

        return sb.toString();
    }


    private void addForm() {
        if (this.multipart) {
            form().forEach(mul()::addFormDataPart);
        } else if (!form().isEmpty()) {
            final FormBody.Builder b = new FormBody.Builder(charset());
            form().forEach(b::addEncoded);
            this.formBody = b.build();
        }
    }

    private void switchM() {
        builder().method(method().name(), addBody());
    }

    private RequestBody addBody() {
        RequestBody rb = null;

        if (HttpMethod.permitsRequestBody(method().name())) {
            if (this.multipart) {
                rb = mul().setType(MediaType.parse(contentType() + ";charset=" + charset().name())).build();
            } else if (nonNull(formBody())) {
                rb = formBody();
            } else {
                rb = new ReqBody(contentType(), charset(), reqBody().getBytes(charset()));
            }
        }
        return rb;
    }

    private Res http0() {
        if (this.async) {
            client().newCall(builder().build()).enqueue(new Callback() {
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
            try (Response execute = client().newCall(builder().build()).execute()) {
                return Res.of(execute);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public OK timeout(int timeoutSeconds) {
        if (timeoutSeconds > 0) {
            builder().tag(Timeout.class, new Timeout(timeoutSeconds));
        }

        return this;
    }

    public OK timeout(int connectTimeoutSeconds, int writeTimeoutSeconds, int readTimeoutSeconds) {
        if (connectTimeoutSeconds > 0) {
            builder().tag(Timeout.class, new Timeout(connectTimeoutSeconds, writeTimeoutSeconds, readTimeoutSeconds));
        }

        return this;
    }

    public OK userAgent(final String ua) {
        builder().header(Header.user_agent.v(), ua);
        return this;
    }

    public OK charset(final String charset) {
        this.charset = Charset.forName(charset);
        return this;
    }

    public OK charset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    public OK headers(final Map<String, String> headers) {
        if (nonNull(headers)) {
            headers.forEach(builder()::header);
        }

        return this;
    }

    public OK header(final String name, final String value) {
        if (isNull(name) || isNull(value)) {
            return this;
        }

        builder().header(name, value);
        return this;
    }

    public OK cookie(final String k, final String v) {
        if (nonNull(k) || nonNull(v)) {
            return this;
        }

        cookie().put(k, v);
        return this;

    }

    public OK cookie(final Map<String, String> cookies) {
        if (nonNull(cookies)) {
            cookie().putAll(cookies);
        }

        return this;
    }

    public OK query(final String k, final String v) {
        query().put(k, v);
        return this;
    }

    public OK query(final Map<String, String> querys) {
        if (nonNull(querys)) {
            querys.forEach(query()::put);
        }

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

    public OK head() {
        this.method = Method.HEAD;
        return this;
    }

    public OK options() {
        this.method = Method.OPTIONS;
        return this;
    }

    public OK trace() {
        this.method = Method.TRACE;
        return this;
    }

    public OK connect() {
        this.method = Method.CONNECT;
        return this;
    }


    public OK method(final Method method) {
        this.method = method;
        return this;
    }

    public OK path(final String path) {
        paths().add(path);
        return this;
    }

    public OK multipart() {
        this.method = Method.POST;
        this.multipart = true;
        this.contentType = ContentType.multipart.v();
        return this;
    }

    public OK file(String key, String fileName, byte[] bytes) {
        mul().addFormDataPart(key, fileName, new ReqBody(contentType(), charset(), bytes));
        return this;
    }

    public OK form(final String name, final String value) {
        form().put(name, value);
        return this;
    }

    public OK form(final Map<String, String> form) {
        form().putAll(form);
        return this;
    }

    public OK raw(final String raw) {
        this.reqBody = raw;
        return this;
    }

    public OK json(final String json) {
        this.contentType = ContentType.json.v();
        this.reqBody = json;
        return this;
    }


    public OK json(final Object json) {
        this.contentType = ContentType.json.v();
        this.reqBody = JSON.toJSONString(json);
        return this;
    }


    //async
    public OK async() {
        this.async = true;
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


    //retry
    public OK retry(int max) {
        this.retry = true;
        this.max = max;
        this.predicate = (r, e) -> true;
        return this;
    }

    public OK retry(int max, Duration delay, BiPredicate<Res, Throwable> predicate) {
        this.retry = true;
        this.max = max;
        this.delay = delay;
        this.predicate = predicate;
        return this;
    }


    //get

    private OkHttpClient client() {
        return C;
    }

    private Request.Builder builder() {
        return builder;
    }

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

    public MultiValueMap<String, String> query() {
        return query;
    }

    public Map<String, String> cookie() {
        return cookie;
    }

    public List<String> paths() {
        return paths;
    }

    public boolean retry() {
        return retry;
    }

    public MultipartBody.Builder mul() {
        return mul;
    }

    public int max() {
        return max;
    }

    public Duration delay() {
        return delay;
    }

    public RequestBody formBody() {
        return formBody;
    }

    public ReqType typeEnum() {
        return typeEnum;
    }

    public Consumer<Res> success() {
        return success;
    }

    public Consumer<IOException> fail() {
        return fail;
    }

    public BiPredicate<Res, Throwable> predicate() {
        return predicate;
    }

    public WebSocketListener listener() {
        return listener;
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
        client().newWebSocket(builder().build(), listener());
    }

}
