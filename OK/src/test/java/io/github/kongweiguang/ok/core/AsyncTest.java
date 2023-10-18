package io.github.kongweiguang.ok.core;


import io.github.kongweiguang.ok.OK;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class AsyncTest {
    @Test
    void test() {
        final Res res = OK.of()
                .async()
                .get()
                .url("https://localhost:80/get")
                .query("a", "1")
                .success(r -> System.out.println(r.str()))
                .fail(System.out::println)
                .ok();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("res = " + res);
    }

}
