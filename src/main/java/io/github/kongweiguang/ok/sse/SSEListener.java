package io.github.kongweiguang.ok.sse;

import static java.util.Objects.nonNull;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * sse请求监听
 *
 * @author kongweiguang
 * @since 0.1
 */
public abstract class SSEListener extends EventSourceListener {

  public EventSource es;


  @Override
  public void onOpen(@NotNull final EventSource eventSource, @NotNull final Response response) {
    this.es = eventSource;
    open(eventSource.request().tag(Req.class), Res.of(response));
  }

  @Override
  public void onEvent(@NotNull final EventSource eventSource,
      @Nullable final String id,
      @Nullable final String type,
      @NotNull final String data) {
    this.es = eventSource;
    event(eventSource.request().tag(Req.class), new SseEvent().id(id).type(type).data(data));
  }

  @Override
  public void onFailure(@NotNull final EventSource eventSource,
      @Nullable final Throwable t,
      @Nullable final Response response) {
    this.es = eventSource;
    fail(eventSource.request().tag(Req.class), Res.of(response), t);
  }

  @Override
  public void onClosed(@NotNull final EventSource eventSource) {
    this.es = eventSource;
    closed(eventSource.request().tag(Req.class));
  }


  public void close() {
    if (nonNull(es)) {
      es.cancel();
    }
  }

  public void open(final Req req, final Res res) {
  }

  public abstract void event(final Req req, final SseEvent msg);

  public void fail(final Req req, final Res res, final Throwable t) {
  }

  public void closed(final Req req) {
  }

}
