package io.github.kongweiguang.ok.sse;

/**
 * Sse 事件
 *
 * @author kongweiguang
 * @since 0.1
 */
public final class SseEvent {

  private final StringBuilder sb = new StringBuilder();

  private String id;
  private String type;
  private String retry;

  private String data;

  public static SseEvent of() {
    return new SseEvent();
  }

  /**
   * 添加 SSE "id" 行.
   */
  public SseEvent id(final String id) {
    this.id = id;
    append("id:").append(this.id).append("\n");
    return this;
  }

  /**
   * 添加 SSE "event" 行.
   */
  public SseEvent type(final String type) {
    this.type = type;
    append("event:").append(this.type).append("\n");
    return this;
  }

  /**
   * 添加 SSE "retry" 行.
   */
  public SseEvent reconnectTime(final long reconnectTimeMillis) {
    this.retry = String.valueOf(reconnectTimeMillis);
    append("retry:").append(this.retry).append("\n");
    return this;
  }

  /**
   * 添加 SSE "data" 行.
   */
  public SseEvent data(final String data) {
    this.data = data;
    append("data:").append(this.data).append("\n");
    return this;
  }

  private SseEvent append(final String text) {
    this.sb.append(text);
    return this;
  }


  public String id() {
    return id;
  }

  public String type() {
    return type;
  }

  public String retry() {
    return retry;
  }

  public String data() {
    return data;
  }

  @Override
  public String toString() {
    return sb.toString();
  }
}