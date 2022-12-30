package com.xiongtao.myglide;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.xiongtao.myglide.fragment.ActivityFragmentManager;
import com.xiongtao.myglide.fragment.FragmentActivityFragmentManager;

public class RequestManager {
    private static final String TAG = RequestManager.class.getSimpleName();

    private final String FRAGMENT_ACTIVITY_NAME = "Fragment_Activity_NAME";
    private final String ACTIVITY_NAME = "Activity_NAME";
    private final int NEXT_HANDLER_MSG = 995465; // Handler 标记
    // 总的环境
    private Context requestManagerContext;
    private static RequestTargetEngine callback;

    // 构造代码块，不用再所有的构造方法里面去实例化了，统一的去写
    {
        if(callback == null){
            callback = new RequestTargetEngine();
        }
    }

    /**
     * 可以管理生命周期 -- FragmentActivity是有生命周期方法
     *
     * @param fragmentActivity
     */
    FragmentActivity fragmentActivity;

    public RequestManager(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
        this.requestManagerContext = fragmentActivity;

        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();

        Fragment fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_ACTIVITY_NAME);
        if(fragment == null){ // 如果等于null，就要去创建Fragment
            fragment = new FragmentActivityFragmentManager(callback);

            // 添加到管理器 -- fragmentManager.beginTransaction().add..
            supportFragmentManager.beginTransaction().add(fragment,FRAGMENT_ACTIVITY_NAME).commitAllowingStateLoss();
        }
// TODO
        // 测试下面的话，这种测试，不能完全准确
        // 证明是不是排队状态
        Fragment fragment2 = supportFragmentManager.findFragmentByTag(FRAGMENT_ACTIVITY_NAME);
        Log.d(TAG, "RequestManager: fragment2" + fragment2);// null ： 还在排队中，还没有消费


        mHandler.sendEmptyMessage(NEXT_HANDLER_MSG);

    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            Fragment fragment2 = fragmentActivity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_ACTIVITY_NAME);
            Log.d(TAG, "Handler: fragment2" + fragment2); // 有值 ： 不在排队中，所以有值
            return false;
        }
    });
    /**
     * 可以管理生命周期 -- Activity是有生命周期方法的(Fragment)
     * @param activity
     */
    public RequestManager(Activity activity){
        this.requestManagerContext = activity;
// 开始绑定操作
        android.app.FragmentManager fragmentManager = activity.getFragmentManager();


        android.app.Fragment fragment  = fragmentManager.findFragmentByTag(ACTIVITY_NAME);

        if(fragment == null){
            fragment = new ActivityFragmentManager(callback);
// 添加到管理器 -- fragmentManager.beginTransaction().add..
            fragmentManager.beginTransaction().add(fragment,ACTIVITY_NAME).commitAllowingStateLoss();

        }

        // TODO 测试
        android.app.Fragment fragment2 = fragmentManager.findFragmentByTag(ACTIVITY_NAME);
        Log.d(TAG, "RequestManager: fragment2" + fragment2); // null ： 还在排队中，还没有消费

        // 发送一次Handler
        mHandler.sendEmptyMessage(NEXT_HANDLER_MSG);
    }

    /**
     * 目前没有做管理  第三个构造函数
     * @param context
     */
    public RequestManager(Context context) {
        this.requestManagerContext = context;
    }

    public RequestTargetEngine  load(String path){
        // 移除掉
        mHandler.removeMessages(NEXT_HANDLER_MSG);

        // 下次 全部串起来
        // ...
        callback.loadValueInitAction(path,requestManagerContext);
        return callback;
    }

}
