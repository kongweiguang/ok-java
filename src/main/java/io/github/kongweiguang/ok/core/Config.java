package io.github.kongweiguang.ok.core;

import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class Config {
    private static final OkHttpClient default_c = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(TimeoutInterceptor.of)
            .build();


    private static List<Interceptor> interceptors;
    private static ExecutorService executorService;
    private static ConnectionPool connectionPool;
    private static Proxy proxy;
    private static Authenticator proxyAuthenticator;

    public static OkHttpClient client() {
        final OkHttpClient.Builder builder = default_c.newBuilder();


        if (nonNull(interceptors)) {
            for (Interceptor interceptor : interceptors) {
                builder.addInterceptor(interceptor);
            }
        }

        if (nonNull(executorService)) {
            builder.dispatcher(new Dispatcher(executorService));
        }

        if (nonNull(connectionPool)) {
            builder.connectionPool(connectionPool);
        }

        if (nonNull(proxy)) {
            builder.proxy(proxy);

            if (nonNull(proxyAuthenticator)) {
                builder.proxyAuthenticator(proxyAuthenticator);
            }
        }

        return builder.build();
    }


    public static void executor(final ExecutorService executor) {
        executorService = executor;
    }

    public static void addInterceptor(final Interceptor interceptor) {
        if (isNull(interceptors)) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
    }


    public static void connectionPool(final ConnectionPool pool) {
        connectionPool = pool;
    }

    public static void proxy(final String host, final int port) {
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    public static void proxyAuthenticator(final String username, final String password) {
        proxyAuthenticator = (route, response) -> response.request()
                .newBuilder()
                .header(Header.proxy_authorization.v(), Credentials.basic(username, password))
                .build();
    }

}
