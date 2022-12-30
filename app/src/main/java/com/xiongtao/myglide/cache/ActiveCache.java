package com.xiongtao.myglide.cache;

import android.util.TimeUtils;

import com.xiongtao.myglide.Tool;
import com.xiongtao.myglide.resource.Value;
import com.xiongtao.myglide.resource.ValueCallback;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActiveCache {
    // 容器
    private Map<String, WeakReference<Value>> mapList = new HashMap<>();
    private  ReferenceQueue<Value> queue;
    private Thread thread; // 线程--》 死循环
    private boolean isCloseThread; // 死循环的标记
    private boolean isShoudonRemove; // 为了控制 手动移除 和 被动移除 的冲突
    private ValueCallback valueCallback;

    public ActiveCache(ValueCallback valueCallback) {
        this.valueCallback = valueCallback;
    }
    /**
     * TODO 添加 活动缓存
     */
    public void put(String key,Value value){
        Tool.checkNotEmpty(key);
        // 绑定Value的监听（有依赖）
        value.setValueCallback(valueCallback);

//        mapList.put(key,new CustomWeakReference(),key);
        mapList.put(key,new CustomWeakReference(value,getQueue(),key));
    }
    /**
     * TODO 给外界获取Value
     * @param key
     * @return
     */
    public Value get(String key){
        WeakReference<Value> weakReference = mapList.get(key);
        if(null != weakReference){
            return weakReference.get();
        }
        return null;
    }

    public Value remove(String key){
        isShoudonRemove = true;
        WeakReference<Value> remove = mapList.remove(key);
        isShoudonRemove = false;
        if(null != remove){
            return remove.get();
        }
        return null;
    }

    public class CustomWeakReference extends WeakReference<Value>{
        // 没有办法去监听，什么时候GC回收了
//        public CustomWeakReference(Value referent) {
//            super(referent);
//        }
        private String key;
        public CustomWeakReference(Value referent, ReferenceQueue<? super Value> q,String key) {
            super(referent, q);
            this.key = key;
        }
    }

    public void coloseThread(){
        isCloseThread = true;

        if(thread != null){
            thread.interrupt();

            try {
                thread.join(TimeUnit.SECONDS.toMillis(5));// 线程稳定的停止下了
                if(thread.isAlive()){
                    throw new IllegalStateException("活动缓存中，关闭线程，无法停止下了");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mapList.clear();
        System.gc();
    }

    /**
     * 监听 什么 时候 被回收
     * @return
     */
    private ReferenceQueue<Value> getQueue(){
        if(queue == null){
            queue = new ReferenceQueue<>();
        }
        // 死循环不停的跑【加线程】
        thread = new Thread(){
            @Override
            public void run() {
                super.run();

                while (!isCloseThread){// 这个循环如何结束？
                    if(!isShoudonRemove){
                        try {
//                            queue.remove();
                            // TODO 后续一定要调试
                            // 阻塞式 等待：什么时候被回收 就释放
                            Reference<? extends Value> remove = queue.remove();
                            // 开始干活 -- TODO 被动移除
                            CustomWeakReference customWeakReference = (CustomWeakReference) remove;

                            if(mapList !=null && !mapList.isEmpty()){
                                mapList.remove(customWeakReference.key); // 容器里面的内容移除
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        };
        thread.start();

        return queue;
    }
}
