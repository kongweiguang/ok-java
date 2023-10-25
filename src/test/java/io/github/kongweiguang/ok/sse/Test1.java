package io.github.kongweiguang.ok.sse;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public class Test1 {


  @Test
  void test() throws InterruptedException {

    Req.sse()
        .url("localhost:8080/sse")
        .sseListener(new SSEListener() {
          @Override
          public void event(Req req, SseEvent msg) {
            System.out.println("sse -> " + msg.id());
            System.out.println("sse -> " + msg.type());
            System.out.println("sse -> " + msg.data());
            if (Objects.equals(msg.data(), "done")) {
              close();
            }
          }

          @Override
          public void open(final Req req, final Res res) {
            super.open(req, res);
          }

          @Override
          public void fail(final Req req, final Res res, final Throwable t) {
            super.fail(req, res, t);
          }

          @Override
          public void closed(final Req req) {
            super.closed(req);
          }
        })
        .ok();

    Thread.sleep(10000);
  }
}
