@setlocal
@echo off
@set cp=.\lib\htmlparser.jar
@set cp=%cp%;.\lib\jcommon-1.0.6.jar
@set cp=%cp%;.\lib\jfreechart-1.0.3.jar
@set cp=%cp%;.\lib\saverbeans-api.jar
@set cp=%cp%;.\lib\xstream-1.2.2.jar
@set cp=%cp%;bin
java -classpath %cp% com.touchclarity.buildstatus.DisplayableScreenSaver com.touchclarity.buildstatus.ScreenSaver -singleScreen