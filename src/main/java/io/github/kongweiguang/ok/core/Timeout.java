package io.github.kongweiguang.ok.core;

/**
 * 设置请求超时
 *
 * @author kongweiguang，solon
 */
public final class Timeout {

  public final int connectTimeout;
  public final int writeTimeout;
  public final int readTimeout;

  public Timeout(int connectTimeout, int writeTimeout, int readTimeout) {
    this.connectTimeout = connectTimeout;
    this.writeTimeout = writeTimeout;
    this.readTimeout = readTimeout;
  }
}
