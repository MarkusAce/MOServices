@echo off
set "JAVA_BIN=java"
if defined JAVA_HOME set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
"%JAVA_BIN%" -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
