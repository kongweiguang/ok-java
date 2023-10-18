package io.github.kongweiguang.ok;

import io.github.kongweiguang.ok.core.Const;
import io.github.kongweiguang.ok.core.ContentType;
import io.github.kongweiguang.ok.core.Header;
import io.github.kongweiguang.ok.core.MultiValueMap;
import io.github.kongweiguang.ok.core.ReqBody;
import io.github.kongweiguang.ok.core.Res;
import io.github.kongweiguang.ok.core.Retry;
import io.github.kongweiguang.ok.core.TimeoutInterceptor;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;

public class OK {

    protected static final OkHttpClient default_c = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(TimeoutInterceptor.of)
            .build();


    protected final OkHttpClient C;
    protected final Request.Builder builder;
    private Req req;

    //sse


    public OK(final OkHttpClient c) {
        this.C = c;
        this.builder = new Request.Builder();
    }


    public static OK of() {
        return new OK(default_c);
    }


    /**
     * 发送请求
     *
     * @return Res {@code  Res}
     */
    public Res ok(Req req) {
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

    private void bf() {
        addQuery();
        addForm();
        addCookie();
        addMethod();
    }

    private Res ojbk() {
        builder().tag(Req.class, req());

        bf();

        if (reqType()) {
            if (req().isRetry()) {
                return Retry.predicate(this::http0, req().predicate())
                        .maxAttempts(req().max())
                        .delay(req().delay())
                        .execute()
                        .get()
                        .orElse(null);
            } else {
                return http0();
            }
        }

        return null;
    }

    private Res http0() {
        if (req().isAsync()) {
            client().newCall(builder().build()).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                    if (nonNull(req().fail())) {
                        req().fail().accept(e);
                    }

                    call.cancel();
                }

                @Override
                public void onResponse(@NotNull final Call call, @NotNull final Response res) throws IOException {
                    if (nonNull(req().success())) {
                        req().success().accept(Res.of(res));
                    }

                    call.cancel();
                }
            });

            return null;
        } else {

            try (Response execute = client().newCall(builder().build()).execute()) {
                return Res.of(execute);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void addQuery() {

        if (nonNull(req().url())) {
            req().scheme(req().url().getProtocol());
            req().host(req().url().getHost());
            req().port(req().url().getPort() == -1 ? Const.port : req().url().getPort());
            req().pathFirst(req().url().getPath());

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


    private void addForm() {
        if (req().isMultipart()) {
            req().contentType(ContentType.multipart);
            req().form().forEach(req().mul()::addFormDataPart);
        } else if (nonNull(form)) {
            req().contentType(ContentType.form_urlencoded);
            final FormBody.Builder b = new FormBody.Builder(charset());
            req().form().forEach(b::addEncoded);
            this.formBody = b.build();
        }
    }

    private void addCookie() {
        if (nonNull(cookie)) {
            builder().addHeader(Header.cookie.v(), cookie2Str(cookie()));
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
                rb = req().mul().setType(MediaType.parse(ContentType.multipart.v() + ";charset=" + req().charset().name())).build();
            } else if (nonNull(formBody())) {
                rb = formBody();
            } else {
                rb = new ReqBody(req().contentType(), req().charset(), req().reqBody().getBytes(req().charset()));
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
                        req().sseListener().client(client())
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
}
