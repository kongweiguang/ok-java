package io.github.kongweiguang.ok;


import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.alibaba.fastjson2.JSON;
import io.github.kongweiguang.ok.core.Config;
import io.github.kongweiguang.ok.core.ContentType;
import io.github.kongweiguang.ok.core.Header;
import io.github.kongweiguang.ok.core.Method;
import io.github.kongweiguang.ok.core.MultiValueMap;
import io.github.kongweiguang.ok.core.ReqType;
import io.github.kongweiguang.ok.core.Timeout;
import io.github.kongweiguang.ok.core.Util;
import io.github.kongweiguang.ok.sse.SSEListener;
import io.github.kongweiguang.ok.ws.WSListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.WebSocketListener;
import okhttp3.internal.http.HttpMethod;

/**
 * 基于okhttp封装的http请求工具
 *
 * @author kongweiguang
 */
public final class Req {

  private ReqType reqType;
  private final Request.Builder builder;

  //header
  private Method method;
  private Timeout timeout;
  private Map<String, String> cookieMap;
  private String contentType;
  private Charset charset;

  //url
  private URL url;
  private String scheme;
  private String host;
  private int port = -1;
  private LinkedList<String> paths;
  private MultiValueMap<String, String> queryMap;

  //body
  private String strBody;

  //form
  private boolean formUrl;
  private boolean multipart;
  private Map<String, String> formMap;
  private MultipartBody.Builder mul;

  //async
  private Consumer<Res> success;
  private Consumer<Throwable> fail;

  //retry
  private int max;
  private Duration delay;
  private BiPredicate<Res, Throwable> predicate;

  //listener
  private WSListener wsListener;
  private SSEListener sseListener;

  //dami
  private Map<Object, Object> attachment;

  private Req() {
    this.charset = StandardCharsets.UTF_8;
    this.method = Method.GET;
    this.reqType = ReqType.http;
    this.builder = new Builder();
  }

  public Req reqType(final ReqType typeEnum) {
    this.reqType = typeEnum;
    return this;
  }

  //工厂方法
  public static Req of() {
    return new Req();
  }

  public static Req of(final String url) {
    return of().url(url);
  }

  public static Req get(final String url) {
    return of(url).method(Method.GET);
  }

  public static Req post(final String url) {
    return of(url).method(Method.POST);
  }

  public static Req delete(final String url) {
    return of(url).method(Method.DELETE);
  }

  public static Req put(final String url) {
    return of(url).method(Method.PUT);
  }

  public static Req patch(final String url) {
    return of(url).method(Method.PATCH);
  }

  public static Req head(final String url) {
    return of(url).method(Method.HEAD);
  }

  public static Req options(final String url) {
    return of(url).method(Method.OPTIONS);
  }

  public static Req trace(final String url) {
    return of(url).method(Method.TRACE);
  }

  public static Req connect(final String url) {
    return of(url).method(Method.CONNECT);
  }

  public static Req formUrlencoded(final String url) {
    return of(url).formUrlencoded();
  }

  public static Req multipart(final String url) {
    return of(url).multipart();
  }

  public Req formUrlencoded() {
    this.formUrl = true;
    return method(Method.POST)
        .contentType(ContentType.form_urlencoded);
  }

  public Req multipart() {
    this.multipart = true;
    return method(Method.POST)
        .contentType(ContentType.multipart);
  }

  //ws
  public static Req ws(final String url) {
    return of(url).reqType(ReqType.ws);
  }

  //sse
  public static Req sse(final String url) {
    return of(url).reqType(ReqType.sse);
  }

  //同步请求
  public Res ok() {
    return ok(Config.client());
  }

  public Res ok(final OkHttpClient client) {
    return OK.ok(this, client);
  }

  //异步请求
  public CompletableFuture<Res> okAsync() {
    return okAsync(Config.client());
  }

  public CompletableFuture<Res> okAsync(final OkHttpClient client) {
    return OK.okAsync(this, client);
  }

  void bf() {
    addMethod();
    addQuery();
    addCookie();

    builder().tag(Req.class, this);
  }


  private void addMethod() {
    builder().method(method().name(), addBody());
  }

  private RequestBody addBody() {
    RequestBody rb = null;

    if (HttpMethod.permitsRequestBody(method().name())) {
      //multipart 格式提交
      if (isMul()) {

        if (nonNull(formMap)) {
          form().forEach(mul()::addFormDataPart);
        }

        rb = mul().setType(MediaType.parse(contentType())).build();

      }
      //form_urlencoded 格式提交
      else if (isFormUrl()) {

        final FormBody.Builder b = new FormBody.Builder(charset());

        if (nonNull(formMap)) {
          form().forEach(b::addEncoded);
        }

        rb = b.build();

      }
      //字符串提交
      else {

        if (nonNull(strBody())) {
          rb = RequestBody.create(strBody(), MediaType.parse(contentType()));
        }

      }
    }

    return rb;
  }


  private void addQuery() {
    final HttpUrl.Builder ub = new HttpUrl.Builder();

    if (isNull(scheme())) {
      ub.scheme(url().getProtocol());
    }

    if (isNull(host())) {
      ub.host(url().getHost());
    }

    if (port() == -1) {
      final int p = url().getPort();
      if (p > 0) {
        ub.port(p);
      }
    }

    if (nonNull(url().getPath())) {
      ub.addPathSegments(Util.removeFirstSlash(url().getPath()));
    }

    Optional.ofNullable(url().getQuery())
        .map(e -> e.split("&"))
        .ifPresent(qr -> {
          for (String part : qr) {
            String[] kv = part.split("=");
            if (kv.length > 1) {
              ub.addEncodedQueryParameter(kv[0], kv[1]);
            }
          }
        });

    Optional.ofNullable(queryMap)
        .map(MultiValueMap::map)
        .ifPresent(map -> map.forEach((k, v) ->
            v.forEach(e -> ub.addEncodedQueryParameter(k, e))));

    if (nonNull(paths)) {
      paths().forEach(ub::addPathSegments);
    }

    builder().url(ub.build());
  }

  private void addCookie() {

    if (nonNull(cookieMap)) {
      builder().addHeader(Header.cookie.v(), cookie2Str(cookie()));
    }

  }

  private static String cookie2Str(Map<String, String> cookies) {
    StringBuilder sb = new StringBuilder();

    cookies.forEach((k, v) -> sb.append(k).append('=').append(v).append("; "));

    return sb.toString();
  }


  //timeout，单位秒
  public Req timeout(int timeoutSeconds) {
    if (timeoutSeconds > 0) {
      return timeout(timeoutSeconds, timeoutSeconds, timeoutSeconds);
    }

    return this;
  }

  public Req timeout(int connectTimeoutSeconds, int writeTimeoutSeconds, int readTimeoutSeconds) {
    if (connectTimeoutSeconds > 0) {
      this.timeout = new Timeout(connectTimeoutSeconds, writeTimeoutSeconds, readTimeoutSeconds);
      builder().tag(Timeout.class, timeout());
    }

    return this;
  }

  //method
  public Req method(final Method method) {
    this.method = method;
    return this;
  }

  //header
  public Req headers(final Map<String, String> headers) {
    if (nonNull(headers)) {
      headers.forEach(builder()::addHeader);
    }

    return this;
  }

  public Req header(final String name, final String value) {
    if (nonNull(name) && nonNull(value)) {
      builder.header(name, value);
    }

    return this;
  }

  public Req cookies(final Map<String, String> cookies) {
    if (nonNull(cookies)) {
      cookie().putAll(cookies);
    }

    return this;
  }

  public Req cookie(final String k, final String v) {
    if (nonNull(k) && nonNull(v)) {
      cookie().put(k, v);
    }

    return this;
  }

  public Req contentType(final ContentType contentType) {
    if (nonNull(contentType)) {
      this.contentType = contentType.v();

      header(Header.content_type.v(), String.join(";charset=", contentType(), charset().name()));
    }

    return this;
  }

  public Req charset(final Charset charset) {
    this.charset = charset;
    return this;
  }

  public Req ua(final String ua) {
    builder().header(Header.user_agent.v(), ua);
    return this;
  }

  public Req auth(final String auth) {
    builder().header(Header.authorization.v(), auth);
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
    if (nonNull(path)) {
      paths().add(Util.removeFirstSlash(path));
    }

    return this;
  }

  public Req query(final String k, final String v) {
    if (nonNull(k) && nonNull(v)) {
      query().put(k, v);
    }

    return this;
  }

  public Req query(final String k, final Iterable<String> vs) {
    if (nonNull(k) && nonNull(vs)) {
      for (String v : vs) {
        query().put(k, v);
      }
    }

    return this;
  }

  public Req query(final Map<String, String> querys) {
    if (nonNull(querys)) {
      querys.forEach(query()::put);
    }

    return this;
  }

  //form
  public Req file(String name, String fileName, byte[] bytes) {
    if (isMul()) {
      mul().addFormDataPart(
          name,
          fileName,
          RequestBody.create(bytes, MediaType.parse(contentType()))
      );
    }

    return this;
  }

  public Req form(final String name, final String value) {
    if (isFormUrl() || isMul()) {
      form().put(name, value);
    }

    return this;
  }

  public Req form(final Map<String, String> form) {
    if (isFormUrl() || isMul()) {
      form().putAll(form);
    }

    return this;
  }

  //body
  public Req json(final String json) {
    return body(json, ContentType.json);
  }

  public Req json(final Object json) {
    return body(JSON.toJSONString(json), ContentType.json);
  }

  public Req body(final String str, final ContentType contentType) {
    contentType(contentType);
    this.strBody = str;
    return this;
  }

  //async
  public Req success(final Consumer<Res> success) {
    this.success = success;
    return this;
  }

  public Req fail(final Consumer<Throwable> fail) {
    this.fail = fail;
    return this;
  }

  //retry
  public Req retry(final int max) {
    return retry(max, Duration.ofSeconds(1),
        (r, e) -> {
          if (nonNull(e)) {
            return true;
          }

          if (nonNull(r)) {
            return !r.isOk();
          }

          return true;
        });
  }

  public Req retry(
      final int max,
      final Duration delay,
      final BiPredicate<Res, Throwable> predicate) {
    this.max = max;
    this.delay = delay;
    this.predicate = predicate;
    return this;
  }

  //listener
  public Req wsListener(final WSListener listener) {
    this.wsListener = listener;
    return this;
  }

  public Req sseListener(final SSEListener sseListener) {
    this.sseListener = sseListener;
    return this;
  }


  //attachment
  public Map<Object, Object> attachment() {
    if (isNull(attachment)) {
      this.attachment = new HashMap<>();
    }
    return attachment;
  }

  public Req setAttachment(final Object k, final Object v) {
    attachment().put(k, v);
    return this;
  }

  //get
  public Builder builder() {
    return builder;
  }

  public ReqType reqType() {
    return reqType;
  }

  public Method method() {
    return method;
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

  public LinkedList<String> paths() {
    if (isNull(paths)) {
      paths = new LinkedList<>();
    }

    return paths;
  }

  public URL url() {
    return url;
  }

  public String strBody() {
    return strBody;
  }

  public String contentType() {
    return contentType;
  }

  public Charset charset() {
    return charset;
  }

  public MultiValueMap<String, String> query() {
    if (isNull(queryMap)) {
      queryMap = new MultiValueMap<>();
    }

    return queryMap;
  }

  public Map<String, String> form() {
    if (isNull(formMap)) {
      formMap = new HashMap<>();
    }

    return formMap;
  }

  public Map<String, String> cookie() {
    if (isNull(cookieMap)) {
      cookieMap = new HashMap<>();
    }

    return cookieMap;
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

  public Consumer<Res> success() {
    return success;
  }

  public Consumer<Throwable> fail() {
    return fail;
  }

  public BiPredicate<Res, Throwable> predicate() {
    return predicate;
  }

  public Timeout timeout() {
    return timeout;
  }

  public WebSocketListener wsListener() {
    return wsListener;
  }

  public SSEListener sseListener() {
    return sseListener;
  }

  public boolean isMul() {
    return multipart;
  }

  public boolean isFormUrl() {
    return formUrl;
  }

  @Override
  public String toString() {
    return "print res";
  }
}
