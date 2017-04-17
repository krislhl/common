package com.ppdai.installment.commonlibrary;

import java.util.Map;
import java.util.WeakHashMap;

import io.reactivex.disposables.Disposable;

/**
 * =====================================================
 * @Description:网络请求池
 * @Author:lihailong
 * @Created Time:2017/4/1
 * @Modify:
 * =====================================================
 */

public class HttpRequestPool {
    private WeakHashMap<String,Disposable> pool;
    private HttpRequestPool(){
        pool = new WeakHashMap<>();
    }

    /**
     * 获取单例对象
     * @return
     */
    public static HttpRequestPool getInstance(){
        return SingleHelper.INSTANCE;
    }

    /**
     * 添加网路请求
     * @param tag
     * @param disposable
     */
    public void addRequest(String tag,Disposable disposable){
        pool.put(tag,disposable);
    }

    /**
     * 取消网络请求
     * @param tag
     */
    public void cancelRequest(String tag){
        if (pool.containsKey(tag)){
            Disposable disposable = pool.get(tag);
            if (!disposable.isDisposed()){
                disposable.dispose();
            }
            pool.remove(tag);
        }
    }

    /**
     * 取消所有的网络请求
     */
    public void cancelAllRequest(){
        for (Map.Entry<String,Disposable> entry : pool.entrySet()){
            Disposable disposable = entry.getValue();
            if (!disposable.isDisposed()){
                disposable.dispose();
            }
        }
        pool.clear();
    }
    private static class SingleHelper {
      private static final HttpRequestPool INSTANCE = new HttpRequestPool();
    }
}
