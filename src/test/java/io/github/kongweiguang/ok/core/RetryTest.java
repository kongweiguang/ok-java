package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class RetryTest {

  @Test
  void testRetry() {
    final Res res = Req.of()
        .get()
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
}
