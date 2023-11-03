package io.github.kongweiguang.ok.core;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

/**
 * 超时拦截器
 *
 * @author kongweiguang，solon
 */
public final class TimeoutInterceptor implements Interceptor {

  public static final TimeoutInterceptor of = new TimeoutInterceptor();

  private TimeoutInterceptor() {
  }


  @NotNull
  @Override
  public Response intercept(Chain chain) throws IOException {

    final Timeout timeout = chain.request().tag(Timeout.class);

    if (nonNull(timeout)) {
      if (timeout.connectTimeout > 0) {
        chain = chain.withConnectTimeout(timeout.connectTimeout, TimeUnit.SECONDS);
      }

      if (timeout.writeTimeout > 0) {
        chain = chain.withWriteTimeout(timeout.writeTimeout, TimeUnit.SECONDS);
      }

      if (timeout.readTimeout > 0) {
        chain = chain.withReadTimeout(timeout.readTimeout, TimeUnit.SECONDS);
      }
    }

    return chain.proceed(chain.request());
  }
}
