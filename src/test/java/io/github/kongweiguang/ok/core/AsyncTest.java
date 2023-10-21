package io.github.kongweiguang.ok.core;


import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

public class AsyncTest {

  @Test
  void test1() {
    final CompletableFuture<Res> future = Req.get()
        .url("https://localhost:80/get")
        .query("a", "1")
        .success(r -> System.out.println(r.str()))
        .fail(System.out::println)
        .okAsync();

    System.out.println("res = " + future.join());
  }

}
