package io.github.kongweiguang.ok.sse;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public class SseTest {


  @Test
  void test() throws InterruptedException {

    Req.sse("localhost:8080/sse")
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

    wait();
  }

  public static void main(String[] args) {
    final Req req = Req.get().url("http://www.baidu.com").header("k", "v").header("k1", "v1");
    System.out.println(req);
    final Res res = req.ok();
    System.out.println(res);
    System.out.println(11111);
  }
}
