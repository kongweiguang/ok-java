package io.github.kongweiguang.ok;

import io.github.kongweiguang.ok.core.Config;
import io.github.kongweiguang.ok.core.Const;
import io.github.kongweiguang.ok.core.ContentType;
import io.github.kongweiguang.ok.core.Header;
import io.github.kongweiguang.ok.core.MultiValueMap;
import io.github.kongweiguang.ok.core.Res;
import io.github.kongweiguang.ok.core.Util;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

import static java.util.Objects.nonNull;

/**
 * 发送请求
 */
public class OK {


    private final OkHttpClient C;
    private final Request.Builder builder;
    private Req req;
    private final CompletableFuture<Res> resFuture = new CompletableFuture<>();

    private OK(final OkHttpClient c) {
        this.C = c;
        this.builder = new Request.Builder();
    }


    public static OK of() {
        return new OK(Config.client());
    }

    /**
     * <h2>发送请求</h2>
     * <p>
     * 只有http请求有返回值，ws和sse没有返回值
     *
     * @return Res {@code  Res}
     */
    public Res ok(final Req req) {
        return okAsync(req).join();
    }

    public CompletableFuture<Res> okAsync(final Req req) {
        this.req = req;
        return ojbk();
    }

    private boolean reqType() {
        switch (req().typeEnum()) {
            case http:
                return true;
            case ws:
                ws0();
                break;
            case sse:
                sse0();
                break;
        }

        return false;
    }


    private CompletableFuture<Res> ojbk() {
        bf();

        builder().tag(Req.class, req());

        if (reqType()) {
            return http0(new AtomicInteger(req().max()), req().delay(), req().predicate());
        }

        return null;
    }

    private CompletableFuture<Res> http0(final AtomicInteger max, final Duration duration, final BiPredicate<Res, Throwable> predicate) {

        if (req().isAsync()) {
            client().newCall(builder().build())
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                            res().completeExceptionally(e);

                            if (nonNull(req().fail())) {
                                req().fail().accept(e);
                            }


                            res(max, duration, predicate.test(null, e), predicate, null);

                            call.cancel();
                        }

                        @Override
                        public void onResponse(@NotNull final Call call, @NotNull final Response res) throws IOException {
                            final Res r = Res.of(res);
                            res().complete(r);

                            if (nonNull(req().success())) {
                                req().success().accept(r);
                            }

                            res(max, duration, predicate.test(r, null), predicate, null);

                            call.cancel();
                        }
                    });

            return res();
        } else {
            try (Response execute = client().newCall(builder().build()).execute()) {
                final Res res = Res.of(execute);
                return res(max, duration, predicate.test(res, null), predicate, res);
            } catch (Exception e) {
                return res(max, duration, predicate, e);
            }
        }

    }

    private CompletableFuture<Res> res(final AtomicInteger max, final Duration duration, final BiPredicate<Res, Throwable> predicate, final Exception e) {
        if (req().isRetry() && max.getAndDecrement() > 0 && predicate.test(null, e)) {
            Util.sleep(duration.toMillis());
            return http0(max, duration, predicate);
        } else {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Res> res(final AtomicInteger max, final Duration duration, final boolean predicate, final BiPredicate<Res, Throwable> pre, final Res res) {
        if (req().isRetry() && max.getAndDecrement() > 0 && predicate) {
            Util.sleep(duration.toMillis());
            return http0(max, duration, pre);
        }

        res().complete(res);
        return res();
    }


    private void bf() {
        addQuery();
        addHeader();
        addMethod();
    }

    private void addQuery() {

        if (nonNull(req().url())) {
            req().scheme(req().url().getProtocol())
                    .host(req().url().getHost())
                    .port(req().url().getPort() == -1 ? Const.port : req().url().getPort())
                    .pathFirst(req().url().getPath());

            Optional.ofNullable(req().url().getQuery())
                    .map(e -> e.split("&"))
                    .ifPresent(qr -> {
                        for (String part : qr) {
                            String[] kv = part.split("=");
                            if (kv.length > 1) {
                                req().query(kv[0], kv[1]);
                            }
                        }
                    });
        }

        final HttpUrl.Builder ub = new HttpUrl.Builder();

        Optional.ofNullable(req().query())
                .map(MultiValueMap::map)
                .ifPresent(map -> map.forEach((k, v) ->
                        v.forEach(e -> ub.addEncodedQueryParameter(k, e)))
                );


        req().paths().forEach(ub::addPathSegments);

        ub.scheme(req().scheme());
        ub.host(req().host());
        ub.port(req().port());

        builder().url(ub.build());
    }


    private void addHeader() {

        if (!req().headers().isEmpty()) {
            req().headers().forEach(builder()::addHeader);
        }

        if (!req().cookie().isEmpty()) {
            builder().addHeader(Header.cookie.v(), cookie2Str(req().cookie()));
        }
    }

    private static String cookie2Str(Map<String, String> cookies) {
        StringBuilder sb = new StringBuilder();

        cookies.forEach((k, v) -> sb.append(k).append('=').append(v).append("; "));

        return sb.toString();
    }


    private void addMethod() {
        builder().method(req().method().name(), addBody());
    }


    private RequestBody addBody() {
        RequestBody rb = null;

        if (HttpMethod.permitsRequestBody(req().method().name())) {
            if (req().isMultipart()) {
                //multipart 格式提交
                req()
                        .contentType(ContentType.multipart)
                        .form()
                        .forEach(req().mul()::addFormDataPart);

                rb = req()
                        .mul()
                        .setType(MediaType.parse(req().contentType()))
                        .build();

            } else if (req().isForm()) {
                final FormBody.Builder b = new FormBody.Builder(req().charset());

                //form_urlencoded 格式提交
                req()
                        .contentType(ContentType.form_urlencoded)
                        .form()
                        .forEach(b::addEncoded);

                rb = b.build();

            } else {
                //字符串提交
                rb = RequestBody.create(
                        req().strBody(),
                        MediaType.parse(req().contentType())
                );

//                rb = new ReqBody(req().contentType(), req().charset(), req().reqBody().getBytes(req().charset()));
            }
        }

        return rb;
    }


    private void ws0() {
        client().newWebSocket(builder().build(), req().wsListener());
    }

    private void sse0() {
        EventSources.createFactory(client())
                .newEventSource(
                        builder().build(),
                        req().sseListener()
                );
    }


    private OkHttpClient client() {
        return C;
    }

    private Request.Builder builder() {
        return builder;
    }


    private Req req() {
        return req;
    }

    private CompletableFuture<Res> res() {
        return resFuture;
    }
}
