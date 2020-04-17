package com.unicom.okhttplib.FileDownload;

/**
 * 下载信息
 */

public class DownloadInfo {
    public static final long TOTAL_ERROR = -1;//获取进度失败
    private String url;
    private long total;
    private long progress;
    private String fileName;
    private String lastModify;
    private String eTag;
    private String sharedPreferencesFileName;


    public DownloadInfo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public static long getTotalError() {
        return TOTAL_ERROR;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLastModify() {
        return lastModify;
    }

    public void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getSharedPreferencesFileName() {
        return sharedPreferencesFileName;
    }

    public void setSharedPreferencesFileName(String sharedPreferencesFileName) {
        this.sharedPreferencesFileName = sharedPreferencesFileName;
    }
}
