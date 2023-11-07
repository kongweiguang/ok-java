package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.ReqBuilder;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class Test1 {

    @Test
    void test() throws Exception {
        final ReqBuilder req = Req.get("www.baidu.com/get");

        req.retry(2, Duration.ofSeconds(1), (r, t) -> true)
                .success(System.out::println)
                .fail(System.out::println);

        System.out.println(req);
        final Res resf = req.ok();
        System.out.println("r:" + resf);


    }

}
