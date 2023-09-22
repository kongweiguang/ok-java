package cn.kpp.ok.ws;


import cn.kpp.ok.OK;
import okhttp3.OkHttpClient;
import okhttp3.WebSocketListener;

public class WS extends OK {

    private WebSocketListener listener;

    private WS(final OkHttpClient c) {
        super(c);
    }

    public static WS of() {
        return new WS(OK.default_c);
    }

    public static WS of(OkHttpClient c) {
        return new WS(c);
    }


    public WS listener(WebSocketListener listener) {
        this.listener = listener;
        return this;
    }

    public void ws() {
        if (this.listener == null) {
            return;
        }

        super.bf();
        super.client().newWebSocket(super.builder().build(), this.listener);
    }

}
