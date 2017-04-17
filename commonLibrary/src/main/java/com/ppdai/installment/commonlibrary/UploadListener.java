package com.ppdai.installment.commonlibrary;

/**
 * =====================================================
 * @Description: 上传文件监听
 * @Author:lihailong
 * @Created Time:2017/4/6
 * @Modify:
 * =====================================================
 */

public interface UploadListener {

    void onRequestProgress(long bytesWriten,long contentLength);
}
