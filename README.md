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

* 非常轻量，代码简单，大小只有15k
* 链式编程，api友好，用的很爽
* 支持http，ws，sse调用
* 支持jdk8以上所有的项目

# 使用方式

Maven

```xml

<dependency>
  <groupId>io.github.kongweiguang</groupId>
  <artifactId>ok-java</artifactId>
  <version>0.3</version>
</dependency>
```

Gradle

```xml
implementation 'io.github.kongweiguang:ok-java:0.3'
```

Gradle-Kotlin

```xml
implementation("io.github.kongweiguang:ok-java:0.3")
```

# 简单介绍

## 请求对象

```java
public class ObjTest {

  @Test
  void test1() throws Exception {

    //自定义请求创建
    Req.of();

    //基本的http请求
    Req.get();
    Req.post();
    Req.delete();
    Req.patch();
    Req.head();
    Req.options();
    Req.trace();
    Req.connect();

    //特殊http请求
    //application/x-www-form-urlencoded
    Req.formUrlencoded();
    //multipart/form-data
    Req.multipart();

    //ws协议请求创建
    Req.ws();

    //sse协议请求创建
    Req.sse();

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
    final Res res = Req.get()
        .url("http://localhost:8080/get/one/two")
        .ok();
  }
}

```

- 使用构建方法

```java

public class UrlTest {

  @Test
  void test2() {
    final Res res = Req.get()
        .scheme("http")
        .host("localhost")
        .port(8080)
        .path("get")
        .path("one")
        .path("two")
        .ok();
    // http://localhost:8080/get/one/two
  }
}

```

- 混合使用

```java

public class UrlTest {

  @Test
  void test3() throws Exception {
    // http://localhost:8080/get/one/two
    final Res res = Req.get()
        .url("/get")
        .scheme("http")
        .host("localhost")
        .port(8080)
        .path("one")
        .path("two")
        .ok();
  }
}

```

## url参数

```java
public class UrlQueryTest {

  @Test
  void test1() throws Exception {
    //http://localhost:8080/get/one/two?k1=v1&k2=1&k2=2&k3=v3&k4=v4
    final Res res = Req.get()
        .url("http://localhost:8080/get/one/two")
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
    final Res res = Req.get()
        .url("http://localhost:8080/get_string")
        //contentType
        .contentType(ContentType.json)
        //charset
        .charset(StandardCharsets.UTF_8)
        //user-agent
        .ua("User-Agent")
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
  }

}

```

## 请求体

get和head请求就算添加了请求体也不会携带在请求

```java
public class BodyTest {

  @Test
  void test1() throws Exception {
    final Res res = Req.post()
        .url("http://localhost:8080/get_string")
        .body(JSON.toJSONString(new User()))
        .body("{}")
        //自动会将对象转成json对象，使用fastjson2
        .body(new User())
        .body(JSON.toJSONString(new User()), ContentType.text_plain)
        .ok();

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
    final Res ok = Req.formUrlencoded()
        .url("http://localhost:80/post_form")
        .form("a", "1")
        .form(new HashMap<String, String>() {{
          put("b", "2");
        }})
        .ok();
  }

  @Test
  void test2() throws Exception {
    //multipart/form-data
    final Res ok = Req.multipart()
        .url("http://localhost:80/post_form")
        .file("k", "k.txt", Files.readAllBytes(Paths.get("")))
        .form("a", "1")
        .form(new HashMap<String, String>() {{
          put("b", "2");
        }})
        .ok();
  }
}
```

## 异步请求

异步请求返回的是future，也可以使用join()或者get()方法等待请求执行完，具体使用请看CompletableFuture（异步编排，很np）

```java
public class AsyncTest {

  @Test
  void test1() {
    final CompletableFuture<Res> future = Req.get()
        .url("https://localhost:80/get")
        .query("a", "1")
        .success(r -> System.out.println(r.str()))
        .fail(System.out::println)
        .okAsync();

    System.out.println("res = " + future.join());
  }

}
```

## 请求超时时间设置

超时设置的时间单位是秒

```java
public class TimeoutTest {

  @Test
  void test1() throws Exception {
    final Res res = Req.get()
        .url("http://localhost:8080/get_string")
        .timeout(10)
        .timeout(10, 10, 10)
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
    final Res res = Req.get()
        .url("http://localhost:80/get_string")
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
    final int status = res.status();

    //原始响应
    final Response response = res.res();


  }

}
```

## 重试

重试可以实现同步重试和异步重试，重试的条件可自定义实现

```java

public class RetryTest {

  @Test
  void testRetry() {
    final Res res = Req.get()
        .url("http://localhost:80/get_string")
        .query("a", "1")
        .retry(3)
        .ok();
  }

  @Test
  void testRetry2() {
    final Res res = Req.get()
        .url("http://localhost:80/get_string")
        .query("a", "1")
        .retry(3, Duration.ofSeconds(2), (r, t) -> {
          final String str = r.str();
          if (str.length() > 10) {
            return true;
          }
          return false;
        })
        .ok();
  }

  @Test
  void testRetry3() {
    //异步重试
    final CompletableFuture<Res> res = Req.get()
        .url("http://localhost:80/get_string")
        .query("a", "1")
        .retry(3)
        .okAsync();

    res.join();
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

    final Res res = Req.get()
        .url("http://localhost:8080/get/one/two")
        .query("a", "1")
        .ok();
  }

}
```

## ws请求

ws请求返回的res对象为null

```java

public class SendTest {

  @Test
  void test() {

    final Res ok = Req.ws()
        .url("ws://websocket/test")
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
  }

}

```

## sse请求

sse请求返回的res对象为null

```java

public class Test1 {


  @Test
  void test() throws InterruptedException {

    Req.sse()
        .url("localhost:8080/sse")
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

    Thread.sleep(10000);
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