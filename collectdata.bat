@setlocal
@echo off
@set cp=c:\windows\system32\buildstatus.jar


if NOT %COMPUTERNAME% == TOBYW goto usage 

java -Ddebug=true -classpath %cp% com.touchclarity.buildstatus.OneTimeUnitTestInfoCollector "C:\Program Files\Apache Software Foundation\Apache2.2\htdocs\screensaver\statistics\junit-stats.xml" \\dev1\c$\tcs_build\archive >> "%HOMEPATH%"\screensaver-gen.log
goto end

:usage
echo Use only from \\TOBYW

:end
echo bye!