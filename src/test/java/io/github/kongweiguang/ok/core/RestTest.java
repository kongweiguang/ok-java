package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.OK;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class RestTest {
    @Test
    void testGet() {
        final Res res = OK.of()
                .get()
                .url("http://localhost:80/get_string")
                .query("a", "1")
                .query("b", "2")
                .query("c", "3")
                .ok();
        System.out.println("res = " + res.str());
    }

    @Test
    void testGetJson() {
        final Res res = OK.of()
                .get()
                .url("http://localhost:80/get_json")
                .query("a", "1")
                .query("b", "2")
                .query("c", "3")
                .ok();
        System.out.println("res = " + res.jsonObj());
    }

    @Test
    void testPost() {
        final Res res = OK.of()
                .post()
                .url("http://localhost:80/post_json")
                .json(new HashMap<String, Object>() {{
                    put("a", "1");
                    put("b", "2");
                    put("c", "3");
                }})
                .ok();
        System.out.println("res = " + res.str());
    }


    @Test
    void testList() {
        final Res res = OK.of()
                .get()
                .url("http://localhost:80/get_list")
                .ok();
        System.out.println("res = " + res.list());
    }



}
