package io.github.kongweiguang.ok.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

/**
 * 配置中心
 */
public final class Config {

  //默认的客户端
  private static final OkHttpClient default_c = new OkHttpClient.Builder()
      .connectTimeout(60, TimeUnit.SECONDS)
      .writeTimeout(60, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .addInterceptor(TimeoutInterceptor.of)
      .build();

  //拦截器
  private static List<Interceptor> interceptors;

  //异步调用的线程池
  private static Executor exec;

  //连接池配置
  private static ConnectionPool connectionPool;

  //代理配置
  private static Proxy proxy;
  private static Authenticator proxyAuthenticator;

  //ssl配置
  private static boolean ssl;


  public static OkHttpClient client() {
    final OkHttpClient.Builder builder = Config.default_c.newBuilder();

    if (nonNull(Config.interceptors)) {
      for (Interceptor interceptor : Config.interceptors) {
        builder.addInterceptor(interceptor);
      }
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

    return builder.build();
  }

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


  public static void ssl(boolean ssl) {
    Config.ssl = ssl;
  }

  private static TrustManager[] buildTrustManagers() {
    return new TrustManager[]{
        new X509TrustManager() {
          @Override
          public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
              String authType) {
          }

          @Override
          public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
              String authType) {
          }

          @Override
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
          }
        }
    };
  }

  public static void exec(final Executor executor) {
    Config.exec = executor;
  }

  public static Executor exec() {
    if (isNull(Config.exec)) {
      synchronized (Config.class) {
        Config.exec = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            r -> new Thread(r, "ok-thread"));
      }
    }
    return Config.exec;
  }

  public static void addInterceptor(final Interceptor interceptor) {
    if (isNull(Config.interceptors)) {
      Config.interceptors = new ArrayList<>();
    }
    Config.interceptors.add(interceptor);
  }


  public static void connectionPool(final ConnectionPool pool) {
    Config.connectionPool = pool;
  }

  public static void proxy(final String host, final int port) {
    Config.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
  }

  public static void proxy(final Proxy.Type type, final String host, final int port) {
    Config.proxy = new Proxy(type, new InetSocketAddress(host, port));
  }

  public static void proxyAuthenticator(final String username, final String password) {
    Config.proxyAuthenticator = (route, response) -> response.request()
        .newBuilder()
        .header(Header.proxy_authorization.v(),
            Credentials.basic(username, password))
        .build();
  }

}
