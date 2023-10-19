package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class Test1 {
    @Test
    void test() throws Exception {
        final Req req = Req.of();
        req
                .get()
                .async()
                .url("www.baidu.com1")
                ;
        System.out.println(req);
        final Res res = req.ok();
        System.out.println(res);
        final String str = res.str();
        final int status = res.status();
        System.out.println("status = " + status);
        System.out.println(str);
    }
}
