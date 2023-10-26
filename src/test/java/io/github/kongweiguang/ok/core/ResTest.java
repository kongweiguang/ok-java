package io.github.kongweiguang.ok.core;

import com.alibaba.fastjson2.JSONObject;
import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

public class ResTest {

  @Test
  void testRes() {
    final Res res = Req.get("http://localhost:80/get_string")
        .query("a", "1")
        .query("b", "2")
        .query("c", "3")
        .ok();

    //返回值
    final String str = res.str();
    final byte[] bytes = res.bytes();
    final User obj = res.obj(User.class);
    final List<User> obj1 = res.obj(new TypeRef<List<User>>() {
    }.type());
    final List<String> list = res.list();
    final Map<String, String> map = res.map();
    final JSONObject jsonObject = res.jsonObj();
    final InputStream stream = res.stream();
    final Integer i = res.rInt();
    final Boolean b = res.rBool();

    //响应头
    final String ok = res.header("ok");
    final Map<String, List<String>> headers = res.headers();

    //状态
    final int status = res.code();

    //原始响应
    final Response response = res.res();


  }

}
