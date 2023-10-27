package io.github.kongweiguang.ok;

import static java.util.Objects.nonNull;

import io.github.kongweiguang.ok.core.Config;
import io.github.kongweiguang.ok.core.Util;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.sse.EventSources;

/**
 * 发送请求
 */
public final class OK {

  private final OkHttpClient C;
  private final Request request;
  private Req req;
  private boolean async;
  private boolean retry;

  private OK(final Req req) {
    this.C = Config.client();
    req.bf();

    this.request = req.builder().build();
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

    if (reqType()) {
      return http0(new AtomicInteger(req().max()), req().delay(), req().predicate());
    }

    return CompletableFuture.completedFuture(null);
  }

  private boolean reqType() {
    switch (req().reqType()) {
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
      return CompletableFuture.supplyAsync(this::execute, Config.exec())
          .handle((r, t) -> {
            if (handleRetry(max, duration, predicate, r, t)) {
              return http0(max, duration, predicate).join();
            }

            if (nonNull(t) && nonNull(req().fail())) {
              req().fail().accept(t);
            } else if (r.isOk() && nonNull(req().success())) {
              req().success().accept(r);
            }

            return r;
          });

    } else {
      return CompletableFuture.completedFuture(execute())
          .handle((r, t) -> {
            if (handleRetry(max, duration, predicate, r, t)) {
              return http0(max, duration, predicate).join();
            }

            return r;
          });
    }

  }

  private boolean handleRetry(final AtomicInteger max,
      final Duration duration,
      final BiPredicate<Res, Throwable> predicate,
      final Res r, final Throwable t) {

    if (retry() && (max.getAndDecrement() > 0 && predicate.test(r, t))) {
      Util.sleep(duration.toMillis());
      return true;
    }

    return false;
  }

  private Res execute() {
    try {
      return Res.of(client().newCall(request()).execute());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  private void ws0() {
    client().newWebSocket(request(), req().wsListener());
  }

  private void sse0() {
    EventSources.createFactory(client()).newEventSource(request(), req().sseListener());
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
  public OkHttpClient client() {
    return C;
  }

  public Request request() {
    return request;
  }

  public Req req() {
    return req;
  }

  public boolean async() {
    return async;
  }

  public boolean retry() {
    return retry;
  }
}
