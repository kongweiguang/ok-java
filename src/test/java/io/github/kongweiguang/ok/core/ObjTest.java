package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import org.junit.jupiter.api.Test;

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
