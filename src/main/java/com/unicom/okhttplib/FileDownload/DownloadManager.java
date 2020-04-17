package com.unicom.okhttplib.FileDownload;

import android.content.Context;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DownloadManager {
    public Context mContext;
    private static final AtomicReference<DownloadManager> INSTANCE = new AtomicReference<>();
    private HashMap<String, Call> downCalls;//用来存放各个下载的请求
    private OkHttpClient mClient;//OKHttpClient;

    //获得一个单例类
    public static DownloadManager getInstance(Context mContext) {
        for (; ; ) {
            DownloadManager current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new DownloadManager(mContext);
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    private DownloadManager(Context mContext) {
        this.mContext = mContext;
        downCalls = new HashMap<>();
        mClient = new OkHttpClient.Builder().build();
    }

    /**
     * 开始下载
     *
     * @param url              下载请求的网址
     * @param downLoadObserver 用来回调的接口
     */
    public void download(String url, DownLoadObserver downLoadObserver) {
        Logger.d("url="+url);
        Observable.just(url)
                .filter(s -> !downCalls.containsKey(s))//call的map已经有了,就证明正在下载,则这次不下载
                .flatMap(s -> Observable.just(createDownInfo(s)))
                .map(this::getRealFileName)//检测本地文件夹,生成新的文件名
                .flatMap(downloadInfo -> Observable.create(new DownloadSubscribe(downloadInfo)))//下载
                .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                .subscribeOn(Schedulers.io())//在子线程执行
                .subscribe(downLoadObserver);//添加观察者
    }

    public void cancel(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            call.cancel();//取消
        }
        downCalls.remove(url);
    }

    /**
     * 创建DownInfo
     *
     * @param url 请求网址
     * @return DownInfo
     */
    private DownloadInfo createDownInfo(String url) {
        DownloadInfo downloadInfo = new DownloadInfo(url);
        ResponseData data = getResponseData(url);
        downloadInfo.setTotal(data.getContentLength());
        downloadInfo.setLastModify(data.getModify());
        downloadInfo.seteTag(data.geteTag());
        String fileName = url.substring(url.lastIndexOf("/"));
        downloadInfo.setFileName(fileName);
        downloadInfo.setSharedPreferencesFileName(FileUtil.getSharedPreferencesFileName(fileName));
        return downloadInfo;
    }

    public void delSharePreferenceFile(String fileName){
        FileUtil.delSharePreferenceFile(mContext,fileName);
    }

    private DownloadInfo getRealFileName(DownloadInfo downloadInfo) throws ParseException {
        String fileName = downloadInfo.getFileName();
        long downloadLength = 0;
        ResponseData data = FileUtil.getFileInfo(mContext,downloadInfo);
        Log.d("coco","开始"+data.getModify()+"   tag="+data.geteTag());
        Log.d("coco","结束"+downloadInfo.getLastModify()+"   tag="+downloadInfo.geteTag());
        File file = new File(mContext.getFilesDir(), fileName);

        // 文件存在，
        if (file.exists()) {
            // 并且服务器文件没有发生变化
            if (data.getModify().equals(downloadInfo.getLastModify()) && data.geteTag().equals(downloadInfo.geteTag())){
                Log.d("coco","11");
                downloadLength = file.length();
            }else {
                Log.d("coco","22");
                // 服务器文件发生变化，则重新下载
                file.delete();
            }
        }
        downloadInfo.setProgress(downloadLength);
        downloadInfo.setFileName(file.getName());
        return downloadInfo;
    }

    private class DownloadSubscribe implements ObservableOnSubscribe<DownloadInfo> {
        private DownloadInfo downloadInfo;

        public DownloadSubscribe(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> e) throws Exception {
            String url = downloadInfo.getUrl();
            long downloadLength = downloadInfo.getProgress();//已经下载好的长度
            long contentLength = downloadInfo.getTotal();//文件的总长度
            // 如果此文件已经下载过就不需要再次下载了
            if (downloadLength == contentLength) {
                Log.d("coco","********");
                return;
            }
            Log.d("coco","downloadLength="+downloadLength+"  contentLength="+contentLength);
            Logger.d("coco="+url+"downloadLength="+downloadLength+"  contentLength="+contentLength);
            //初始进度信息
            e.onNext(downloadInfo);
            // save当前要下载的文件的修改日期与tag
            FileUtil.saveFileInfo(mContext,downloadInfo);

            Request request = new Request.Builder()
                    //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
//                    .addHeader("IF-RANGE",String.valueOf(downloadInfo.getLastModify()))
                    .addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength)
                    .url(url)
                    .build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);//把这个添加到call里,方便取消
            Response response = call.execute();

            File file = new File(mContext.getFilesDir(), downloadInfo.getFileName());
            InputStream is = null;
            FileOutputStream fileOutputStream = null;
            try {
                is = response.body().byteStream();
                fileOutputStream = new FileOutputStream(file, true);
                byte[] buffer = new byte[2048];//缓冲数组2kB
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                    downloadLength += len;
                    downloadInfo.setProgress(downloadLength);
                    e.onNext(downloadInfo);
                }
                fileOutputStream.flush();
                downCalls.remove(url);
            } finally {
                //关闭IO流
                IOUtil.closeAll(is, fileOutputStream);

            }
            e.onComplete();//完成
        }
    }

    /**
     * 获取下载info
     *
     * @param downloadUrl
     * @return
     */
    private ResponseData getResponseData(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                ResponseData data = new ResponseData();
                data.setContentLength(Long.parseLong(response.header("Content-Length")));
                data.setModify(response.header("Last-Modified"));
                data.seteTag(response.header("ETag"));
                response.close();
                return data;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.d("e="+e.getMessage());
        }
        return new ResponseData(DownloadInfo.TOTAL_ERROR, "","");
    }


}
