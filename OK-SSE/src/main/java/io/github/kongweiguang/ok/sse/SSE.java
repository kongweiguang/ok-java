//package io.github.kongweiguang.ok.sse;
//
//import io.github.kongweiguang.ok.OK;
//import io.github.kongweiguang.ok.sse.core.EventMessage;
//import io.github.kongweiguang.ok.sse.core.OkHttpSSEEngine;
//import io.github.kongweiguang.ok.sse.core.OkHttpSseListener;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import okhttp3.sse.EventSource;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//
//public class SSE extends OK {
//
//    protected SSE(final OkHttpClient c) {
//        super(c);
//    }
//
//    private final OkHttpSSEEngine engine;
//    private OkHttpClient client;
//
//    private BiConsumer<Request, Response> open;
//    private Consumer<EventMessage> event;
//    private Consumer<okhttp3.Request> close;
//    private Consumer<Throwable> fail;
//
//    /**
//     * 构造方法
//     *
//     * @param request 请求参数
//     */
//    private SSE(Request request) {
//        this.engine = new OkHttpSSEEngine(request);
//    }
//
//    /**
//     * 构造工厂类
//     *
//     * @param request 请求参数
//     * @return this
//     */
//    public static io.github.kongweiguang.ok.sse.core.SSE of(Request request) {
//        return new io.github.kongweiguang.ok.sse.core.SSE(request);
//    }
//
//    /**
//     * 打开链接时调用方法
//     *
//     * @param open 需要执行的方法
//     * @return this
//     */
//
//    public io.github.kongweiguang.ok.sse.core.SSE onOpen(BiConsumer<okhttp3.Request, Response> open) {
//        this.open = open;
//        return this;
//    }
//
//    /**
//     * 接收到消息调用方法
//     *
//     * @param event 需要执行的方法
//     * @return this
//     */
//    public io.github.kongweiguang.ok.sse.core.SSE onEvent(Consumer<EventMessage> event) {
//        this.event = event;
//        return this;
//    }
//
//    /**
//     * 连接关闭时调用方法
//     *
//     * @param close 执行的方法
//     * @return this
//     */
//    public io.github.kongweiguang.ok.sse.core.SSE onClosed(Consumer<okhttp3.Request> close) {
//        this.close = close;
//        return this;
//    }
//
//    /**
//     * 请求失败时调用
//     *
//     * @param fail 执行的方法
//     * @return this
//     */
//    public io.github.kongweiguang.ok.sse.core.SSE onFailure(Consumer<Throwable> fail) {
//        this.fail = fail;
//        return this;
//    }
//
//    /**
//     * 完成构建，发送请求
//     *
//     * @return this
//     */
//    public SSE ok() {
//        engine.listen(new OkHttpSseListener() {
//            @Override
//            public void onOpen(@NotNull final EventSource eventSource, @NotNull final Response response) {
//                io.github.kongweiguang.ok.sse.core.SSE.this.client = client;
//
//                if (ObjUtil.isNotEmpty(io.github.kongweiguang.ok.sse.core.SSE.this.open)) {
//                    io.github.kongweiguang.ok.sse.core.SSE.this.open.accept(eventSource.request(), response);
//                }
//            }
//
//            @Override
//            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
//                if (ObjUtil.isNotEmpty(io.github.kongweiguang.ok.sse.core.SSE.this.event)) {
//                    io.github.kongweiguang.ok.sse.core.SSE.this.event.accept(new EventMessage(id, type, data));
//                }
//            }
//
//            @Override
//            public void onClosed(@NotNull final EventSource eventSource) {
//                if (ObjUtil.isNotEmpty(io.github.kongweiguang.ok.sse.core.SSE.this.close)) {
//                    io.github.kongweiguang.ok.sse.core.SSE.this.close.accept(eventSource.request());
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull final EventSource eventSource, @Nullable final Throwable t, @Nullable final Response response) {
//                if (ObjUtil.isNotEmpty(io.github.kongweiguang.ok.sse.core.SSE.this.fail)) {
//                    io.github.kongweiguang.ok.sse.core.SSE.this.fail.accept(t);
//                }
//            }
//        });
//
//        return this;
//    }
//
//    /**
//     * 关闭当前的请求线程
//     */
//    public void close() {
//        if (ObjUtil.isNotEmpty(client)) {
//            client.dispatcher().executorService().shutdown();
//        }
//    }
//}
