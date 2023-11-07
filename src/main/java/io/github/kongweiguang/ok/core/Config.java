package io.github.kongweiguang.ok.core;

import okhttp3.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.github.kongweiguang.ok.core.Util.isTure;
import static io.github.kongweiguang.ok.core.Util.notNull;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * 配置中心
 *
 * @author kongweiguang
 */
public final class Config {

    //拦截器
    static List<Interceptor> interceptors;

    //分发器
    static Dispatcher dispatcher;

    //异步调用的线程池
    static Executor exec;

    //连接池配置
    static ConnectionPool connectionPool;

    //代理配置
    static Proxy proxy;
    static Authenticator proxyAuthenticator;

    //ssl配置
    static boolean ssl;


    /**
     * 设置ssl链接
     *
     * @param ssl 是否开启
     */
    public static void ssl(boolean ssl) {
        Config.ssl = ssl;
    }


    /**
     * 设置异步执行的线程池
     *
     * @param executor 使用的线程池
     */
    public static void exec(final Executor executor) {
        Config.exec = executor;
    }

    /**
     * 获取异步执行的线程池
     *
     * @return 线程池
     */
    public static Executor exec() {
        if (isNull(Config.exec)) {
            synchronized (Config.class) {
                if (isNull(Config.exec)) {
                    Config.exec = new ThreadPoolExecutor(0,
                            Integer.MAX_VALUE,
                            60,
                            TimeUnit.SECONDS,
                            new SynchronousQueue<>(),
                            r -> new Thread(r, "ok-thread"));
                }
            }
        }

        return Config.exec;
    }

    /**
     * 添加请求拦截器
     *
     * @param interceptor 拦截器
     */
    public static void addInterceptor(final Interceptor interceptor) {
        if (nonNull(interceptor)) {

            if (isNull(Config.interceptors)) {
                Config.interceptors = new ArrayList<>();
            }

            Config.interceptors.add(interceptor);
        }
    }

    public static void dispatcher(final Dispatcher dispatcher) {
        notNull(dispatcher, "dispatcher must not be null");

        Config.dispatcher = dispatcher;
    }

    /**
     * 设置链接池
     *
     * @param pool 链接池
     */
    public static void connectionPool(final ConnectionPool pool) {
        Config.connectionPool = pool;
    }

    /**
     * 设置http链接代理
     *
     * @param host 主机
     * @param port 端口
     */
    public static void proxy(final String host, final int port) {
        proxy(Type.HTTP, host, port);
    }

    /**
     * 设置http链接代理
     *
     * @param type 代理类型
     * @param host 主机
     * @param port 端口
     */
    public static void proxy(final Proxy.Type type, final String host, final int port) {
        notNull(type, "type must not be null");
        notNull(host, "host must not be null");
        isTure(port > 0, "port must > 0");

        Config.proxy = new Proxy(type, new InetSocketAddress(host, port));
    }

    /**
     * 设置代理的授权认证
     *
     * @param username 账号
     * @param password 密码
     */
    public static void proxyAuthenticator(final String username, final String password) {
        notNull(username, "username must not be null");
        notNull(password, "password must not be null");

        Config.proxyAuthenticator = (route, response) -> response.request()
                .newBuilder()
                .header(Header.proxy_authorization.v(),
                        Credentials.basic(username, password))
                .build();
    }

}
