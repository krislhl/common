package com.ppdai.installment.commonlibrary;

/**
 * =====================================================
 * @Description: http请求结果接口
 * @Author:lihailong
 * @Created Time:2017/4/1
 * @Modify:
 * =====================================================
 */

public interface HttpRequestResult<T> {
    /**
     * 请求成功
     * @param t
     */
     void onSuccess(T t);

    /**
     * 请求失败
     * @param message
     */
     void onFail(String message);
}
