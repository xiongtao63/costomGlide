package com.xiongtao.myglide.resource;

import android.graphics.Bitmap;
import android.util.Log;

import com.xiongtao.myglide.Tool;

/**
 * Bitmap的封装
 */
public class Value {
    private static final String TAG = Value.class.getSimpleName();
    // 单例模式
    public static Value value;

    public static Value getInstance() {
        if (null == value) {
            synchronized (Value.class) {
                if (null == value) {
                    value = new Value();
                }
            }
        }
        return value;
    }

    private Bitmap mBitmap;
    private int count;
    private ValueCallback valueCallback;
    private String key;

    public static Value getValue() {
        return value;
    }

    public static void setValue(Value value) {
        Value.value = value;
    }

    public Bitmap getmBitmap() {
        return mBitmap;
    }

    public void setmBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ValueCallback getValueCallback() {
        return valueCallback;
    }

    public void setValueCallback(ValueCallback valueCallback) {
        this.valueCallback = valueCallback;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * TODO 使用一次 计数一次 + 1
     */
    public void useAction() {
        Tool.checkNotEmpty(mBitmap);

        if (mBitmap.isRecycled()) {
            Log.d(TAG, "useAction: 已经被回收了");
            return;
        }
        Log.d(TAG, "useAction: 加一，count:" + count);
        count++;
    }

    /**
     * TODO  不使用一次(使用完成) 计数一次 - 1
     */
    public void nonUseAction() {
        // + 1 = 1
        // - 1 = 0
        count--;
        if (count <= 0 && valueCallback != null) {
            // 证明我们的Value没有使用（管理回收）
            // 告诉外界，回调接口
            valueCallback.valueNonUseListener(key, this);// 活动缓存管理监听
        }
        Log.d(TAG, "nonUseAction: 减一，count:" + count);
    }
    /**
     * TODO　释放
     */
    public void recycleBitmap(){
        if(count>0){// 正在使用中...
            Log.d(TAG, "recycleBitmap: 引用计数大于0，正在使用中...，不能释放");
            return;
        }
        if(mBitmap.isRecycled()){
            Log.d(TAG, "recycleBitmap: 都已经被回收了，不能释放");
            return;
        }
        mBitmap.recycle();
        value = null;
        System.gc();
    }
}
