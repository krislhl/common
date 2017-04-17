package com.ppdai.installment.commonlibrary;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * =====================================================
 * @Description:
 * @Author:lihailong
 * @Created Time:2017/4/6
 * @Modify:
 * =====================================================
 */

public class CountingRequstBody extends RequestBody {

    private final RequestBody mRequestBody;
    private final UploadListener mListener;

    public CountingRequstBody(RequestBody requestBody, UploadListener listener){
        this.mRequestBody = requestBody;
        this.mListener  = listener;
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        try {
            return mRequestBody.contentLength();
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink bufferedSink;
        CountingBufferedSink countingBufferedSink = new CountingBufferedSink(sink);
        bufferedSink = Okio.buffer(countingBufferedSink);
        mRequestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private class CountingBufferedSink extends ForwardingSink{

        private long byteWriten;

        public CountingBufferedSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            byteWriten += byteCount;
            if (mListener != null) {
                mListener.onRequestProgress(byteWriten, contentLength());
            }
        }
    }
}
