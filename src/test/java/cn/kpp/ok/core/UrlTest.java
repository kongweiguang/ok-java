package cn.kpp.ok.core;

import cn.kpp.ok.OK;
import org.junit.jupiter.api.Test;

public class UrlTest {
    @Test
    void test1() {
        final Res res = OK.of()
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
