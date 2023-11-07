package io.github.kongweiguang.ok;


import static io.github.kongweiguang.ok.core.Util.cookie2Str;
import static io.github.kongweiguang.ok.core.Util.encode;
import static io.github.kongweiguang.ok.core.Util.fixUrl;
import static io.github.kongweiguang.ok.core.Util.notNull;
import static io.github.kongweiguang.ok.core.Util.removeFirstSlash;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.alibaba.fastjson2.JSON;
import io.github.kongweiguang.ok.core.Client;
import io.github.kongweiguang.ok.core.ContentType;
import io.github.kongweiguang.ok.core.Header;
import io.github.kongweiguang.ok.core.Method;
import io.github.kongweiguang.ok.core.ReqType;
import io.github.kongweiguang.ok.core.Timeout;
import io.github.kongweiguang.ok.sse.SSEListener;
import io.github.kongweiguang.ok.ws.WSListener;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
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
 * http请求构建器
 *
 * @author kongweiguang
 */
public final class ReqBuilder {

  private ReqType reqType;
  private final Request.Builder builder;

  //header
  private Method method;
  private Timeout timeout;
  private Map<String, String> cookieMap;
  private String contentType;
  private Charset charset;

  //url
  private HttpUrl.Builder urlBuilder;

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

  ReqBuilder() {
    this.charset = StandardCharsets.UTF_8;
    this.method = Method.GET;
    this.reqType = ReqType.http;
    this.builder = new Builder();
  }

  /**
   * 请求的类型
   *
   * @param typeEnum 请求类型 {@link ReqType}
   * @return Res {@link ReqBuilder}
   */
  public ReqBuilder reqType(final ReqType typeEnum) {
    this.reqType = typeEnum;
    return this;
  }

  /**
   * 设置请求为form_urlencoded类型
   *
   * @return Res {@link ReqBuilder}
   */
  public ReqBuilder formUrlencoded() {
    this.formUrl = true;
    return method(Method.POST).contentType(ContentType.form_urlencoded);
  }

  /**
   * 设置请求为multipart类型
   *
   * @return Res {@link ReqBuilder}
   */
  public ReqBuilder multipart() {
    this.multipart = true;
    return method(Method.POST).contentType(ContentType.multipart);
  }

  /**
   * 同步请求
   *
   * @return Res {@link ReqBuilder}
   */
  public Res ok() {
    return ok(Client.of(timeout));
  }

  /**
   * 同步请求，自定义client
   *
   * @param client {@link OkHttpClient}
   * @return Res {@link ReqBuilder}
   */
  public Res ok(final OkHttpClient client) {
    return OK.ok(this, client);
  }

  /**
   * 异步请求
   *
   * @return Res {@link ReqBuilder}
   */
  public CompletableFuture<Res> okAsync() {
    return okAsync(Client.of(timeout));
  }

  /**
   * 异步请求，自定义client
   *
   * @param client {@link OkHttpClient}
   * @return Res {@link ReqBuilder}
   */
  public CompletableFuture<Res> okAsync(final OkHttpClient client) {
    return OK.okAsync(this, client);
  }

  /**
   * 请求前初始化
   */
  void bf() {
    //method
    builder().method(method().name(), addBody());

    //url
    builder().url(urlBuilder().build());

    //cookie
    if (nonNull(cookieMap)) {
      builder().addHeader(Header.cookie.v(), cookie2Str(cookie()));
    }

    //tag
    builder().tag(ReqBuilder.class, this);
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
   * 请求超时时间设置
   *
   * @param timeout 超时时间
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder timeout(Duration timeout) {

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
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder timeout(Duration connect, Duration write, Duration read) {

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
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder method(final Method method) {
    this.method = method;
    return this;
  }

  /**
   * 添加请求头
   *
   * @param headers map集合
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder headers(final Map<String, String> headers) {
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
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder header(final String name, final String value) {
    if (nonNull(name) && nonNull(value)) {
      builder.header(name, value);
    }

    return this;
  }

  /**
   * 添加cookie
   *
   * @param cookies map集合
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder cookies(final Map<String, String> cookies) {
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
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder cookie(final String k, final String v) {
    if (nonNull(k) && nonNull(v)) {
      cookie().put(k, v);
    }

    return this;
  }

  /**
   * 设置contentType
   *
   * @param contentType {@link ContentType}
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder contentType(final ContentType contentType) {
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
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder charset(final Charset charset) {
    this.charset = charset;
    return this;
  }

  /**
   * 设置user-agent
   *
   * @param ua user-agent
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder ua(final String ua) {
    builder().header(Header.user_agent.v(), ua);
    return this;
  }

  /**
   * 设置authorization
   *
   * @param auth 认证凭证
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder auth(final String auth) {
    builder().header(Header.authorization.v(), auth);
    return this;
  }

  /**
   * 设置bearer类型的authorization
   *
   * @param token bearer token
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder bearer(final String token) {
    return auth("Bearer " + token);
  }

  /**
   * 设置basic类型的authorization
   *
   * @param username 用户名
   * @param password 密码
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder basic(final String username, final String password) {
    return auth("Basic " + encode(username + ":" + password));
  }

  /**
   * 设置url
   *
   * @param url url
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder url(final String url) {
    notNull(url, "url must not be null");

    this.urlBuilder =
        HttpUrl.parse(fixUrl(url.trim(), ReqType.ws.equals(reqType())))
            .newBuilder();

    return this;
  }

  /**
   * 设置url的协议
   *
   * @param scheme 协议
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder scheme(final String scheme) {
    urlBuilder().scheme(scheme);
    return this;
  }

  /**
   * 设置url的主机地址
   *
   * @param host 主机地址
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder host(final String host) {
    urlBuilder().host(host);
    return this;
  }

  /**
   * 设置url的端口
   *
   * @param port 端口
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder port(final int port) {
    urlBuilder().port(port);
    return this;
  }

  /**
   * 设置url的path
   *
   * @param path path
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder path(final String path) {
    if (nonNull(path)) {
      urlBuilder().addPathSegments(removeFirstSlash(path));
    }

    return this;
  }

  /**
   * 设置url的query
   *
   * @param k 键
   * @param v 值
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder query(final String k, final String v) {
    if (nonNull(k) && nonNull(v)) {
      urlBuilder().addEncodedQueryParameter(k, v);
    }

    return this;
  }

  /**
   * 设置url的query
   *
   * @param k  键
   * @param vs 值集合
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder query(final String k, final Iterable<String> vs) {
    if (nonNull(k) && nonNull(vs)) {
      for (String v : vs) {
        urlBuilder().addEncodedQueryParameter(k, v);
      }
    }

    return this;
  }

  /**
   * 设置url的query
   *
   * @param querys query的map集合
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder query(final Map<String, String> querys) {
    if (nonNull(querys)) {
      querys.forEach(urlBuilder()::addEncodedQueryParameter);
    }

    return this;
  }

  /**
   * 设置url的fragment
   *
   * @param fragment #号后面的内容
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder fragment(final String fragment) {
    if (nonNull(fragment)) {
      urlBuilder().encodedFragment(fragment);
    }

    return this;
  }

  /**
   * 添加上传文件，只有multipart方式才可以
   *
   * @param name     名称
   * @param fileName 文件名
   * @param bytes    文件内容
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder file(String name, String fileName, byte[] bytes) {
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
   * 添加form表单，只有form_urlencoded或者multipart方式才可以
   *
   * @param name  名称
   * @param value 值
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder form(final String name, final String value) {
    if (isFormUrl() || isMul()) {
      form().put(name, value);
    }

    return this;
  }

  /**
   * 添加form表单，只有form_urlencoded或者multipart方式才可以
   *
   * @param form form表单map
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder form(final Map<String, String> form) {
    if (isFormUrl() || isMul()) {
      form().putAll(form);
    }

    return this;
  }

  /**
   * 添加json字符串的body
   *
   * @param json json字符串
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder json(final String json) {
    return body(json, ContentType.json);
  }

  /**
   * 添加数据类型的对象，使用fastjson转换成json字符串
   *
   * @param json 数据类型的对象
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder json(final Object json) {
    return body(JSON.toJSONString(json), ContentType.json);
  }

  /**
   * 自定义设置json对象
   *
   * @param str         内容
   * @param contentType 类型 {@link ContentType}
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder body(final String str, final ContentType contentType) {
    contentType(contentType);
    this.strBody = str;
    return this;
  }

  /**
   * 异步请求时成功时调用函数
   *
   * @param success 成功回调函数
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder success(final Consumer<Res> success) {
    this.success = success;
    return this;
  }

  /**
   * 异步请求失败时调用函数
   *
   * @param fail 失败回调函数
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder fail(final Consumer<Throwable> fail) {
    this.fail = fail;
    return this;
  }

  /**
   * 重试   设置成3会额外多请求3次，加上本身请求的一次，一共是4次
   *
   * @param max 最大重试次数
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder retry(final int max) {
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
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder retry(
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
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder wsListener(final WSListener listener) {
    this.wsListener = listener;
    return this;
  }

  /**
   * sse协议调用时的监听函数
   *
   * @param sseListener 监听函数
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder sseListener(final SSEListener sseListener) {
    this.sseListener = sseListener;
    return this;
  }

  /**
   * 请求中添加的附件
   *
   * @param k 键
   * @param v 值
   * @return Req {@link ReqBuilder}
   */
  public ReqBuilder attachment(final Object k, final Object v) {
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

  public HttpUrl.Builder urlBuilder() {
    if (isNull(urlBuilder)) {
      url("");
    }

    return urlBuilder;
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
