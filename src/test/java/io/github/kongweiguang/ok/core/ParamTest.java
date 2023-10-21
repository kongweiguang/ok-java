package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class ParamTest {

  @Test
  void test1() throws Exception {
    final Res res = Req.post()
        //query
        .query("a", "1")
        .query("b", "2")
        .query("c", "3")
        .query("d", Arrays.asList("0", "9", "8"))
        .query(new HashMap<String, String>() {{
          put("e", "4");
          put("f", "5");
        }})
        .ok();
  }

}
