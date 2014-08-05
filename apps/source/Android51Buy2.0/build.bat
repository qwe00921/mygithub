@echo off
SET BASE_PATH=%~dp0
rmdir %BASE_PATH%output /Q/S
mkdir  %BASE_PATH%output\
call apkbuilder.bat %BASE_PATH%output\unsigned.apk -u -z %BASE_PATH%bin\resources.ap_ -f  %BASE_PATH%bin\classes.dex -rf  %BASE_PATH%bin\classes -rj %BASE_PATH%lib\alipay_plugin.jar
winrar x %BASE_PATH%output\unsigned.apk %BASE_PATH%output\tmp\
del %BASE_PATH%output\unsigned.apk
for /f "delims=" %%i in (%BASE_PATH%channel.txt) do (
	>%BASE_PATH%output\tmp\assets\channel set/p=%%i<nul
	jar cvf %BASE_PATH%output\tmp.apk.jar -C %BASE_PATH%output\tmp\ .
	jarsigner -verbose -keystore %BASE_PATH%icson.keystore -storepass kunjiangicson2012 -keypass jiangkunicson2012 -signedjar %BASE_PATH%output\icson_%%i_tmp.apk  %BASE_PATH%output\tmp.apk.jar icson_web.keystore -digestalg SHA1 -sigalg MD5withRSA
	zipalign -v 4 %BASE_PATH%output\icson_%%i_tmp.apk %BASE_PATH%output\51buy_%%i.apk
	del %BASE_PATH%output\tmp.apk.jar
	del %BASE_PATH%output\icson_%%i_tmp.apk
)
rmdir %BASE_PATH%output\tmp /Q/S

IF "%1" EQU "" pause 