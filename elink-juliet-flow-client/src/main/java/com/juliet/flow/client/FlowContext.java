package com.juliet.flow.client;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-07-25
 */
public class FlowContext {

    private static TransmittableThreadLocal<Map<String, Object>> LOCAL_CACHE = new TransmittableThreadLocal<>();
    public static void putAttachment(String key, Object value) {
        Map<String, Object> local = LOCAL_CACHE.get();
        if (local == null) {
            local = new HashMap<>();
        }
        local.put(key, value);
        LOCAL_CACHE.set(local);
    }

    public static Object getAttachment(String key) {
        Map<String, Object> local = LOCAL_CACHE.get();
        if (local != null) {
            return local.get(key);
        }
        return null;
    }

    public static Map<String, Object> getAttachmentMap() {
        return LOCAL_CACHE.get();
    }

    public static void clean() {
        LOCAL_CACHE.set(new HashMap<>());
    }
}
