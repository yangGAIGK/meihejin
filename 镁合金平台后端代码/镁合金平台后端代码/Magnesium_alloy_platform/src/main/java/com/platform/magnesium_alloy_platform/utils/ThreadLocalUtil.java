package com.platform.magnesium_alloy_platform.utils;

/**
 * ThreadLocal 工具类
 * 这个类可以用于任何需要在多线程环境中维护线程特定数据的场景，
 * 例如，在线程池中为每个线程保存数据库连接、用户会话信息或其他上下文数据。
 */
public class ThreadLocalUtil {

    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();//提供ThreadLocal对象,

    public static <T> T get(){//根据键获取值
        return (T) THREAD_LOCAL.get();
    }

    public static void set(Object value){//存储键值对
        THREAD_LOCAL.set(value);
    }

    public static void remove(){//清除ThreadLocal 防止内存泄漏
        THREAD_LOCAL.remove();
    }
}
