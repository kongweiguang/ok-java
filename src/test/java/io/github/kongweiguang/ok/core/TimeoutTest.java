package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

public class TimeoutTest {

  @Test
  void test1() throws Exception {
    final Res res = Req.get()
        .url("http://localhost:8080/get_string")
        .timeout(1000)
        .timeout(10, 10, 10)
        .ok();
    System.out.println(res.str());
  }

}
