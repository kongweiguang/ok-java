package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

public class UrlTest {

  @Test
  void test1() throws Exception {
    final Res res = Req.get()
        .url("http://localhost:8080/get/one/two")
        .ok();
  }

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
