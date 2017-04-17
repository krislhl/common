package com.ppdai.installment.commonlibrary;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;

/**
 * =====================================================
 * @Description: 封装网络请求Get、Post方式
 * @Author:lihailong
 * @Created Time:2017/4/1
 * @Modify:
 * =====================================================
 */

public interface ApiService {

    @GET("{url}")
    Observable<ResponseBody> execGet(@Path("url") String url,@QueryMap Map<String,String> paramMap);

    @POST("{url}")
    Observable<ResponseBody> execPost(@Path("url") String url,@QueryMap Map<String,String> paramMap);

    @Multipart
    @POST("{url}")
    Observable<ResponseBody> execUploadFile(@Path("url") String url, @Part MultipartBody.Part file);

    @Multipart
    @POST("{url}")
    Observable<ResponseBody> execUploadMultiFile(@Path("url") String url, @PartMap Map<String, RequestBody> partMap);

    @Streaming
    @GET("{url}")
    Observable<RequestBody> execDownloadFile(@Path("url") String url);

}
