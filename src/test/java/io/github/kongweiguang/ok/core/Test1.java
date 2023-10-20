package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

public class Test1 {
    @Test
    void test() throws Exception {
        final Req req = Req.of();
        req
                .get()
                .url("www.baidu.com")
                ;
        System.out.println(req);
        final CompletableFuture<Res> res = req.okAsync();
        System.out.println(res);
        final String str = res.join().str();
        final int status = res.join().status();
        System.out.println("status = " + status);
        System.out.println(str);
    }
}
