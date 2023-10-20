package io.github.kongweiguang.ok.ws;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

public class SendTest {
    @Test
    void test() {

        final Res ok = Req.of()
                .ws()
                .url("ws://websocket/test")
                .wsListener(new WSListener() {
                    @Override
                    public void msg(final Req req, final String text) {

                    }
                })
                .ok();
    }

}
