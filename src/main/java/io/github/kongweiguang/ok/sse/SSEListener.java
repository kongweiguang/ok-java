package io.github.kongweiguang.ok.sse;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.nonNull;

/**
 * sse请求监听
 *
 * @author kongweiguang
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

    /**
     * 关闭当前连接
     */
    public void close() {
        if (nonNull(es)) {
            es.cancel();
        }
    }

    /**
     * 打开连接触发事件
     *
     * @param req 请求信息 {@link Req}
     * @param res 响应信息 {@link Res}
     */
    public void open(final Req req, final Res res) {
    }

    /**
     * 获取消息触发事件
     *
     * @param req 请求信息 {@link Req}
     * @param msg 事件信息 {@link SseEvent}
     */
    public abstract void event(final Req req, final SseEvent msg);

    /**
     * 失败时触发事件
     *
     * @param req 请求信息 {@link Req}
     * @param res 响应信息 {@link Res}
     * @param t   异常信息 {@link Throwable}
     */
    public void fail(final Req req, final Res res, final Throwable t) {
    }

    /**
     * 关闭时出发连接
     *
     * @param req 请求信息 {@link Req}
     */
    public void closed(final Req req) {
    }

}
