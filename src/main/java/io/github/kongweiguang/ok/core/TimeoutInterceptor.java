package io.github.kongweiguang.ok.core;

import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 参考solon-cloud-httputils中的拦截器
 *
 * @author kongweiguang
 */
public final class TimeoutInterceptor implements Interceptor {
    public static final TimeoutInterceptor of = new TimeoutInterceptor();

    private TimeoutInterceptor() {
    }


    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {

        final Timeout timeout = chain.request().tag(Timeout.class);


        if (timeout != null) {
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
