/*
 * Copyright (c) 2023 looly(loolly@aliyun.com)
 * Hutool is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          https://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package io.github.kongweiguang.ok.core;

/**
 * Header中涉及到的常量
 *
 * @author kongweiguang，hutool
 */
public enum Header {

    //------------------------------------------------------------- 通用头域
    /**
     * 提供验证头，例如：
     * <pre>
     * Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l
     * </pre>
     */
    authorization("Authorization"),
    /**
     * 提供给代理服务器的用于身份验证的凭证，例如：
     * <pre>
     * Proxy-Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l
     * </pre>
     */
    proxy_authorization("Proxy-Authorization"),
    /**
     * 提供日期和时间标志,说明报文是什么时间创建的
     */
    date("Date"),
    /**
     * 允许客户端和服务器指定与请求/响应连接有关的选项
     */
    connection("Connection"),
    /**
     * 给出发送端使用的MIME版本
     */
    mime_version("MIME-Version"),
    /**
     * 如果报文采用了分块传输编码(chunked transfer encoding) 方式,就可以用这个首部列出位于报文拖挂(trailer)部分的首部集合
     */
    trailer("Trailer"),
    /**
     * 告知接收端为了保证报文的可靠传输,对报文采用了什么编码方式
     */
    transfer_encoding("Transfer-Encoding"),
    /**
     * 给出了发送端可能想要"升级"使用的新版本和协议
     */
    upgrade("Upgrade"),
    /**
     * 显示了报文经过的中间节点
     */
    via("Via"),
    /**
     * 指定请求和响应遵循的缓存机制
     */
    cache_control("Cache-Control"),
    /**
     * 用来包含实现特定的指令，最常用的是Pragma:no-cache。在HTTP/1.1协议中，它的含义和Cache- Control:no-cache相同
     */
    pragma("Pragma"),
    /**
     * 请求表示提交内容类型或返回返回内容的MIME类型
     */
    content_type("Content-Type"),

    //------------------------------------------------------------- 请求头域
    /**
     * 指定请求资源的Intenet主机和端口号，必须表示请求url的原始服务器或网关的位置。HTTP/1.1请求必须包含主机头域，否则系统会以400状态码返回
     */
    host("Host"),
    /**
     * 允许客户端指定请求uri的源资源地址，这可以允许服务器生成回退链表，可用来登陆、优化cache等。他也允许废除的或错误的连接由于维护的目的被
     * 追踪。如果请求的uri没有自己的uri地址，Referer不能被发送。如果指定的是部分uri地址，则此地址应该是一个相对地址
     */
    referer("Referer"),
    /**
     * 指定请求的域
     */
    origin("Origin"),
    /**
     * HTTP客户端运行的浏览器类型的详细信息。通过该头部信息，web服务器可以判断到当前HTTP请求的客户端浏览器类别
     */
    user_agent("User-Agent"),
    /**
     * 指定客户端能够接收的内容类型，内容类型中的先后次序表示客户端接收的先后次序
     */
    accept("Accept"),
    /**
     * 指定HTTP客户端浏览器用来展示返回信息所优先选择的语言
     */
    accept_language("Accept-Language"),
    /**
     * 指定客户端浏览器可以支持的web服务器返回内容压缩编码类型
     */
    accept_encoding("Accept-Encoding"),
    /**
     * 浏览器可以接受的字符编码集
     */
    accept_charset("Accept-Charset"),
    /**
     * HTTP请求发送时，会把保存在该请求域名下的所有cookie值一起发送给web服务器
     */
    cookie("Cookie"),
    /**
     * 请求的内容长度
     */
    content_length("Content-Length"),

    //------------------------------------------------------------- 响应头域
    /**
     * 提供WWW验证响应头
     */
    www_authenticate("WWW-Authenticate"),
    /**
     * Cookie
     */
    set_cookie("Set-Cookie"),
    /**
     * Content-Encoding
     */
    content_encoding("Content-Encoding"),
    /**
     * Content-Disposition
     */
    content_disposition("Content-Disposition"),
    /**
     * ETag
     */
    etag("ETag"),
    /**
     * 重定向指示到的URL
     */
    location("Location");;


    private final String v;

    Header(final String v) {
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
