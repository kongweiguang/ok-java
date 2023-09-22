package cn.kpp.ok.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 获取泛型的参数
 *
 * @author kongweiguang
 */
public class TypeRef<T> {
    private final Type type;

    public TypeRef() {
        this.type = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public Type type() {
        return type;
    }
}
