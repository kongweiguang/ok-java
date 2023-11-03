package io.github.kongweiguang.ok.ws;

import static java.util.Objects.nonNull;

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
 *
 * @author kongweiguang
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

  /**
   * 发送消息
   *
   * @param text 字符串类型
   * @return {@link WSListener}
   */
  public WSListener send(final String text) {
    if (nonNull(ws)) {
      ws.send(text);
    }

    return this;
  }

  /**
   * 发送消息
   *
   * @param bytes byte类型
   * @return {@link WSListener}
   */
  public WSListener send(final byte[] bytes) {
    if (nonNull(ws)) {
      ws.send(ByteString.of(bytes));
    }

    return this;
  }

  /**
   * 关闭连接
   */
  public void close() {
    ws.cancel();
  }


  /**
   * 打开连接触发事件
   *
   * @param req {@link Req}
   * @param res {@link Res}
   */
  public void open(final Req req, final Res res) {
  }

  /**
   * 收到消息触发事件
   *
   * @param req  请求信息 {@link Req}
   * @param text string类型响应数据 {@link String}
   */
  public void msg(final Req req, final String text) {
  }


  /**
   * 收到消息触发事件
   *
   * @param req   请求信息 {@link Req}
   * @param bytes byte类型响应数据 {@link Byte}
   */
  public void msg(final Req req, final byte[] bytes) {
  }

  /**
   * 失败触发事件
   *
   * @param req 请求信息 {@link Req}
   * @param res 响应信息 {@link Res}
   * @param t   异常信息 {@link Throwable}
   */
  public void fail(final Req req, final Res res, final Throwable t) {
  }

  /**
   * 关闭触发事件
   *
   * @param req    请求信息 {@link Req}
   * @param code   状态码
   * @param reason 原因
   */
  public void closing(final Req req, final int code, final String reason) {
  }

  /**
   * 关闭触发事件
   *
   * @param req    请求信息 {@link Req}
   * @param code   状态码
   * @param reason 原因
   */
  public void closed(final Req req, final int code, final String reason) {
  }

}
