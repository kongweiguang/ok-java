package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class TimeoutTest {

  @Test
  void test1() throws Exception {
    final Res res = Req.get("http://localhost:8080/timeout")
        .timeout(Duration.ofSeconds(3))
//        .timeout(10, 10, 10)
        .ok();
    System.out.println(res.str());
  }

}
