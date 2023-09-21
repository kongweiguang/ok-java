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


参考solon-httputils，hutool，forest按照自己的编码习惯封装的建议轻量http客户端工具



### 特点


* 轻量
* api友好


### 例子

- get请求
```java
final Res res = OK.of()
                .get()
                .url("http://localhost:8080/get")
                .query("a", "1")
                .query("b", "2")
                .query("c", "3")
                .ok();
System.out.println("res = " + res.str());
```
- post请求
```java
 final Res res = OK.of()
                .post()
                .url("http://localhost:8080/post")
                .query("a", "a")
                .query("b", "b")
                .query("c", "c")
                .jsonBody(new HashMap<String, Object>() {{
                    put("a", "1");
                    put("b", "2");
                    put("c", "3");
                }})
                .ok();
        System.out.println("res = " + res.headers());
```