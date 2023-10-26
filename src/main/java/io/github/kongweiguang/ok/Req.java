package io.github.kongweiguang.ok;


import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.alibaba.fastjson2.JSON;
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
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.WebSocketListener;

/**
 * 基于okhttp封装的http请求工具
 *
 * @author kongweiguang
 */
public final class Req {

  private ReqType typeEnum;

  //header
  private Method method;
  private Timeout timeout;
  private final Map<String, String> headers;
  private Map<String, String> cookie;
  private String contentType;
  private Charset charset;

  //url
  private URL url;
  private String scheme;
  private String host;
  private int port;
  private LinkedList<String> paths;
  private MultiValueMap<String, String> query;

  //body
  private String strBody;

  //form
  private boolean formUrlencoded;
  private boolean multipart;
  private Map<String, String> form;
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


  private Req() {
    this.headers = new HashMap<>();
    this.charset = StandardCharsets.UTF_8;
    this.method = Method.GET;
    this.typeEnum = ReqType.http;
  }

  public Req reqType(final ReqType typeEnum) {
    this.typeEnum = typeEnum;
    return this;
  }

  //工厂方法
  public static Req of() {
    return new Req();
  }

  public static Req get() {
    return of().method(Method.GET);
  }

  public static Req post() {
    return of().method(Method.POST);
  }

  public static Req delete() {
    return of().method(Method.DELETE);
  }

  public static Req put() {
    return of().method(Method.PUT);
  }

  public static Req patch() {
    return of().method(Method.PATCH);
  }

  public static Req head() {
    return of().method(Method.HEAD);
  }

  public static Req options() {
    return of().method(Method.OPTIONS);
  }

  public static Req trace() {
    return of().method(Method.TRACE);
  }

  public static Req connect() {
    return of().method(Method.CONNECT);
  }

  public static Req formUrlencoded() {
    return of().method(Method.POST)
        .contentType(ContentType.form_urlencoded)
        .formUrlencoded(true);
  }

  public static Req multipart() {
    return of().method(Method.POST)
        .contentType(ContentType.multipart)
        .multipart(true);
  }

  private Req formUrlencoded(final boolean mul) {
    this.formUrlencoded = mul;
    return this;
  }

  private Req multipart(final boolean mul) {
    this.multipart = mul;
    return this;
  }

  //ws
  public static Req ws() {
    return of().reqType(ReqType.ws);
  }

  //sse
  public static Req sse() {
    return of().reqType(ReqType.sse);
  }

  //同步请求
  public Res ok() {
    return OK.ok(this);
  }

  //异步请求
  public CompletableFuture<Res> okAsync() {
    return OK.okAsync(this);
  }

  //timeout，单位秒
  public Req timeout(int timeoutSeconds) {
    if (timeoutSeconds > 0) {
      this.timeout = new Timeout(timeoutSeconds);
    }

    return this;
  }

  public Req timeout(int connectTimeoutSeconds, int writeTimeoutSeconds, int readTimeoutSeconds) {
    if (connectTimeoutSeconds > 0) {
      this.timeout = new Timeout(connectTimeoutSeconds, writeTimeoutSeconds, readTimeoutSeconds);
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
      headers().putAll(headers);
    }

    return this;
  }

  public Req header(final String name, final String value) {
    if (nonNull(name) && nonNull(value)) {
      headers().put(name, value);
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
    if (isNull(contentType)) {
      return this;
    }

    this.contentType = contentType.v();

    header(Header.content_type.v(), String.join(";charset=", contentType(), charset().name()));
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
      paths().add(Util.replacePath(path));
    }

    return this;
  }

  public Req pathFirst(final String path) {
    if (nonNull(path)) {
      paths().addFirst(Util.replacePath(path));
    }

    return this;
  }

  public Req query(final String k, final String v) {
    query().put(k, v);
    return this;
  }

  public Req query(final String k, final List<String> vs) {
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
    mul().addFormDataPart(
        name,
        fileName,
        RequestBody.create(bytes, MediaType.parse(contentType()))
    );
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
    this.strBody = obj;
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
    return retry(
        max,
        Duration.ofSeconds(1),
        (r, e) -> {
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

  public String strBody() {
    return strBody;
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

  public ReqType reqType() {
    return typeEnum;
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

  public Map<String, String> headers() {
    return headers;
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

  public boolean isForm() {
    return formUrlencoded;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Req.class.getSimpleName() + "[", "]").add("method=" + method)
        .add("url=" + url)
        .add("strBody='" + strBody + "'")
        .toString();
  }
}
