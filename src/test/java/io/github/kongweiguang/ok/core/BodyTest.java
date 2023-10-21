package io.github.kongweiguang.ok.core;

import com.alibaba.fastjson2.JSON;
import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

public class BodyTest {

  @Test
  void test1() throws Exception {
    final Res res = Req.post()
        .url("http://localhost:8080/get_string")
        .body(JSON.toJSONString(new User()))
        .body("{}")
        //自动会将对象转成json对象，使用fastjson2
        .body(new User())
        .body(JSON.toJSONString(new User()),ContentType.text_plain)
        .ok();

  }

}
