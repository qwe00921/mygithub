SET BASE_PATH=%~dp0
call %BASE_PATH%build.bat nopause
adb install -r %BASE_PATH%output/*_pc.apk
adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.icson/com.icson.portal.PortalActivity
IF "%1" EQU "" pause 

