package io.github.kongweiguang.ok.sse;

import io.github.kongweiguang.ok.Req;
import org.junit.jupiter.api.Test;

public class Test1 {


  @Test
  void test() throws InterruptedException {

    Req.of()
        .url("localhost:8080/sse")
        .sse()
        .sseListener(new SSEListener() {
          @Override
          public void event(Req req, SseEvent msg) {
            System.out.println("sse -> " + msg.id());
            System.out.println("sse -> " + msg.type());
            System.out.println("sse -> " + msg.data());
          }
        })
        .ok();

    Thread.sleep(10000);
  }
}
