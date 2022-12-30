package com.xiongtao.myglide.load_data;

import com.xiongtao.myglide.resource.Value;
/**
 * 加载外部资源 成功 和 失败 回调
 */
public interface ResponseListener {
    void responseSuccess(Value value);
    void responseException(Exception e);
}
