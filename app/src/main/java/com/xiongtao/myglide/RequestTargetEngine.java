package com.xiongtao.myglide;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.xiongtao.myglide.cache.ActiveCache;
import com.xiongtao.myglide.cache.MemoryCache;
import com.xiongtao.myglide.cache.MemoryCacheCallback;
import com.xiongtao.myglide.cache.disk.DiskLruCacheImpl;
import com.xiongtao.myglide.fragment.LifecycleCallback;
import com.xiongtao.myglide.load_data.LoadDataManager;
import com.xiongtao.myglide.load_data.ResponseListener;
import com.xiongtao.myglide.resource.Key;
import com.xiongtao.myglide.resource.Value;
import com.xiongtao.myglide.resource.ValueCallback;

public class RequestTargetEngine implements LifecycleCallback, ValueCallback , MemoryCacheCallback, ResponseListener {
    private static final String TAG = RequestTargetEngine.class.getSimpleName();
    @Override
    public void glideInitAction() {
        Log.d(TAG, "glideInitAction: Glide生命周期之 已经开启了 初始化了....");
    }

    @Override
    public void glideStopAction() {
        Log.d(TAG, "glideStopAction: Glide生命周期之 已经停止中 ....");
    }

    @Override
    public void glideRecycleAction() {
        Log.d(TAG, "glideRecycleAction: Glide生命周期之 进行释放操作 缓存策略释放操作等 >>>>>> ....");
    }



    private ActiveCache activeCache; // 活动缓存
    private MemoryCache memoryCache; // 内存缓存
    private DiskLruCacheImpl diskLruCache; // 磁盘缓存
    // Glide 获取 内存的 八分之一
    private final int MEMORY_MAX_SIXE = 1024 * 1024 * 60;

    public RequestTargetEngine() {
        if(activeCache == null){
            activeCache= new ActiveCache(this);// 回调给外界，Value资源不再使用了 设置监听
        }
        if(memoryCache == null){
            memoryCache = new MemoryCache(MEMORY_MAX_SIXE);
            memoryCache.setMemoryCacheCallback(this);
        }
        diskLruCache = new DiskLruCacheImpl();
    }


    public void into(ImageView imageView){
        this.imageView = imageView;
        Tool.checkNotEmpty(imageView);
        Tool.assertMainThread();// 非主线程 抛出异常
        // 触发 缓存机制
        // TODO 加载资源 ---》缓存机制 ---》网络/sd/加载资源成功后 ---》把资源保存到缓存中
        Value value = cacheAction();

    }
    /**
     * TODO 加载资源 ---》缓存机制 ---》网络/sd/加载资源成功后 ---》把资源保存到缓存中
     */
    private Value cacheAction() {
        // TODO 第一步：判断活动缓存是否有资源，如果有资源，就返回，  否则就继续往下找
        Value value = activeCache.get(key);
        if(value != null){
            Log.d(TAG, "cacheAction: 本次加载的是在（活动缓存）中获取的资源>>>");
            value.useAction();// 返回 == 代表  使用了一次  Value
            return value;
        }

        // TODO 第二步：判断内存缓存是否对资源，如果有资源 剪切（内存--->活动），就返回，   否则就继续往下找
        value = memoryCache.get(key);
        if(value != null){
            Log.d(TAG, "cacheAction: 本次加载的是在（内存缓存）中获取的资源>>>");
            // 移动操作 剪切（内存--->活动）
            memoryCache.shoudongRemove(key); // 移除内存缓存
            activeCache.put(key,value); // 把内存缓存中的元素，加入到活动缓存中...
            // 返回 == 代表  使用了一次  Value
            value.useAction(); // +1
            return value;

        }

        // TODO 第三步：从磁盘缓存中你去找，如果找到了，把磁盘缓存中的元素 加入到 活动缓存中....

        value = diskLruCache.get(key);
        if(value != null){
            Log.d(TAG, "cacheAction: 本次加载的是在（磁盘缓存）中获取的资源>>>");
            // 把磁盘缓存中的元素 ---- 加入 ---》 活动缓存中....
            activeCache.put(key,value);
            // 返回 == 代表  使用了一次  Value
            value.useAction(); // +1
            return value;

        }

        // TODO 第四步， 真正去加载外部资源， 去 网络 SDcard 等等
        value = new LoadDataManager().loadResource(path,this,glideContext);
        if(value != null){
            return value;
        }
        return null;
    }

    private String path;
    private Context glideContext;
    private String key; // ac037ea49e34257dc5577d1796bb137dbaddc0e42a9dff051beee8ea457a4668 (磁盘缓存用的key)
    private ImageView imageView; // 显示的目标
    /**
     * RequestManager传递过来的值
     * @param path
     * @param requestManagerContext
     */
    public void loadValueInitAction(String path,Context requestManagerContext){
        this.path = path;
        this.glideContext = requestManagerContext;
        this.key = new Key(path).getKey();
    }
    /**
     * 活动缓存间接的调用 Value发出的（-1 == 0）
     * 专门给Value，不再使用，的回调接口
     * 监听的方法（Value不再使用了）
     * @param key
     * @param value
     */
    @Override
    public void valueNonUseListener(String key, Value value) { // 把活动缓存 移除
        if(key != null && value != null){ //  加入到 内存缓存
            memoryCache.put(key,value);
        }

    }
    /**
     * 活动缓存 的 LRU 被动移除后，回调的函数
     * 内存缓存中，LRU移除 回调的接口
     * 内存缓存中移除的 key--value
     * @param key
     * @param oldValue
     */
    @Override
    public void entryRemovedMemoryCache(String key, Value oldValue) {

    }
    /**
     * 外置资源 成功  回调
     * @param value
     */
    @Override
    public void responseSuccess(Value value) {
        if (null != value) {
            saveCahce(key, value); // 调用触发保存

            imageView.setImageBitmap(value.getmBitmap()); // 显示给目标
        }
    }
    /**
     * 外置资源加载成功后  保存到磁盘缓存
     */
    private void saveCahce(String key, Value value) {
        Log.d(TAG, "saveCahce: >>>>>>>>>>>>>>>>>>>>>>>>>> 加载外置资源成功后 ，保存到缓存中， key:" + key + " value:" + value);
        value.setKey(key);
        if(diskLruCache !=null){
            diskLruCache.put(key,value);// 保存到磁盘缓存中....
        }
    }

    @Override
    public void responseException(Exception e) {
        Log.d(TAG, "responseException: 加载外部资源失败 e:" + e.getMessage());
    }
}
