package com.ppdai.installment.commonlibrary;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSink;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * =====================================================
 * 网络请求管理单例类
 * @author:lihailong
 * create by 2017/4/1
 * =====================================================
 */

public class HttpManager {

    /**
     * 网络请求接口服务
     */
    private ApiService mApiService;
    /**
     * 网络请求的host
     */
    private String mBaseUrl;

    private HttpManager(){

    }

    /**
     * 获取单例对象
     * @return
     */
    public static HttpManager getInstance(){
        return SingleHelper.INSTANCE;
    }
    /**
     * 初始化管理类的参数
     * @param baseUrl 网络请求host
     */
    public void init(String baseUrl){
        if (TextUtils.isEmpty(mBaseUrl)||!mBaseUrl.equals(baseUrl)) {
            mBaseUrl = baseUrl;
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(mBaseUrl)
                    .client(getClient())
                    .build();
            mApiService = retrofit.create(ApiService.class);
        }
    }

    /**
     * 自定义OkhttpClient,包括网络连接超时、自定义Header、日志输出
     * @return
     */
    private OkHttpClient getClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                if (BuildConfig.DEBUG){
                    Log.e("pfl api",message);
                }
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(30,TimeUnit.SECONDS)
                .writeTimeout(30,TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("", "")
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .addInterceptor(logging)
                .build();

        return okHttpClient;
    }

    /**
     * get方式请求
     * @param url
     * @param paramMap
     * @param httpRequestResult
     * @param clazz
     * @param <T>
     */
    public<T> void get(String url, Map<String,String> paramMap, final HttpRequestResult<T> httpRequestResult, final Class<T> clazz){
        Disposable disposable = tranformer(mApiService.execGet(url, paramMap), httpRequestResult, clazz);
        HttpRequestPool.getInstance().addRequest(url,disposable);
    }

    /**
     * post方式请求
     * @param url
     * @param paramMap
     * @param httpRequestResult
     * @param clazz
     * @param <T>
     */
    public <T> void post(String url, Map<String,String> paramMap,  HttpRequestResult<T> httpRequestResult, final Class<T> clazz){
        Disposable disposable = tranformer(mApiService.execPost(url, paramMap), httpRequestResult, clazz);
        HttpRequestPool.getInstance().addRequest(url,disposable);
    }

    /**
     * 单文件上传，带进度
     * @param url
     * @param filePath
     * @param httpRequestResult
     * @param clazz
     * @param <T>
     */
    public <T> void uploadFile(String url, String filePath, final HttpRequestResult<T> httpRequestResult,UploadListener listener, final Class<T> clazz){
        CountingRequstBody body = new CountingRequstBody(RequestBody.create(MediaType.parse("image/*"), new File(filePath)), listener);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file",filePath,body);
        mApiService.execUploadFile(url,part).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<ResponseBody, T>() {
                    @Override
                    public T apply(@NonNull ResponseBody responseBody) throws Exception {
                        Gson gson = new Gson();
                        String json = responseBody.string();
                        T t = gson.fromJson(json, clazz);
                        return t;
                    }
                })
                .subscribe(new Consumer<T>() {
                    @Override
                    public void accept(@NonNull T t) throws Exception {
                        httpRequestResult.onSuccess(t);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        httpRequestResult.onFail(throwable.getMessage());
                    }
                });
    }


    /**
     * 多文件上传
     * @param url
     * @param filePaths
     * @param httpRequestResult
     * @param clazz
     * @param <T>
     */
    public  <T> void uploadMultiFile(String url, String[] filePaths, final HttpRequestResult<T> httpRequestResult, final Class<T> clazz){
        HashMap<String, RequestBody> partMap = new HashMap<>();
        for (String filePath:filePaths) {
            File file = new File(filePath);
            if (file.exists()) {
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
                partMap.put("file\"; filename=\""+file.getName()+"\"",fileBody);
            }
        }
        mApiService.execUploadMultiFile(url,partMap).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<ResponseBody, T>() {
                    @Override
                    public T apply(@NonNull ResponseBody responseBody) throws Exception {
                        Gson gson = new Gson();
                        String json = responseBody.string();
                        T t = gson.fromJson(json, clazz);
                        return t;
                    }
                })
                .subscribe(new Consumer<T>() {
                    @Override
                    public void accept(@NonNull T t) throws Exception {
                        httpRequestResult.onSuccess(t);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        httpRequestResult.onFail(throwable.getMessage());
                    }
                });
    }


    /**
     * 复用代码
     * @param observable
     * @param httpRequestResult
     * @param clazz
     * @param <T>
     */
    private <T> Disposable tranformer(Observable<ResponseBody> observable, final HttpRequestResult<T> httpRequestResult, final Class<T> clazz){
        Disposable disposable = observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, T>() {
                    @Override
                    public T apply(@NonNull ResponseBody responseBody) throws Exception {
                        Gson gson = new Gson();
                        String json = responseBody.string();
                        T t = gson.fromJson(json, clazz);
                        return t;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<T>() {
                    @Override
                    public void accept(@NonNull T t) throws Exception {
                        httpRequestResult.onSuccess(t);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        httpRequestResult.onFail(throwable.getMessage());
                    }
                });
        return disposable;
    }

    /**
     * 通过tag取消网络请求
     * @param tag 请求的tag
     */
    public void cancelByTag(String tag){
        HttpRequestPool.getInstance().cancelRequest(tag);
    }

    /**
     * 取消所有的网络请求
     */
    public void cancelAll(){
        HttpRequestPool.getInstance().cancelAllRequest();
    }

    /**
     * 单例帮助类
     */
    private static class SingleHelper {
        private static final HttpManager INSTANCE = new HttpManager();
    }
}
