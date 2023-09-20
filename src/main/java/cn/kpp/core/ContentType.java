package cn.kpp.core;


public enum ContentType {

    FORM_URLENCODED("application/x-www-form-urlencoded"),
    MULTIPART("multipart/form-data"),
    JSON("application/json"),
    XML("application/xml"),
    TEXT_PLAIN("text/plain"),
    TEXT_XML("text/xml"),
    TEXT_HTML("text/html"),
    OCTET_STREAM("application/octet-stream"),
    EVENT_STREAM("text/event-stream");

    private final String v;

    ContentType(final String v) {
        this.v = v;
    }

    public String v() {
        return v;
    }
}
