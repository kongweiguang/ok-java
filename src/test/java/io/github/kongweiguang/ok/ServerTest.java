package io.github.kongweiguang.ok;

import io.github.kongweiguang.khttp.KHTTP;
import io.github.kongweiguang.khttp.core.Handler;
import io.github.kongweiguang.khttp.core.MultiValueMap;
import io.github.kongweiguang.khttp.core.Req;
import io.github.kongweiguang.khttp.core.Res;

import java.io.IOException;

public class ServerTest {
    public static void main(String[] args) {
        KHTTP.of()
                .post("/post_query", new Handler() {
                    @Override
                    public void doHandler(final Req req, final Res res) throws IOException {
                        final MultiValueMap<String, String> params = req.params();
                        System.out.println("params = " + params);
                        res.send("ok");
                    }
                })
                .post("/post_query/a/b/c", new Handler() {
                    @Override
                    public void doHandler(final Req req, final Res res) throws IOException {
                        final MultiValueMap<String, String> params = req.params();
                        System.out.println("params = " + params);
                        res.send("ok");
                    }
                })
                .ok(8080);
    }
}
