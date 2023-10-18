package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.OK;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class RetryTest {
    @Test
    void testRetry() {
        final Res res = OK.of()
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
