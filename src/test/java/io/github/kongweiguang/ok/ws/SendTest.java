package io.github.kongweiguang.ok.ws;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.core.Res;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class SendTest {
    @Test
    void test() {

        final Res ok = Req.of()
                .ws()
                .url("ws://websocket/test")
                .listener(new WSListener() {
                    @Override
                    public void msg(final Req req, final String text) {

                    }
                })
                .ok();
    }

}
