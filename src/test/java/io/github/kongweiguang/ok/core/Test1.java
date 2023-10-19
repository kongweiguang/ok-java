package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import org.junit.jupiter.api.Test;

public class Test1 {
    @Test
    void test() throws Exception {
        final Req req = Req.of();
        req
                .get()
                .url("www.baidu.com");
        System.out.println(req);
        final Res res = req.ok();
        System.out.println(res);
        final String str = res.str();
        System.out.println(str);
    }
}
