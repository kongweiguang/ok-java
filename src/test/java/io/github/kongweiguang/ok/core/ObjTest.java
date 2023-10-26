package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import org.junit.jupiter.api.Test;

public class ObjTest {

  @Test
  void test1() throws Exception {

    //自定义请求创建
    Req.of();

    //基本的http请求
    Req.get("http://localhost:8080/get");
    Req.post("http://localhost:8080/post");
    Req.delete("http://localhost:8080/delete");
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
