package io.github.kongweiguang.ok.core;

/**
 * User-Agent
 * @author kongweiguang
 */
public interface UA {

  String v();

  enum Win implements UA {
    chrome("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.94 Safari/537.36"),
    fireFox("Mozilla/5.0 (Windows NT 6.2; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0"),

    ;

    private final String v;

    Win(final String v) {
      this.v = v;
    }

    @Override
    public String v() {
      return v;
    }
  }


  enum Mac implements UA {
    chrome("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.93 Safari/537.36"),
    fireFox("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:21.0) Gecko/20100101 Firefox/21.0"),

    ;

    private final String v;

    Mac(final String v) {
      this.v = v;
    }

    @Override
    public String v() {
      return v;
    }
  }

  enum Ubuntu implements UA {
    chrome("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.11 (KHTML, like Gecko) Ubuntu/11.10 Chromium/27.0.1453.93 Chrome/27.0.1453.93 Safari/537.36"),
    fireFox("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:21.0) Gecko/20130331 Firefox/21.0"),

    ;

    private final String v;

    Ubuntu(final String v) {
      this.v = v;
    }

    @Override
    public String v() {
      return v;
    }
  }

  enum Android implements UA {
    chrome("Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19"),
    fireFox("Mozilla/5.0 (Android; Mobile; rv:14.0) Gecko/14.0 Firefox/14.0"),

    ;
    private final String v;

    Android(final String v) {
      this.v = v;
    }

    @Override
    public String v() {
      return v;
    }
  }


  enum IPhone implements UA {
   chrome("Mozilla/5.0 (iPhone; CPU iPhone OS 6_1_4 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) CriOS/27.0.1453.10 Mobile/10B350 Safari/8536.25"),

    ;
    private final String v;

    IPhone(final String v) {
      this.v = v;
    }

    @Override
    public String v() {
      return v;
    }
  }

  enum IPad implements UA {
    safari("Mozilla/5.0 (iPad; CPU OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3"),

    ;
    private final String v;

    IPad(final String v) {
      this.v = v;
    }

    @Override
    public String v() {
      return v;
    }
  }


}
