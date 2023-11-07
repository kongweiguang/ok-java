<h1 align="center" style="text-align:center;">
  OK
</h1>
<p align="center">
	<strong>基于okhttp封装的轻量级http客户端</strong>
</p>

<p align="center">
    <a target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0.txt">
		<img src="https://img.shields.io/:license-Apache2-blue.svg" alt="Apache 2" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8+-green.svg" alt="jdk-8+" />
	</a>
    <br />
</p>

<br/>

<hr />

# 特点

* 非常轻量，代码简单，大小只有17k
* 链式编程，api友好，用的很爽
* 支持http，ws，sse调用
* 支持jdk8

# 使用方式

Maven

```xml

<dependency>
    <groupId>io.github.kongweiguang</groupId>
    <artifactId>ok-java</artifactId>
    <version>0.7</version>
</dependency>
```

Gradle

```xml
implementation 'io.github.kongweiguang:ok-java:0.7'
```

Gradle-Kotlin

```xml
implementation("io.github.kongweiguang:ok-java:0.7")
```

# 简单介绍

## 请求对象

```java
public class ObjTest {

    @Test
    void test1() throws Exception {

        //自定义请求创建
        Req.of().method(Method.GET).url("http://localhost:8080/get");

        //基本的http请求
        Req.get("http://localhost:8080/get");
        Req.post("http://localhost:8080/post");
        Req.delete("http://localhost:8080/delete");
        Req.put("http://localhost:8080/put");
        Req.patch("http://localhost:8080/patch");
        Req.head("http://localhost:8080/head");
        Req.options("http://localhost:8080/options");
        Req.trace("http://localhost:8080/trace");
        Req.connect("http://localhost:8080/connect");

        //特殊http请求
        //application/x-www-form-urlencoded
        Req.formUrlencoded("http://localhost:8080/formUrlencoded");
        //multipart/form-data
        Req.multipart("http://localhost:8080/multipart");

        //ws协议请求创建
        Req.ws("http://localhost:8080/ws");

        //sse协议请求创建
        Req.sse("http://localhost:8080/sse");

    }

}
```

## url请求地址

url添加有两种方式，可以混合使用，如果url和构建函数里面都有值，按构建函数里面为主

- 直接使用url方法

```java

public class UrlTest {

    @Test
    void test1() throws Exception {
        final Res res = Req.get("http://localhost:8080/get/one/two")
                .ok();
        System.out.println("res = " + res.str());
    }


    // 使用构建方法
    @Test
    void test2() {
        final Res res = Req.of()
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("get")
                .path("one")
                .path("two")
                .ok();
        System.out.println("res.str() = " + res.str());
        // http://localhost:8080/get/one/two
    }

    //混合使用
    @Test
    void test3() throws Exception {
        // http://localhost:8080/get/one/two
        final Res res = Req.get("/get")
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("one")
                .path("two")
                .ok();
        System.out.println("res = " + res.str());
    }
}
```

## url参数

```java
public class UrlQueryTest {

    @Test
    void test1() throws Exception {
        //http://localhost:8080/get/one/two?q=1&k1=v1&k2=1&k2=2&k3=v3&k4=v4
        final Res res = Req.get("http://localhost:8080/get/one/two?q=1")
                .query("k1", "v1")
                .query("k2", Arrays.asList("1", "2"))
                .query(new HashMap<String, String>() {{
                    put("k3", "v3");
                    put("k4", "v4");
                }})
                .ok();
    }

}
```

## 请求头

设置请求头内容，cookie等

```java

public class HeaderTest {

    @Test
    void test1() throws Exception {
        final Res res = Req.get("http://localhost:8080/header")
                //contentype
                .contentType(ContentType.json)
                //charset
                .charset(StandardCharsets.UTF_8)
                //user-agent
                .ua(Mac.chrome.v())
                //authorization
                .auth("auth qwe")
                //authorization bearer
                .bearer("qqq")
                //header
                .header("name", "value")
                //headers
                .headers(new HashMap<String, String>() {{
                    put("name1", "value1");
                    put("name2", "value2");
                }})
                //cookie
                .cookie("k", "v")
                //cookies
                .cookies(new HashMap<String, String>() {{
                    put("k1", "v1");
                    put("k2", "v2");
                }})
                .ok();
        System.out.println("res.str() = " + res.str());
    }

}
```

## 请求体

get和head请求就算添加了请求体也不会携带在请求

```java
public class BodyTest {

    @Test
    void test1() throws Exception {
        final User kkk = new User().setAge(12).setHobby(new String[]{"a", "b", "c"}).setName("kkk");
        final Res res = Req.post("http://localhost:8080/post_body")
                //        .body(JSON.toJSONString(kkk))
                //        .body("{}")
                //自动会将对象转成json对象，使用fastjson2
                .json(kkk)
                //        .body("text", ContentType.text_plain)
                .ok();
        System.out.println("res.str() = " + res.str());
    }

}
```

### form表单请求

可发送application/x-www-form-urlencoded表单请求，如果需要上传文件则使用multipart/form-data

```java

public class FormTest {

    @Test
    void testForm() throws IOException {
        //application/x-www-form-urlencoded
        final Res ok = Req.formUrlencoded("http://localhost:8080/post_form")
                .form("a", "1")
                .form(new HashMap<String, String>() {{
                    put("b", "2");
                }})
                .ok();
        System.out.println("ok.str() = " + ok.str());
    }

    @Test
    void test2() throws Exception {
        //multipart/form-data
        final Res ok = Req.multipart("http://localhost:8080/post_mul_form")
                .file("k", "k.txt", Files.readAllBytes(Paths.get("D:\\k\\k.txt")))
                .form("a", "1")
                .form(new HashMap<String, String>() {{
                    put("b", "2");
                }})
                .ok();
        System.out.println("ok.str() = " + ok.str());
    }
}
```

## 异步请求

异步请求返回的是future，也可以使用join()或者get()方法等待请求执行完，具体使用请看CompletableFuture（异步编排）

```java
public class AsyncTest {

    @Test
    void test1() throws Exception {
        final CompletableFuture<Res> future = Req.get("http://localhost:8080/get")
                .query("a", "1")
                .success(r -> System.out.println(r.str()))
                .fail(System.out::println)
                .okAsync();

        System.out.println("res = " + future.get(3, TimeUnit.SECONDS));
    }

}
```

## 请求超时时间设置

超时设置的时间单位是**秒**

```java

public class TimeoutTest {

    @Test
    void test1() throws Exception {
        final Res res = Req.get("http://localhost:8080/timeout")
                .timeout(3)
//        .timeout(10, 10, 10)
                .ok();
        System.out.println(res.str());
    }

}
```

## 响应对象

```java

public class ResTest {

    @Test
    void testRes() {
        final Res res = Req.get("http://localhost:80/get_string")
                .query("a", "1")
                .query("b", "2")
                .query("c", "3")
                .ok();

        //返回值
        final String str = res.str();
        final byte[] bytes = res.bytes();
        final User obj = res.obj(User.class);
        final List<User> obj1 = res.obj(new TypeRef<List<User>>() {
        }.type());
        final List<String> list = res.list();
        final Map<String, String> map = res.map();
        final JSONObject jsonObject = res.jsonObj();
        final InputStream stream = res.stream();
        final Integer i = res.rInt();
        final Boolean b = res.rBool();

        //响应头
        final String ok = res.header("ok");
        final Map<String, List<String>> headers = res.headers();

        //状态
        final int status = res.code();

        //原始响应
        final Response response = res.raw();


    }

}
```

## 重试

重试可以实现同步重试和异步重试，重试的条件可自定义实现

```java

public class RetryTest {

    @Test
    void testRetry() {
        final Res res = Req.get("http://localhost:8080/error")
                .query("a", "1")
                .retry(3)
                .ok();
        System.out.println("res = " + res.str());
    }

    @Test
    void testRetry2() {
        final Res res = Req.get("http://localhost:8080/error")
                .query("a", "1")
                .retry(3, Duration.ofSeconds(2), (r, t) -> {
                    final String str = r.str();
                    if (str.length() > 10) {
                        return true;
                    }
                    return false;
                })
                .ok();
        System.out.println("res.str() = " + res.str());
    }

    @Test
    void testRetry3() {
        //异步重试
        final CompletableFuture<Res> res = Req.get("http://localhost:8080/error")
                .query("a", "1")
                .retry(3)
                .okAsync();
        System.out.println(1);
        System.out.println("res.join().str() = " + res.join().str());
    }
}
```

## 请求代理

代理默认是http，可以设置socket代理

```java

public class ProxyTest {

    @Test
    void test1() throws Exception {

        Config.proxy("127.0.0.1", 80);
        Config.proxy(Type.SOCKS, "127.0.0.1", 80);
        Config.proxyAuthenticator("k", "pass");

        final Res res = Req.get("http://localhost:8080/get/one/two")
                .query("a", "1")
                .ok();
    }

}
```

## 下载

```java
public class DowTest {

    @Test
    void testDow() {
        final Res ok = Req.get("http://localhost:80/get_file").ok();

        try {
            ok.file("d:\\k.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

## ws请求

ws请求返回的res对象为null

```java

public class WsTest {

    @Test
    void test() {

        final Res ok = Req.ws("ws://websocket/test")
                .query("k", "v")
                .wsListener(new WSListener() {
                    @Override
                    public void open(final Req req, final Res res) {
                        super.open(req, res);
                    }

                    @Override
                    public void msg(final Req req, final String text) {
                        send("hello");
                    }

                    @Override
                    public void msg(final Req req, final byte[] bytes) {
                        super.msg(req, bytes);
                    }

                    @Override
                    public void fail(final Req req, final Res res, final Throwable t) {
                        super.fail(req, res, t);
                    }

                    @Override
                    public void closing(final Req req, final int code, final String reason) {
                        super.closing(req, code, reason);
                    }

                    @Override
                    public void closed(final Req req, final int code, final String reason) {
                        super.closed(req, code, reason);
                    }
                })
                .ok();
        //res == null
        Util.sync(this);
    }

}
```

## sse请求

sse请求返回的res对象为null

```java

public class SseTest {


    @Test
    void test() throws InterruptedException {

        Req.sse("localhost:8080/sse")
                .sseListener(new SSEListener() {
                    @Override
                    public void event(Req req, SseEvent msg) {
                        System.out.println("sse -> " + msg.id());
                        System.out.println("sse -> " + msg.type());
                        System.out.println("sse -> " + msg.data());
                        if (Objects.equals(msg.data(), "done")) {
                            close();
                        }
                    }

                    @Override
                    public void open(final Req req, final Res res) {
                        super.open(req, res);
                    }

                    @Override
                    public void fail(final Req req, final Res res, final Throwable t) {
                        super.fail(req, res, t);
                    }

                    @Override
                    public void closed(final Req req) {
                        super.closed(req);
                    }
                })
                .ok();

        Util.sync(this);
    }

}

```

## 全局配置设置

```java

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
```