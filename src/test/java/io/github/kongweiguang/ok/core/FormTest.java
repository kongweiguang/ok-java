package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class FormTest {

  @Test
  void testForm() {
    final Res ok = Req.of()
        .post()
        .url("http://localhost:80/post_form")
        .form("a", "1")
        .form(new HashMap<String, String>() {{
          put("b", "2");
        }})
        .ok();
  }
}
