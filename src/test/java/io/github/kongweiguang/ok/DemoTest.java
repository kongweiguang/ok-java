package io.github.kongweiguang.ok;

import okhttp3.HttpUrl;

public class DemoTest {

  public static void main(String[] args) {
    final Res ok = Req.get("http://www.baidu.com").header("k","v").header("q","wer").ok();
    System.out.println( ok);
    System.out.println(123);
//    final HttpUrl parse = HttpUrl.parse("http://www.baidu.com");
//    System.out.println("parse = " + parse.host());
  }

}
