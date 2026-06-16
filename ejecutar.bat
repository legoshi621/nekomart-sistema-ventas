@echo off
SET JAVA_HOME=C:\Program Files\BellSoft\LibericaJDK-25-Full
SET MAVEN_HOME=C:\tools\maven
SET PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo ========================================
echo   NekoMart Sistema de Ventas
echo ========================================
echo Compilando y ejecutando...
echo.

mvn compile exec:java -Dexec.mainClass="com.nekomart.Main" -q

pause
