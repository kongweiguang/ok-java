package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

import java.net.Proxy.Type;

public class ProxyTest {

    @Test
    void test1() throws Exception {

        Config.proxy("127.0.0.1", 80);
        Config.proxy(Type.SOCKS, "127.0.0.1", 80);
        Config.proxyAuthenticator("k", "pass");

        final Res res = Req.get("http://localhost:8080/get/one/two")
                .query("a", "1")
                .ok();
    }

}
