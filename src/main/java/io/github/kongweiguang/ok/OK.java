package io.github.kongweiguang.ok;

import static java.util.Objects.nonNull;

import io.github.kongweiguang.ok.core.Config;
import io.github.kongweiguang.ok.core.Const;
import io.github.kongweiguang.ok.core.ContentType;
import io.github.kongweiguang.ok.core.Header;
import io.github.kongweiguang.ok.core.MultiValueMap;
import io.github.kongweiguang.ok.core.Util;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;
import okhttp3.sse.EventSources;

/**
 * 发送请求
 */
public final class OK {

  private final OkHttpClient C;
  private final Request.Builder builder;
  private Req req;
  private boolean async;
  private boolean retry;

  private OK(final Req req) {
    this.C = Config.client();
    this.builder = new Request.Builder();

    req(req).retry(req.max() > 0);
  }

  /**
   * <h2>发送请求</h2>
   * <p>
   * 只有http请求有返回值，ws和sse没有返回值
   *
   * @param req 请求参数 {@link Req}
   * @return Res {@link Res}
   */
  public static Res ok(final Req req) {
    return new OK(req).ojbk().join();
  }

  /**
   * <h2>异步调用发送请求</h2>
   * 只有http请求有返回值，ws和sse没有返回值
   *
   * @param req 请求参数 {@link Req}
   * @return Res {@link Res}
   */
  public static CompletableFuture<Res> okAsync(final Req req) {
    return new OK(req).async(true).ojbk();
  }


  private CompletableFuture<Res> ojbk() {
    bf();

    builder().tag(Req.class, req());

    if (reqType()) {
      return http0(new AtomicInteger(req().max()), req().delay(), req().predicate());
    }

    return CompletableFuture.completedFuture(null);
  }

  private boolean reqType() {
    switch (req().typeEnum()) {
      case http:
        return true;
      case ws:
        ws0();
        break;
      case sse:
        sse0();
        break;
    }

    return false;
  }

  private CompletableFuture<Res> http0(final AtomicInteger max,
      final Duration duration,
      final BiPredicate<Res, Throwable> predicate) {

    if (async()) {
      return CompletableFuture.supplyAsync(this::execute, Config.exec()).whenComplete((r, t) -> {
        if (retry() && (max.getAndDecrement() > 0 && predicate.test(r, t))) {
          Util.sleep(duration.toMillis());
          http0(max, duration, predicate);
          return;
        }

        if (nonNull(t) && nonNull(req().fail())) {
          req().fail().accept(t);
        } else if (r.isOk() && nonNull(req().success())) {
          req().success().accept(r);
        }
      });

    } else {
      return CompletableFuture.completedFuture(execute()).handle((r, t) -> {
        if (retry() && (max.getAndDecrement() > 0 && predicate.test(r, t))) {
          Util.sleep(duration.toMillis());
          return http0(max, duration, predicate).join();
        }
        return r;
      });
    }

  }

  private Res execute() {
    try (Response execute = client().newCall(builder().build()).execute()) {
      return Res.of(execute);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void bf() {
    addMethod();
    addQuery();
    addHeader();
  }

  private void addMethod() {
    builder().method(req().method().name(), addBody());
  }

  private RequestBody addBody() {
    RequestBody rb = null;

    if (HttpMethod.permitsRequestBody(req().method().name())) {
      if (req().isMultipart()) {
        //multipart 格式提交
        req().contentType(ContentType.multipart).form().forEach(req().mul()::addFormDataPart);

        rb = req().mul().setType(MediaType.parse(req().contentType())).build();

      } else if (req().isForm()) {
        final FormBody.Builder b = new FormBody.Builder(req().charset());
        //form_urlencoded 格式提交
        req().contentType(ContentType.form_urlencoded).form().forEach(b::addEncoded);

        rb = b.build();

      } else {
        //字符串提交
        rb = RequestBody.create(req().strBody(), MediaType.parse(req().contentType()));

      }
    }

    return rb;
  }

  private void addQuery() {

    if (nonNull(req().url())) {
      req().scheme(req().url().getProtocol())
          .host(req().url().getHost())
          .port(req().url().getPort() == -1 ? Const.port : req().url().getPort())
          .pathFirst(req().url().getPath());

      Optional.ofNullable(req().url().getQuery()).map(e -> e.split("&")).ifPresent(qr -> {
        for (String part : qr) {
          String[] kv = part.split("=");
          if (kv.length > 1) {
            req().query(kv[0], kv[1]);
          }
        }
      });
    }

    final HttpUrl.Builder ub = new HttpUrl.Builder();

    Optional.ofNullable(req().query())
        .map(MultiValueMap::map)
        .ifPresent(map -> map.forEach((k, v) ->
            v.forEach(e -> ub.addEncodedQueryParameter(k, e))));

    req().paths().forEach(ub::addPathSegments);

    ub.scheme(req().scheme());
    ub.host(req().host());
    ub.port(req().port());

    builder().url(ub.build());
  }

  private void addHeader() {

    if (!req().headers().isEmpty()) {
      req().headers().forEach(builder()::addHeader);
    }

    if (!req().cookie().isEmpty()) {
      builder().addHeader(Header.cookie.v(), cookie2Str(req().cookie()));
    }
  }

  private static String cookie2Str(Map<String, String> cookies) {
    StringBuilder sb = new StringBuilder();

    cookies.forEach((k, v) -> sb.append(k).append('=').append(v).append("; "));

    return sb.toString();
  }

  private void ws0() {
    client().newWebSocket(builder().build(), req().wsListener());
  }

  private void sse0() {
    EventSources.createFactory(client()).newEventSource(builder().build(), req().sseListener());
  }


  private OK req(final Req req) {
    this.req = req;
    return this;
  }

  private OK retry(final boolean retry) {
    this.retry = retry;
    return this;
  }

  private OK async(final boolean async) {
    this.async = async;
    return this;
  }

  //get
  private OkHttpClient client() {
    return C;
  }

  private Request.Builder builder() {
    return builder;
  }

  private Req req() {
    return req;
  }

  private boolean async() {
    return async;
  }

  public boolean retry() {
    return retry;
  }
}
