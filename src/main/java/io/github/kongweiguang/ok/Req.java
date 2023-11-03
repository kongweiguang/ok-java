package io.github.kongweiguang.ok;


import static io.github.kongweiguang.ok.core.Util.notNull;
import static io.github.kongweiguang.ok.core.Util.removeFirstSlash;
import static io.github.kongweiguang.ok.core.Util.urlRegex;
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

  //attachment
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
    return of().reqType(ReqType.ws).url(url);
  }

  //sse
  public static Req sse(final String url) {
    return of().reqType(ReqType.sse).url(url);
  }

  /**
   * 同步请求
   *
   * @return Res {@link Res}
   */
  public Res ok() {
    return ok(Config.client(timeout));
  }

  /**
   * 同步请求，自定义client
   *
   * @param client {@link OkHttpClient}
   * @return Res {@link Res}
   */
  public Res ok(final OkHttpClient client) {
    return OK.ok(this, client);
  }

  /**
   * 异步请求
   *
   * @return Res {@link Res}
   */
  public CompletableFuture<Res> okAsync() {
    return okAsync(Config.client(timeout));
  }

  /**
   * 异步请求，自定义client
   *
   * @param client {@link OkHttpClient}
   * @return Res {@link Res}
   */
  public CompletableFuture<Res> okAsync(final OkHttpClient client) {
    return OK.okAsync(this, client);
  }

  /**
   * 请求前初始化
   */
  void bf() {
    addMethod();
    addQuery();
    addCookie();

    builder().tag(Req.class, this);
  }

  /**
   * 添加http method
   */
  private void addMethod() {
    builder().method(method().name(), addBody());
  }

  /**
   * 添加body
   *
   * @return RequestBody {@link RequestBody}
   */
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

  /**
   * 添加请求的url和查询参数
   */
  private void addQuery() {
    final HttpUrl.Builder ub = new HttpUrl.Builder();

    if (nonNull(scheme())) {
      ub.scheme(scheme());
    } else {
      ub.scheme(url().getProtocol());
    }

    if (nonNull(host())) {
      ub.host(host());
    } else {
      ub.host(url().getHost());
    }

    if (port() == -1) {
      final int p = url().getPort();
      if (p > 0) {
        ub.port(p);
      }
    } else {
      ub.port(port());
    }

    if (nonNull(url().getPath())) {
      ub.addPathSegments(removeFirstSlash(url().getPath()));
    }

    if (nonNull(paths)) {
      paths().forEach(ub::addPathSegments);
    }

    final String query = url().getQuery();
    if (nonNull(query)) {

      for (final String part : query.split("&")) {
        String[] kv = part.split("=");

        if (kv.length > 1) {
          ub.addEncodedQueryParameter(kv[0], kv[1]);
        }
      }

    }

    if (nonNull(queryMap)) {
      query().map()
          .forEach((k, v) ->
              v.forEach(e -> ub.addEncodedQueryParameter(k, e))
          );
    }

    builder().url(ub.build());
  }

  /**
   * 添加cookie
   */
  private void addCookie() {

    if (nonNull(cookieMap)) {
      builder().addHeader(Header.cookie.v(), cookie2Str(cookie()));
    }

  }

  /**
   * cookie转字符串
   *
   * @param cookies map集合
   * @return cookie字符串
   */
  private static String cookie2Str(Map<String, String> cookies) {
    StringBuilder sb = new StringBuilder();

    cookies.forEach((k, v) -> sb.append(k).append('=').append(v).append("; "));

    return sb.toString();
  }


  /**
   * 请求超时时间设置
   *
   * @param timeout 超时时间
   * @return Req {@link Req}
   */
  public Req timeout(Duration timeout) {

    if (nonNull(timeout)) {
      return timeout(timeout, timeout, timeout);
    }

    return this;
  }

  /**
   * 请求超时时间设置
   *
   * @param connect 连接超时时间
   * @param write   写入超时时间
   * @param read    读取超时时间
   * @return Req {@link Req}
   */
  public Req timeout(Duration connect, Duration write, Duration read) {

    notNull(connect, "connect must not be null");
    notNull(write, "write must not be null");
    notNull(read, "read must not be null");

    this.timeout = new Timeout(connect, write, read);

    return this;
  }

  /**
   * 设置method
   *
   * @param method {@link Method}
   * @return Req {@link Req}
   */
  public Req method(final Method method) {
    this.method = method;
    return this;
  }

  /**
   * 添加请求头
   *
   * @param headers map集合
   * @return Req {@link Req}
   */
  public Req headers(final Map<String, String> headers) {
    if (nonNull(headers)) {
      headers.forEach(builder()::addHeader);
    }

    return this;
  }

  /**
   * 添加请求头
   *
   * @param name  名称
   * @param value 值
   * @return Req {@link Req}
   */
  public Req header(final String name, final String value) {
    if (nonNull(name) && nonNull(value)) {
      builder.header(name, value);
    }

    return this;
  }

  /**
   * 添加cookie
   *
   * @param cookies map集合
   * @return Req {@link Req}
   */
  public Req cookies(final Map<String, String> cookies) {
    if (nonNull(cookies)) {
      cookie().putAll(cookies);
    }

    return this;
  }

  /**
   * 添加cookie
   *
   * @param k key
   * @param v value
   * @return Req {@link Req}
   */
  public Req cookie(final String k, final String v) {
    if (nonNull(k) && nonNull(v)) {
      cookie().put(k, v);
    }

    return this;
  }

  /**
   * 设置contentType
   *
   * @param contentType {@link ContentType}
   * @return Req {@link Req}
   */
  public Req contentType(final ContentType contentType) {
    if (nonNull(contentType)) {
      this.contentType = contentType.v();

      header(Header.content_type.v(), String.join(";charset=", contentType(), charset().name()));
    }

    return this;
  }

  /**
   * 设置charset
   *
   * @param charset 编码类型
   * @return Req {@link Req}
   */
  public Req charset(final Charset charset) {
    this.charset = charset;
    return this;
  }

  /**
   * 设置user-agent
   *
   * @param ua user-agent
   * @return Req {@link Req}
   */
  public Req ua(final String ua) {
    builder().header(Header.user_agent.v(), ua);
    return this;
  }

  /**
   * 设置authorization
   *
   * @param auth 认证凭证
   * @return Req {@link Req}
   */
  public Req auth(final String auth) {
    builder().header(Header.authorization.v(), auth);
    return this;
  }

  /**
   * 设置bearer类型的authorization
   *
   * @param token bearer token
   * @return Req {@link Req}
   */
  public Req bearer(final String token) {
    return auth("Bearer " + token);
  }

  /**
   * 设置url
   *
   * @param url url
   * @return Req {@link Req}
   */
  public Req url(final String url) {
    notNull(url, "url must not be null");

    try {
      this.url = new URL(urlRegex(url.trim(), ReqType.ws.equals(reqType())));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }

    return this;
  }

  /**
   * 设置url的协议
   *
   * @param scheme 协议
   * @return Req {@link Req}
   */
  public Req scheme(final String scheme) {
    this.scheme = scheme;
    return this;
  }

  /**
   * 设置url的主机地址
   *
   * @param host 主机地址
   * @return Req {@link Req}
   */
  public Req host(final String host) {
    this.host = host;
    return this;
  }

  /**
   * 设置url的端口
   *
   * @param port 端口
   * @return Req {@link Req}
   */
  public Req port(final int port) {
    this.port = port;
    return this;
  }

  /**
   * 设置url的path
   *
   * @param path path
   * @return Req {@link Req}
   */
  public Req path(final String path) {
    if (nonNull(path)) {
      paths().add(removeFirstSlash(path));
    }

    return this;
  }

  /**
   * 设置url的query
   *
   * @param k 键
   * @param v 值
   * @return
   */
  public Req query(final String k, final String v) {
    if (nonNull(k) && nonNull(v)) {
      query().put(k, v);
    }

    return this;
  }

  /**
   * 设置url的query
   *
   * @param k  键
   * @param vs 值集合
   * @return Req {@link Req}
   */
  public Req query(final String k, final Iterable<String> vs) {
    if (nonNull(k) && nonNull(vs)) {
      for (String v : vs) {
        query().put(k, v);
      }
    }

    return this;
  }

  /**
   * 设置url的query
   *
   * @param querys query的map集合
   * @return Req {@link Req}
   */
  public Req query(final Map<String, String> querys) {
    if (nonNull(querys)) {
      querys.forEach(query()::put);
    }

    return this;
  }

  /**
   * 添加上传文件，只有multipart方式才可以
   *
   * @param name     名称
   * @param fileName 文件名
   * @param bytes    文件内容
   * @return Req {@link Req}
   */
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

  /**
   * 设置form表单，只有form_urlencoded或者multipart方式才可以
   *
   * @param name  名称
   * @param value 值
   * @return Req {@link Req}
   */
  public Req form(final String name, final String value) {
    if (isFormUrl() || isMul()) {
      form().put(name, value);
    }

    return this;
  }

  /**
   * 设置form表单，只有form_urlencoded或者multipart方式才可以
   *
   * @param form form表单map
   * @return Req {@link Req}
   */
  public Req form(final Map<String, String> form) {
    if (isFormUrl() || isMul()) {
      form().putAll(form);
    }

    return this;
  }

  /**
   * 添加json字符串的body
   *
   * @param json json字符串
   * @return Req {@link Req}
   */
  public Req json(final String json) {
    return body(json, ContentType.json);
  }

  /**
   * 添加数据类型的对象，使用fastjson转换成json字符串
   *
   * @param json 数据类型的对象
   * @return Req {@link Req}
   */
  public Req json(final Object json) {
    return body(JSON.toJSONString(json), ContentType.json);
  }

  /**
   * 自定义设置json对象
   *
   * @param str         内容
   * @param contentType 类型 {@link ContentType}
   * @return Req {@link Req}
   */
  public Req body(final String str, final ContentType contentType) {
    contentType(contentType);
    this.strBody = str;
    return this;
  }

  /**
   * 异步请求时成功时调用函数
   *
   * @param success 成功回调函数
   * @return Req {@link Req}
   */
  public Req success(final Consumer<Res> success) {
    this.success = success;
    return this;
  }

  /**
   * 异步请求失败时调用函数
   *
   * @param fail 失败回调函数
   * @return Req {@link Req}
   */
  public Req fail(final Consumer<Throwable> fail) {
    this.fail = fail;
    return this;
  }

  /**
   * 重试   设置成3会额外多请求3次，加上本身请求的一次，一共是4次
   *
   * @param max 最大重试次数
   * @return Req {@link Req}
   */
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

  /**
   * 重试   设置成3会额外多请求3次，加上本身请求的一次，一共是4次
   *
   * @param max       最大重试次数
   * @param delay     重试间隔时间
   * @param predicate 重试条件
   * @return Req {@link Req}
   */
  public Req retry(
      final int max,
      final Duration delay,
      final BiPredicate<Res, Throwable> predicate) {
    this.max = max;
    this.delay = delay;
    this.predicate = predicate;
    return this;
  }

  /**
   * 设置ws协议的监听函数
   *
   * @param listener 监听函数
   * @return Req {@link Req}
   */
  public Req wsListener(final WSListener listener) {
    this.wsListener = listener;
    return this;
  }

  /**
   * sse协议调用时的监听函数
   *
   * @param sseListener 监听函数
   * @return Req {@link Req}
   */
  public Req sseListener(final SSEListener sseListener) {
    this.sseListener = sseListener;
    return this;
  }

  /**
   * 请求中添加的附件
   *
   * @param k 键
   * @param v 值
   * @return Req {@link Req}
   */
  public Req setAttachment(final Object k, final Object v) {
    attachment().put(k, v);
    return this;
  }

  //get
  public Map<Object, Object> attachment() {
    if (isNull(attachment)) {
      this.attachment = new HashMap<>();
    }
    return attachment;
  }

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
      this.paths = new LinkedList<>();
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
      this.queryMap = new MultiValueMap<>();
    }

    return queryMap;
  }

  public Map<String, String> form() {
    if (isNull(formMap)) {
      this.formMap = new HashMap<>();
    }

    return formMap;
  }

  public Map<String, String> cookie() {
    if (isNull(cookieMap)) {
      this.cookieMap = new HashMap<>();
    }

    return cookieMap;
  }

  public MultipartBody.Builder mul() {
    if (isNull(mul)) {
      this.mul = new MultipartBody.Builder();
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
