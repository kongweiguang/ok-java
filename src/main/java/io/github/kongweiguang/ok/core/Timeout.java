package io.github.kongweiguang.ok.core;

/**
 * 参考solon-cloud-httputils中的请求超时
 *
 * @author kongweiguang
 */
public final class Timeout {
    public final int connectTimeout;
    public final int writeTimeout;
    public final int readTimeout;

    public Timeout(int timeout) {
        this.connectTimeout = timeout;
        this.writeTimeout = timeout;
        this.readTimeout = timeout;
    }

    public Timeout(int connectTimeout, int writeTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.writeTimeout = writeTimeout;
        this.readTimeout = readTimeout;
    }
}
