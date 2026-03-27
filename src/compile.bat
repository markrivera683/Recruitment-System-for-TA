@echo off
setlocal

REM Manual build - run from the src\ folder
REM Requires: Java 11+  (javac on PATH),  Tomcat 9

set TOMCAT_HOME=D:\Apps\IntelliJ Idea\apache-tomcat-9.0.115
set SERVLET_JAR=%TOMCAT_HOME%\lib\servlet-api.jar
set SRC=src\main\java
set WEB=src\main\webapp
set OUT=out\WEB-INF\classes
set WAR=ta-recruitment.war

echo [1/4] Cleaning...
if exist out rmdir /s /q out
mkdir "%OUT%"

echo [2/4] Compiling...
javac -source 11 -target 11 -encoding UTF-8 -cp "%SERVLET_JAR%" -d "%OUT%" "%SRC%\com\bupt\ta\model\User.java" "%SRC%\com\bupt\ta\model\ApplicantProfile.java" "%SRC%\com\bupt\ta\service\FileStore.java" "%SRC%\com\bupt\ta\service\AuthService.java" "%SRC%\com\bupt\ta\service\ProfileService.java" "%SRC%\com\bupt\ta\servlet\BaseServlet.java" "%SRC%\com\bupt\ta\servlet\LoginServlet.java" "%SRC%\com\bupt\ta\servlet\RegisterServlet.java" "%SRC%\com\bupt\ta\servlet\LogoutServlet.java" "%SRC%\com\bupt\ta\servlet\ProfileServlet.java" "%SRC%\com\bupt\ta\servlet\ForgotPasswordServlet.java" "%SRC%\com\bupt\ta\servlet\ResetPasswordServlet.java"

if %ERRORLEVEL% neq 0 ( echo [ERROR] Compilation failed. ^& exit /b 1 )

echo [3/4] Assembling WAR...
xcopy /s /e /y "%WEB%\*" "out\"
if not exist "out\WEB-INF\data" mkdir "out\WEB-INF\data"
if not exist "out\WEB-INF\data\users.json" echo [] > "out\WEB-INF\data\users.json"
if not exist "out\WEB-INF\data\profiles.json" echo [] > "out\WEB-INF\data\profiles.json"

echo [4/4] Packaging WAR...
cd out
jar -cvf "..\%WAR%" .
cd ..

echo.
echo Done! Copy %WAR% to %TOMCAT_HOME%\webapps\
echo Then open: http://localhost:8080/ta-recruitment/login
endlocal