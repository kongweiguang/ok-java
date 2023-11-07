package io.github.kongweiguang.ok;

import io.github.kongweiguang.ok.core.Config;
import io.github.kongweiguang.ok.core.Util;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.sse.EventSources;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

import static java.util.Objects.nonNull;

/**
 * 发送请求
 *
 * @author kongweiguang
 */
public final class OK {

    private final OkHttpClient client;
    private final Request request;
    private ReqBuilder reqBuilder;
    private boolean async;
    private boolean retry;

    private OK(final ReqBuilder reqBuilder, final OkHttpClient client) {
        this.client = client;
        reqBuilder.before();
        this.request = reqBuilder.builder().build();
        reqBuilder(reqBuilder).retry(reqBuilder.max() > 0);
    }

    /**
     * <h2>发送请求</h2>
     * <p>
     * 只有http请求有返回值，ws和sse没有返回值
     *
     * @param reqBuilder 请求参数 {@link Req}
     * @param client     OkHttpClient {@link OkHttpClient}
     * @return Res {@link Res}
     */
    public static Res ok(final ReqBuilder reqBuilder, final OkHttpClient client) {
        return new OK(reqBuilder, client).ojbk().join();
    }

    /**
     * <h2>异步调用发送请求</h2>
     * 只有http请求有返回值，ws和sse没有返回值
     *
     * @param reqBuilder 请求参数 {@link Req}
     * @param client     OkHttpClient {@link OkHttpClient}
     * @return Res {@link Res}
     */
    public static CompletableFuture<Res> okAsync(final ReqBuilder reqBuilder, final OkHttpClient client) {
        return new OK(reqBuilder, client).async(true).ojbk();
    }


    /**
     * 实际发送请求
     *
     * @return 结果
     */
    private CompletableFuture<Res> ojbk() {
        return reqType();
    }

    /**
     * 请求类型判断
     *
     * @return 是否http请求
     */
    private CompletableFuture<Res> reqType() {
        switch (reqBuilder().reqType()) {
            case http:
                return http0(
                        new AtomicInteger(reqBuilder().max()),
                        reqBuilder().delay(),
                        reqBuilder().predicate()
                );
            case ws:
                ws0();
                break;
            case sse:
                sse0();
                break;
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * http请求
     *
     * @param max       重试次数
     * @param duration  重试间隔
     * @param predicate 重试条件
     * @return 响应结果
     */
    private CompletableFuture<Res> http0(final AtomicInteger max,
                                         final Duration duration,
                                         final BiPredicate<Res, Throwable> predicate) {

        if (async()) {
            return CompletableFuture.supplyAsync(this::execute, Config.exec())
                    .handle((r, t) -> {
                        if (handleRetry(max, duration, predicate, r, t)) {
                            async(false);
                            return http0(max, duration, predicate).join();
                        }

                        if (nonNull(t) && nonNull(reqBuilder().fail())) {
                            reqBuilder().fail().accept(t);
                        } else if (r.isOk() && nonNull(reqBuilder().success())) {
                            reqBuilder().success().accept(r);
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

    /**
     * 处理是否重试
     *
     * @param max       重试次数
     * @param duration  重试间隔
     * @param predicate 重试条件
     * @param r         响应结果
     * @param t         异常
     * @return 是否重试
     */
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

    /**
     * 提交请求
     *
     * @return 响应结果 {@link Res}
     */
    private Res execute() {
        try {
            return Res.of(client().newCall(request()).execute());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ws请求
     */
    private void ws0() {
        client().newWebSocket(request(), reqBuilder().wsListener());
    }

    /**
     * sse请求
     */
    private void sse0() {
        EventSources.createFactory(client()).newEventSource(request(), reqBuilder().sseListener());
    }


    private OK reqBuilder(final ReqBuilder req) {
        this.reqBuilder = req;
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
        return client;
    }

    public Request request() {
        return request;
    }

    public ReqBuilder reqBuilder() {
        return reqBuilder;
    }

    public boolean async() {
        return async;
    }

    public boolean retry() {
        return retry;
    }
}
