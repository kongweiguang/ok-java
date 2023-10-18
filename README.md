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


### 特点

* 非常轻量，代码简单，大小只有13k
* api友好，用的很爽
* 链式编程

### 使用方式

Maven

```xml

<dependency>
    <groupId>io.github.kongweiguang</groupId>
    <artifactId>OK</artifactId>
    <version>0.1</version>
</dependency>
```

Gradle

```xml
implementation 'io.github.kongweiguang:OK:0.1'
```

Gradle-Kotlin

```xml
implementation("io.github.kongweiguang:OK:0.1")
```

### 例子

- get请求

```java
final Res res=OK.of()
        .get()
        .url("http://localhost:80/get_string")
        .query("a","1")
        .query("b","2")
        .query("c","3")
        .ok();
```

- post请求

```java
 final Res res = OK.of()
        .post()
        .url("http://localhost:80/post_json")
        .query("b", "b")
        .body(new HashMap<String, Object>() {{
            put("a", "1");
            put("b", "2");
            put("c", "3");
        }})
        .ok();
```

- form表单请求

```java
final Res ok=OK.of()
        .post()
        .url("http://localhost:80/post_form")
        .form("a","1")
        .form(new HashMap<String, String>(){{
             put("b","2");
        }})
        .ok();
```

- 构建url

```java
final Res res=OK.of()
        .get()
        .scheme("http")
        .host("localhost")
        .port(8080)
        .path("get")
        .path("one")
        .query("name","kpp")
        .query("name","kpp2")
        .query("name1","kpp1")
        .ok();
```

- async异步请求

```java
final Res res=OK.of()
        .get()
        .async()
        .url("https://localhost:80/get")
        .query("a","1")
        .success(r->System.out.println(r.str()))
        .fail(System.out::println)
        .ok();
```

- retry重试请求

```java
final Res res=OK.of()
        .get()
        .url("http://localhost:80/get_string")
        .query("a","1")
        .retry(3)
        .ok();

//例子2

final Res res=OK.of()
        .get()
        .url("http://localhost:80/get_string")
        .query("a","1")
        .retry(3,Duration.ofSeconds(2),(r,t)->{
            final String str=r.str();
            if(str.length()>10){
                return true;
            }
            return false;
        })
        .ok();
```

- upload上传

```java
final Res ok=OK.of()
        .multipart()
        .url("http://localhost:80/post_upload_file")
        .file("introduce","introduce.txt",Files.readAllBytes(Paths.get("d:\\k.txt")))
        .form("a","b")
        .ok();
```

- dow下载

```java
final Res ok=OK.of()
        .get()
        .url("http://localhost:80/get_file")
        .ok();

        try{
            ok.file("d:\\k.txt");
        }catch(IOException e){
            throw new RuntimeException(e);
        }
```

- ws请求

```java
final Res res=OK.of()
        .ws()
        .url("ws://websocket/test")
        .listener(new WebSocketListener(){
            @Override
            public void onMessage(@NotNull final WebSocket webSocket,@NotNull final String text){
                    System.out.println("text = "+text);
            }
        })
        .ok();
```

res响应

```java

//返回值
final String str=res.str();
final byte[]bytes=res.bytes();
final User obj=res.obj(User.class);
final List<User> obj1=res.obj(new TypeRef<List<User>>(){}.type());
final List<String> list=res.list();
final Map<String, String> map=res.map();
final JSONObject jsonObject=res.jsonObj();
final InputStream stream=res.stream();
final Integer i=res.rInt();
final Boolean b=res.rBool();
res.file("d:\\k.txt");

//响应头
final String ok=res.header("ok");
final Map<String, List<String>>headers=res.headers();

//状态
final int status=res.status();

//原始响应
final Response response=res.res();

```