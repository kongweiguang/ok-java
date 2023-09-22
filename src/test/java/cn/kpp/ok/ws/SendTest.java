package cn.kpp.ok.ws;

import okhttp3.WebSocket;
import okio.ByteString;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class SendTest {
    @Test
    void test() {
        WS.of()
                .listener(new OkListener() {
                    @Override
                    public void onMessage(final WebSocket webSocket, final ByteString bytes) {
                        System.out.println(bytes.string(StandardCharsets.UTF_8));
                    }
                })
                .ws();
    }

}
