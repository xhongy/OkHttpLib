package com.unicom.okhttplib.FileDownload;

public class ResponseData {
    long contentLength;
    String modify;
    String eTag;

    public ResponseData(){
    }

    public ResponseData(long contentLength,String modify,String eTag){
        this.contentLength = contentLength;
        this.modify = modify;
        this.eTag = eTag;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getModify() {
        return modify;
    }

    public void setModify(String modify) {
        this.modify = modify;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }
}
