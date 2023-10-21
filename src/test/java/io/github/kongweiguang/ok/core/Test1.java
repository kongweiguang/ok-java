package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class Test1 {

  @Test
  void test() throws Exception {
    final Req req = Req.get();

    req.url("www.baidu.com/get")
        .retry(2, Duration.ofSeconds(1), (r, t) -> true)
        .success(System.out::println)
        .fail(System.out::println);

    System.out.println(req);
    final Res resf = req.ok();
    System.out.println("r:" + resf);


  }
}
