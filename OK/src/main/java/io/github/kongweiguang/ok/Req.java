package io.github.kongweiguang.ok;


import com.alibaba.fastjson2.JSON;
import io.github.kongweiguang.ok.core.BaseHttp;
import io.github.kongweiguang.ok.core.ContentType;
import io.github.kongweiguang.ok.core.Header;
import io.github.kongweiguang.ok.core.Method;
import io.github.kongweiguang.ok.core.MultiValueMap;
import io.github.kongweiguang.ok.core.ReqBody;
import io.github.kongweiguang.ok.core.ReqType;
import io.github.kongweiguang.ok.core.Res;
import io.github.kongweiguang.ok.core.Timeout;
import io.github.kongweiguang.ok.core.Util;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.WebSocketListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * 基于okhttp封装的http请求工具
 *
 * @author kongweiguang
 */
public class Req implements BaseHttp<Req> {


    //header
    private Method method;
    private Map<String, String> cookie;
    private Timeout timeout;
    private Map<String, String> headers;

    //url
    private URL url;
    private String scheme;
    private String host;
    private int port;
    private LinkedList<String> paths;
    private MultiValueMap<String, String> query;

    //body
    private String reqBody;
    private RequestBody formBody;
    private String contentType;
    private Charset charset;

    //form
    private boolean multipart;
    private Map<String, String> form;
    private MultipartBody.Builder mul;

    //async
    private boolean async;
    private Consumer<Res> success;
    private Consumer<IOException> fail;

    //retry
    private boolean retry;
    private int max;
    private Duration delay;
    private BiPredicate<Res, Throwable> predicate;

    //ws
    private WebSocketListener listener;

    private ReqType typeEnum;


    private Req() {
        this.headers = new HashMap<>();
        this.charset = StandardCharsets.UTF_8;
        this.method = Method.GET;
        this.typeEnum = ReqType.http;
    }

    public static Req of() {
        return new Req();
    }

    public Res ok() {
        return OK.of().ok(this);
    }


    //timeout
    public Req timeout(int timeoutSeconds) {
        if (timeoutSeconds > 0) {
            timeout = new Timeout(timeoutSeconds);
        }

        return this;
    }


    public Req timeout(int connectTimeoutSeconds, int writeTimeoutSeconds, int readTimeoutSeconds) {
        if (connectTimeoutSeconds > 0) {
            timeout = new Timeout(connectTimeoutSeconds, writeTimeoutSeconds, readTimeoutSeconds);
        }
        return this;
    }


    //header
    public Req headers(final Map<String, String> headers) {
        if (nonNull(headers)) {
            headers().putAll(headers);
        }

        return this;
    }

    public Req header(final String name, final String value) {
        if (isNull(name) || isNull(value)) {
            return this;
        }

        headers.put(name, value);
        return this;
    }

    public Req cookie(final String k, final String v) {
        if (isNull(k) || isNull(v)) {
            return this;
        }

        cookie().put(k, v);
        return this;

    }

    public Req cookie(final Map<String, String> cookies) {
        if (nonNull(cookies)) {
            cookie().putAll(cookies);
        }

        return this;
    }

    public Req contentType(final ContentType contentType) {
        if (isNull(contentType)) {
            return this;
        }

        this.contentType = contentType.v();
        return this;
    }


    public Req charset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    public Req ua(final String ua) {
        headers().put(Header.user_agent.v(), ua);
        return this;
    }


    public Req auth(final String auth) {
        headers().put(Header.authorization.v(), auth);
        return this;
    }

    public Req bearer(final String token) {
        return auth("Bearer " + token);
    }

    //url
    public Req url(final String url) {
        try {
            this.url = new URL(Util.urlRegex(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Req query(final String k, final String v) {
        query().put(k, v);
        return this;
    }


    public Req query(final Map<String, String> querys) {
        if (nonNull(querys)) {
            querys.forEach(query()::put);
        }

        return this;
    }

    public Req scheme(final String scheme) {
        this.scheme = scheme;
        return this;
    }

    public Req host(final String host) {
        this.host = host;
        return this;
    }

    public Req port(final int port) {
        this.port = port;
        return this;
    }


    public Req path(final String path) {
        if (isNull(path)) {
            return this;
        }

        paths().add(Util.replacePath(path));
        return this;
    }


    public Req pathFirst(final String path) {
        if (isNull(path)) {
            return this;
        }

        paths().addFirst(Util.replacePath(path));
        return this;
    }

    //method
    public Req method(final Method method) {
        this.method = method;
        return this;
    }

    public Req get() {
        this.method = Method.GET;
        return this;
    }

    public Req post() {
        this.method = Method.POST;
        return this;
    }

    public Req delete() {
        this.method = Method.DELETE;
        return this;
    }

    public Req put() {
        this.method = Method.PUT;
        return this;
    }

    public Req patch() {
        this.method = Method.PATCH;
        return this;
    }

    public Req head() {
        this.method = Method.HEAD;
        return this;
    }

    public Req options() {
        this.method = Method.OPTIONS;
        return this;
    }

    public Req trace() {
        this.method = Method.TRACE;
        return this;
    }

    public Req connect() {
        this.method = Method.CONNECT;
        return this;
    }


    //form
    public Req multipart() {
        this.method = Method.POST;
        this.multipart = true;
        contentType(ContentType.multipart);
        return this;
    }


    public Req file(String key, String fileName, byte[] bytes) {
        contentType(ContentType.multipart);
        mul().addFormDataPart(key, fileName, new ReqBody(contentType(), charset(), bytes));
        return this;
    }

    public Req form(final String name, final String value) {
        form().put(name, value);
        return this;
    }

    public Req form(final Map<String, String> form) {
        form().putAll(form);
        return this;
    }

    //body
    public Req body(final String json) {
        return body(json, ContentType.json);
    }

    public Req body(final Object json) {
        return body(JSON.toJSONString(json), ContentType.json);
    }

    public Req body(final String obj, final ContentType contentType) {
        contentType(contentType);
        this.reqBody = obj;
        return this;
    }

    //async
    public Req async() {
        this.async = true;
        return this;
    }

    public Req success(final Consumer<Res> success) {
        this.success = success;
        return this;
    }


    public Req fail(final Consumer<IOException> fail) {
        this.fail = fail;
        return this;
    }


    //retry
    public Req retry(int max) {
        return retry(max, Duration.ofSeconds(1), (r, e) -> true);
    }

    public Req retry(int max, Duration delay, BiPredicate<Res, Throwable> predicate) {
        this.retry = true;
        this.max = max;
        this.delay = delay;
        this.predicate = predicate;
        return this;
    }


    //ws
    public Req listener(WebSocketListener listener) {
        this.listener = listener;
        return this;
    }


    public Req ws() {
        this.typeEnum = ReqType.ws;
        return this;
    }


    //get

    public String scheme() {
        return scheme;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public URL url() {
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
        if (isNull(form)) {
            form = new HashMap<>();
        }

        return form;
    }

    public MultiValueMap<String, String> query() {
        if (isNull(query)) {
            query = new MultiValueMap<>();
        }

        return query;
    }

    public Map<String, String> cookie() {
        if (isNull(cookie)) {
            cookie = new HashMap<>();
        }

        return cookie;
    }

    public LinkedList<String> paths() {
        if (isNull(paths)) {
            paths = new LinkedList<>();
        }

        return paths;
    }


    public MultipartBody.Builder mul() {
        if (isNull(mul)) {
            mul = new MultipartBody.Builder();
        }
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

    public Timeout timeout() {
        return timeout;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public boolean isAsync() {
        return async;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public boolean isRetry() {
        return retry;
    }
}
