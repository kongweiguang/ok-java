package cn.kpp.ok.ws;

import cn.kpp.ok.core.Res;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.util.function.Consumer;

public abstract class OkListener extends WebSocketListener {

    @Override
    public void onClosed(final WebSocket webSocket, final int code, final String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onClosing(final WebSocket webSocket, final int code, final String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onFailure(final WebSocket webSocket, final Throwable t, final Response response) {
        super.onFailure(webSocket, t, response);
    }

    @Override
    public void onMessage(final WebSocket webSocket, final String text) {
        super.onMessage(webSocket, text);
    }

    @Override
    public void onMessage(final WebSocket webSocket, final ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onOpen(final WebSocket webSocket, final Response response) {
        super.onOpen(webSocket, response);
    }

}
