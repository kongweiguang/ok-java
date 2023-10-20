package io.github.kongweiguang.ok.ws;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ws监听器
 */
public abstract class WSListener extends WebSocketListener {

  private WebSocket ws;

  @Override
  public void onOpen(@NotNull final WebSocket webSocket, @NotNull final Response response) {
    this.ws = webSocket;
    open(webSocket.request().tag(Req.class), Res.of(response));
  }

  @Override
  public void onMessage(@NotNull final WebSocket webSocket, @NotNull final String text) {
    this.ws = webSocket;
    msg(webSocket.request().tag(Req.class), text);
  }

  @Override
  public void onMessage(@NotNull final WebSocket webSocket, @NotNull final ByteString bytes) {
    this.ws = webSocket;
    msg(webSocket.request().tag(Req.class), bytes.toByteArray());
  }

  @Override
  public void onFailure(@NotNull final WebSocket webSocket,
      @NotNull final Throwable t,
      @Nullable final Response response) {
    this.ws = webSocket;
    fail(webSocket.request().tag(Req.class), Res.of(response), t);
  }

  @Override
  public void onClosing(@NotNull final WebSocket webSocket, final int code,
      @NotNull final String reason) {
    this.ws = webSocket;
    closing(webSocket.request().tag(Req.class), code, reason);
  }

  @Override
  public void onClosed(@NotNull final WebSocket webSocket, final int code,
      @NotNull final String reason) {
    this.ws = webSocket;
    closed(webSocket.request().tag(Req.class), code, reason);
  }


  public WSListener send(final String text) {
    ws.send(text);
    return this;
  }

  public WSListener send(final byte[] bytes) {
    ws.send(ByteString.of(bytes));
    return this;
  }

  public void close() {
    ws.cancel();
  }


  public void open(final Req req, final Res res) {
  }

  public abstract void msg(final Req req, final String text);

  public void msg(final Req req, final byte[] bytes) {
  }

  public void fail(final Req req, final Res res, final Throwable t) {
  }

  public void closing(final Req req, final int code, final String reason) {
  }

  public void closed(final Req req, final int code, final String reason) {
  }

}
