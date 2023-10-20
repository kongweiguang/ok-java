package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DowTest {
    @Test
    void testDow() {
        final Res ok = Req.of()
                .get()
                .url("http://localhost:80/get_file")
                .ok();

        try {
            ok.file("d:\\k.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testUp() throws IOException {
        final Res ok = Req.of()
                .multipart()
                .url("http://localhost:80/post_upload_file")
                .file("introduce", "introduce.txt", Files.readAllBytes(Paths.get("d:\\k.txt")))
                .form("a", "b")
                .ok();
        System.out.println(ok.status());
    }
}
