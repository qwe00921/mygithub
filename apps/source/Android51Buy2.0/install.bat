SET BASE_PATH=%~dp0
call %BASE_PATH%build.bat nopause
adb uninstall com.icson
adb install %BASE_PATH%output/51buy_pc.apk
adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.icson/com.icson.portal.PortalActivity
IF "%1" EQU "" pause 
