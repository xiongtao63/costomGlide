package com.xiongtao.myglide.cache;

import com.xiongtao.myglide.resource.Value;


/**
 * 内存缓存中，LRU移除 回调的接口
 */
public interface MemoryCacheCallback {
    /**
     * 内存缓存中移除的 key--value
     * @param key
     * @param oldValue
     */
    void entryRemovedMemoryCache(String key, Value oldValue);
}
