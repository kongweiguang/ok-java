package io.github.kongweiguang.ok.core;


/**
 * ContentType中涉及到的常量
 *
 * @author kongweiguang，hutool
 */
public enum ContentType {

    /**
     * 标准表单编码，当action为get时候，浏览器用x-www-form-urlencoded的编码方式把form数据转换成一个字串（name1=value1&amp;name2=value2…）
     */
    form_urlencoded("application/x-www-form-urlencoded"),
    /**
     * 文件上传编码，浏览器会把整个表单以控件为单位分割，并为每个部分加上Content-Disposition，并加上分割符(boundary)
     */
    multipart("multipart/form-data"),
    /**
     * Rest请求JSON编码
     */
    json("application/json"),
    /**
     * Rest请求XML编码
     */
    xml("application/xml"),
    /**
     * text/plain编码
     */
    text_plain("text/plain"),
    /**
     * Rest请求text/xml编码
     */
    text_xml("text/xml"),
    /**
     * text/html编码
     */
    text_html("text/html"),
    /**
     * application/octet-stream编码
     */
    octet_stream("application/octet-stream"),
    /**
     * text/event-stream编码
     */
    event_stream("text/event-stream");

    private final String v;

    ContentType(final String v) {
        this.v = v;
    }

    public String v() {
        return v;
    }

    @Override
    public String toString() {
        return v();
    }
}
