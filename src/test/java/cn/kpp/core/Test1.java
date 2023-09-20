package cn.kpp.core;

public class Test1 {

    public static void main(String[] args) throws Exception {
        final String send = OK.of()
                .url("http://www.baidu.com")
                .send();
        System.out.println("send = " + send);
    }
}
