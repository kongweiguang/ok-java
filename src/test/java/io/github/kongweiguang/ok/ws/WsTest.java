package io.github.kongweiguang.ok.ws;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

public class WsTest {

  @Test
  void test() throws InterruptedException {

    final Res ok = Req.ws()
        .url("ws://websocket/test")
        .query("k", "v")
        .wsListener(new WSListener() {
          @Override
          public void open(final Req req, final Res res) {
            super.open(req, res);
          }

          @Override
          public void msg(final Req req, final String text) {
            send("hello");
          }

          @Override
          public void msg(final Req req, final byte[] bytes) {
            super.msg(req, bytes);
          }

          @Override
          public void fail(final Req req, final Res res, final Throwable t) {
            super.fail(req, res, t);
          }

          @Override
          public void closing(final Req req, final int code, final String reason) {
            super.closing(req, code, reason);
          }

          @Override
          public void closed(final Req req, final int code, final String reason) {
            super.closed(req, code, reason);
          }
        })
        .ok();
    //res == null
    wait();
  }

}
