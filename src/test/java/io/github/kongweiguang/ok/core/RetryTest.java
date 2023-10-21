package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

public class RetryTest {

  @Test
  void testRetry() {
    final Res res = Req.get()
        .url("http://localhost:80/get_string")
        .query("a", "1")
        .retry(3)
        .ok();
  }

  @Test
  void testRetry2() {
    final Res res = Req.get()
        .url("http://localhost:80/get_string")
        .query("a", "1")
        .retry(3, Duration.ofSeconds(2), (r, t) -> {
          final String str = r.str();
          if (str.length() > 10) {
            return true;
          }
          return false;
        })
        .ok();
  }

  @Test
  void testRetry3() {
    //异步重试
    final CompletableFuture<Res> res = Req.get()
        .url("http://localhost:80/get_string")
        .query("a", "1")
        .retry(3)
        .okAsync();

    res.join();
  }
}
