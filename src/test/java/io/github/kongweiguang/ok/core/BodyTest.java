package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

public class BodyTest {

  @Test
  void test1() throws Exception {
    final User kkk = new User().setAge(12).setHobby(new String[]{"a", "b", "c"}).setName("kkk");
    final Res res = Req.post()
        .url("http://localhost:8080/post_body")
        //        .body(JSON.toJSONString(kkk))
        //        .body("{}")
        //自动会将对象转成json对象，使用fastjson2
        .body(kkk)
        //        .body("text", ContentType.text_plain)
        .ok();
    System.out.println("res.str() = " + res.str());

  }

}
