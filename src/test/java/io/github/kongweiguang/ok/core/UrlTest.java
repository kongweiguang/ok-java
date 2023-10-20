package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

public class UrlTest {
    @Test
    void test1() {
        final Res res = Req.of()
                .get()
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("get")
                .path("one")
                .query("name", "kpp")
                .query("name", "kpp2")
                .query("name1", "kpp1")
                .ok();
        System.out.println(res.str());

    }
}
