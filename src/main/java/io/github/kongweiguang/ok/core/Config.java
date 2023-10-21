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
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

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

  public static OkHttpClient client() {
    final OkHttpClient.Builder builder = default_c.newBuilder();

    if (nonNull(interceptors)) {
      for (Interceptor interceptor : interceptors) {
        builder.addInterceptor(interceptor);
      }
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


  public static void exec(final Executor executor) {
    exec = executor;
  }

  public static Executor exec() {
    if (isNull(exec)) {
      synchronized (Config.class) {
        exec = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            r -> new Thread(r, "ok-thread"));
      }
    }
    return exec;
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
        .header(Header.proxy_authorization.v(),
            Credentials.basic(username, password))
        .build();
  }

}
