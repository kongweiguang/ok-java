package cn.kpp.core;

public class Test1 {

    public static void main(String[] args) throws Exception {
        extracted();
    }

    private static void extracted() throws Exception {
        final Res send = OK.of()
                .url("http://localhost:8000/hello")
                .ok();
        System.out.println("send = " + send.list());
    }
}
