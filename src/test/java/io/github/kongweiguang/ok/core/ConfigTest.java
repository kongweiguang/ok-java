package io.github.kongweiguang.ok.core;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Proxy.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConfigTest {

    @Test
    void test1() throws Exception {
        //设置代理
        Config.proxy("127.0.0.1", 80);
        Config.proxy(Type.SOCKS, "127.0.0.1", 80);
        Config.proxyAuthenticator("k", "pass");

        //设置拦截器
        Config.addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull final Chain chain) throws IOException {
                System.out.println(1);
                return chain.proceed(chain.request());
            }
        });

        //设置连接池
        Config.connectionPool(new ConnectionPool(10, 10, TimeUnit.MINUTES));

        //设置异步调用的线程池
        Config.exec(Executors.newCachedThreadPool());
    }

}
