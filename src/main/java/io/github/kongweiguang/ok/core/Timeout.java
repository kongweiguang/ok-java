package io.github.kongweiguang.ok.core;

import java.time.Duration;

/**
 * 设置请求超时
 *
 * @author kongweiguang
 */
public final class Timeout {

    private final Duration connect;
    private final Duration write;
    private final Duration read;

    public Timeout(Duration connect, Duration write, Duration read) {
        this.connect = connect;
        this.write = write;
        this.read = read;
    }

    public Duration connect() {
        return connect;
    }

    public Duration write() {
        return write;
    }

    public Duration read() {
        return read;
    }
}
