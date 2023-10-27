package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class DowTest {

  @Test
  void testDow() {
    final Res ok = Req.get("http://localhost:80/get_file").ok();

    try {
      ok.file("d:\\k.txt");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
