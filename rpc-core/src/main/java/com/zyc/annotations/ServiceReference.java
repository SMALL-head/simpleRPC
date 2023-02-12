package com.zyc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 注释在类/接口上的注解
@Retention(RetentionPolicy.RUNTIME) // 在.class文件中保留该注解，这样的话就可以进行反射操作了
public @interface ServiceReference {
    String value = "";
    String value();


}
