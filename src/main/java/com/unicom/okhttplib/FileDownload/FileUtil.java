package com.unicom.okhttplib.FileDownload;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FileUtil {
    private static String TAG = "";

    /**
     * 保存当前下载文件的缓存信息
     * @param mContext
     * @param downloadInfo
     */
    public static void saveFileInfo(Context mContext, DownloadInfo downloadInfo) {
        PreferencesUtility preferencesUtility = new PreferencesUtility(
                mContext, downloadInfo.getSharedPreferencesFileName());
        preferencesUtility.setPreferencesField("lastmodify",
                downloadInfo.getLastModify());
        preferencesUtility.setPreferencesField("eTag", downloadInfo.geteTag());
    }

    /**
     * 获取当前下载文件的缓存信息
     * @param mContext
     * @param downloadInfo
     * @return
     */
    public static ResponseData getFileInfo(Context mContext,DownloadInfo downloadInfo) {
        Log.d("coco","filename="+downloadInfo.getFileName());
        ResponseData data = new ResponseData();
        PreferencesUtility preferencesUtility = new PreferencesUtility(
                mContext, downloadInfo.getSharedPreferencesFileName());
        data.setModify(preferencesUtility
                .getPreferencesAsString("lastmodify"));
        data.seteTag(preferencesUtility
                .getPreferencesAsString("eTag"));
        return data;
    }

    /**
     * 获取当前下载文件缓存文件名
     * @param path
     * @return
     */
    public static String getSharedPreferencesFileName(String path){
        String filePath = path;
        if(path.contains("/")){
            filePath = path.split("/")[1];
        }
        return filePath;
    }

    /**
     * 删除指定的缓存文件
     * @param mContext
     * @param fileName
     */
    public static void delSharePreferenceFile(Context mContext,String fileName){
        SharedPreferences sp= mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        sp.edit().clear().commit();
        // 通过文件名获取到文件缓存信息路径
        File file = new File("/data/data/"+mContext.getPackageName()+"/shared_prefs"+fileName);
        file.delete();
    }

    //移除文件，获取文件时间与当前时间对比，我这时间定位5天，删除五天前的文件
    public static void removeFileByTime(String dirPath) {
        //获取目录下所有文件
        List<File> allFile = getDirAllFile(new File(dirPath));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //获取当前时间
        Date end = new Date(System.currentTimeMillis());
        try {
            end = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));
        } catch (Exception e){
            Log.d(TAG, "dataformat exeption e " + e.toString());
        }
        Log.d(TAG, "getNeedRemoveFile  dirPath = "  +dirPath);
        for (File file : allFile) {//ComDef
            try {
                //文件时间减去当前时间
                Date start = dateFormat.parse(dateFormat.format(new Date(file.lastModified())));
                long diff = end.getTime() - start.getTime();//这样得到的差值是微秒级别
                long days = diff / (1000 * 60 * 60 * 24);
                if(5 <= days){
                    deleteFile(file);
                }

            } catch (Exception e){
                Log.d(TAG, "dataformat exeption e " + e.toString());
            }
        }
    }

    //删除文件夹及文件夹下所有文件
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();
        } else if (file.exists()) {
            file.delete();
        }
    }

    //获取指定目录下一级文件
    public static List<File> getDirAllFile(File file) {
        List<File> fileList = new ArrayList<>();
        File[] fileArray = file.listFiles();
        if(fileArray == null)
            return fileList;
        for (File f : fileArray) {
            fileList.add(f);
        }
        fileSortByTime(fileList);
        return fileList;
    }

    //对文件进行时间排序
    public static void fileSortByTime(List<File> fileList) {
        Collections.sort(fileList, new Comparator<File>() {
            public int compare(File p1, File p2) {
                if (p1.lastModified() < p2.lastModified()) {
                    return -1;
                }
                return 1;
            }
        });
    }
}
