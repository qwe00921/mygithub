版本发布流程：

1. 关闭debug输出： icson\src\com\icson\util\Config.java， 修改： DEBUG = false;

2. 更新版本号:icson\AndroidManifest.xml,  修改versionCode 和 versionName

3. 项目右键->Android Tools -> Export Unsigned Application Package..., 导出文件存放路径: icson\bin\icson_unsigned.apk

4. 执行icson\build.bat， 生成的文件位于icson\output目录下

5. 上传apk文件至外网服务器

6. 修改下载入口：icson\webapp\app_icson_com\mod\amore.php, 关键修改version和url字段，（注： amore.php中version 字段必须与versionName一致 , 否则appe版本始终认为不是最新版)