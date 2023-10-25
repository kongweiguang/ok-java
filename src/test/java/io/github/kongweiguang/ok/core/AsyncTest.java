package io.github.kongweiguang.ok.core;


import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class AsyncTest {

  @Test
  void test1() throws Exception {
    final CompletableFuture<Res> future = Req.get()
        .url("http://localhost:8080/get")
        .query("a", "1")
        .success(r -> System.out.println(r.str()))
        .fail(System.out::println)
        .okAsync();

    System.out.println("res = " + future.get(3, TimeUnit.SECONDS));
  }

}
