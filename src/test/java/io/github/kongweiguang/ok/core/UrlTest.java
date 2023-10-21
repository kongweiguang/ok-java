package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

public class UrlTest {

  @Test
  void test1() {
    final Res res = Req.get()
        .scheme(Const.http)
        .host(Const.localhost)
        .port(Const.port)
        .path("get")
        .path("one")
        .path("two")
        .query("name", "kpp")
        .query("name", "kpp2")
        .query("name1", "kpp1")
        .ok();
    // http://localhost:8080/get/one/two?name=kpp&name=kpp2&name1=kpp1

  }
}
