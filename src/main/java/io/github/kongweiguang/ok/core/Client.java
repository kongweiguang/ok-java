package io.github.kongweiguang.ok.core;

import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.time.Duration;
import java.util.function.Supplier;

import static java.util.Objects.nonNull;

/**
 * 请求客户端
 *
 * @author kongweiguang
 */
public final class Client {

    private static final Supplier<Dispatcher> disSup = () -> {
        final Dispatcher dis = new Dispatcher();
        dis.setMaxRequests(1 << 20);
        dis.setMaxRequestsPerHost(1 << 20);
        return dis;
    };

    //默认的客户端
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .dispatcher(disSup.get())
            .connectTimeout(Duration.ofMinutes(1))
            .writeTimeout(Duration.ofMinutes(1))
            .readTimeout(Duration.ofMinutes(1))
            .build();

    /**
     * 创建OkHttpClient
     *
     * @return OkHttpClient {@link OkHttpClient}
     */
    public static OkHttpClient of() {
        return of(null);
    }

    /**
     * 创建OkHttpClient
     *
     * @param timeout 超时时间
     * @return OkHttpClient {@link OkHttpClient}
     */
    public static OkHttpClient of(final Timeout timeout) {
        final OkHttpClient.Builder builder = client.newBuilder();

        if (nonNull(Config.interceptors)) {
            for (Interceptor interceptor : Config.interceptors) {
                builder.addInterceptor(interceptor);
            }
        }

        if (nonNull(Config.dispatcher)) {
            builder.dispatcher(Config.dispatcher);
        }

        if (nonNull(Config.connectionPool)) {
            builder.connectionPool(Config.connectionPool);
        }

        if (nonNull(Config.proxy)) {
            builder.proxy(Config.proxy);

            if (nonNull(Config.proxyAuthenticator)) {
                builder.proxyAuthenticator(Config.proxyAuthenticator);
            }
        }

        if (Config.ssl) {
            ssl(builder);
        }

        if (nonNull(timeout)) {
            builder.connectTimeout(timeout.connect())
                    .writeTimeout(timeout.write())
                    .readTimeout(timeout.read());
        }

        return builder.build();
    }

    /**
     * 构建ssl请求链接
     *
     * @param builder 构建类
     */
    private static void ssl(final Builder builder) {
        try {
            final TrustManager[] trustAllCerts = buildTrustManagers();

            final SSLContext sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
        } catch (Exception ignored) {

        }
    }


    /**
     * 构建TrustManager
     *
     * @return TrustManager[]
     */
    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }
}
