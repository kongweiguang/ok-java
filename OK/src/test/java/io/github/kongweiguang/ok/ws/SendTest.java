package io.github.kongweiguang.ok.ws;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.WS;
import io.github.kongweiguang.ok.core.Res;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class SendTest {
    @Test
    void test() {

        final Res ok = WS.of()
                .ws()
                .url("ws://websocket/test")
                .listener(new WebSocketListener() {
                    @Override
                    public void onMessage(@NotNull final WebSocket webSocket, @NotNull final String text) {
                        System.out.println("text = " + text);
                    }
                })
                .ok();
    }

}
