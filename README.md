# OkHttpLib
使用okhttp实现文件断点续传功能

## 集成如下
### 在需要集成此功能的module下面的build.gradle文件添加如下代码：
``` code
android {
    ...
    
  // add 开启Java1.8 能够使用lambda表达式,用到了lambda语法
    compileOptions{
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    ....
    implementation 'com.coco.plugin:OkHttpPlugin:1.0.0'
    implementation 'io.reactivex.rxjava2:rxandroid:2.0.1'
}
```
### 调用文件下载方法如下
``` code
public void download(){
        File cacheUrl = MainActivity.this.getCacheDir();
        // downloadurl--要下载的文件的地址（自定义）
        // cacheUrl 文件下载后保存路径（自定义）
        // DownLoadObserver 监测下载情况（写死）
        DownloadManager.getInstance(MainActivity.this).download(downloadurl,cacheUrl, new DownLoadObserver() {
            @Override
            public void onNext(DownloadInfo value) {
                super.onNext(value);
            }

            @Override
            public void onComplete() {
                Log.d("coco","finish");
                if (downloadInfo != null) {
                    Toast.makeText(MainActivity.this,
                            downloadInfo.getFileName() + "-DownloadComplete",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
```
    
### AndroidManifest.xml文件添加相关权限
``` code
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<!-- 添加读写权限 -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
### 如果当前api版本大于等于28还需要在AndroidManifest.xml添加一下代码
``` code
 <application
    android:networkSecurityConfig="@xml/network_security_config"
    ....
 ```
在res目录下添加@xml/network_security_config，内容如下
``` code
<?xml version="1.0" encoding="utf-8"?>
    <network-security-config>
        <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```
    
