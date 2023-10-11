package io.github.kongweiguang.ok.core;

import static java.util.Objects.isNull;

public class Util {
    public static String replacePath(String path) {
        if (isNull(path)) {
            return "";
        }

        if (path.startsWith("/")) {
            path = path.replaceFirst("/", "");
        }
        return path;
    }

    public static String urlRegex(String url) {

        if (!url.startsWith(Const._http) || !url.startsWith(Const._https)) {
            if (url.startsWith("/")) {
                url = Const._http + Const.localhost + url;
            } else {
                url = Const._http + url;
            }
        }

        return url;
    }

}
