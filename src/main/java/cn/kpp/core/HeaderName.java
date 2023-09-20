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

package cn.kpp.core;

/**
 *
 */
public enum HeaderName {
    AUTHORIZATION("Authorization"),

    CONTENT_TYPE("Content-Type"),

    USER_AGENT("User-Agent"),

    COOKIE("Cookie");


    private final String v;

    HeaderName(final String v) {
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
