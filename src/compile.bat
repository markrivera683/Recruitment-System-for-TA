@echo off
setlocal

REM Manual build - run from the src\ folder
REM Requires: Java 11+  (javac on PATH),  Tomcat 9

set TOMCAT_HOME=D:\tomcat\apache-tomcat-9.0.116
set SERVLET_JAR=%TOMCAT_HOME%\lib\servlet-api.jar
set SRC=src\main\java
set WEB=src\main\webapp
set OUT=out\WEB-INF\classes
set WAR=ta-recruitment.war

echo [1/4] Cleaning...
if exist out rmdir /s /q out
mkdir "%OUT%"

echo [2/4] Compiling...
REM Single javac pass with ALL sources (Roles + EducationEntry + CvDownloadServlet + AdminServlet, etc.)
javac --release 11 -encoding UTF-8 -cp "%SERVLET_JAR%" -d "%OUT%" ^
  "%SRC%\com\bupt\ta\model\TaResumeDisplay.java" ^
  "%SRC%\com\bupt\ta\model\TaWorkloadStats.java" ^
  "%SRC%\com\bupt\ta\model\Roles.java" ^
  "%SRC%\com\bupt\ta\model\User.java" ^
  "%SRC%\com\bupt\ta\model\EducationEntry.java" ^
  "%SRC%\com\bupt\ta\model\ApplicantProfile.java" ^
  "%SRC%\com\bupt\ta\model\Application.java" ^
  "%SRC%\com\bupt\ta\model\Job.java" ^
  "%SRC%\com\bupt\ta\service\FileStore.java" ^
  "%SRC%\com\bupt\ta\service\AuthService.java" ^
  "%SRC%\com\bupt\ta\service\ProfileService.java" ^
  "%SRC%\com\bupt\ta\service\ApplicationService.java" ^
  "%SRC%\com\bupt\ta\service\JobService.java" ^
  "%SRC%\com\bupt\ta\util\AppConfig.java" ^
  "%SRC%\com\bupt\ta\util\Strings.java" ^
  "%SRC%\com\bupt\ta\util\HttpJsonClient.java" ^
  "%SRC%\com\bupt\ta\ai\LmProviderType.java" ^
  "%SRC%\com\bupt\ta\ai\LmMessage.java" ^
  "%SRC%\com\bupt\ta\ai\LmRequest.java" ^
  "%SRC%\com\bupt\ta\ai\LmResponse.java" ^
  "%SRC%\com\bupt\ta\ai\LmException.java" ^
  "%SRC%\com\bupt\ta\ai\LmClient.java" ^
  "%SRC%\com\bupt\ta\ai\LmStreamListener.java" ^
  "%SRC%\com\bupt\ta\ai\AiFeatureNames.java" ^
  "%SRC%\com\bupt\ta\ai\LmModelDefaults.java" ^
  "%SRC%\com\bupt\ta\ai\LmConfig.java" ^
  "%SRC%\com\bupt\ta\ai\MockLmClient.java" ^
  "%SRC%\com\bupt\ta\ai\HttpLmClient.java" ^
  "%SRC%\com\bupt\ta\ai\LmClientFactory.java" ^
  "%SRC%\com\bupt\ta\service\ai\AiFeatureOutput.java" ^
  "%SRC%\com\bupt\ta\service\ai\AiLmDefaults.java" ^
  "%SRC%\com\bupt\ta\service\ai\SkillMatchService.java" ^
  "%SRC%\com\bupt\ta\service\ai\MissingSkillService.java" ^
  "%SRC%\com\bupt\ta\service\ai\RecommendationService.java" ^
  "%SRC%\com\bupt\ta\service\ai\AiFeatureService.java" ^
  "%SRC%\com\bupt\ta\servlet\BaseServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\AdminServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\AdminUserServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\AdminJobServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\AdminJobViewServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\AdminTaProfilesServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\AdminCvServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\AdminExportServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\ApplicationServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\LoginServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\RegisterServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\LogoutServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\ProfileServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\JobServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\AiStreamServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\MoServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\CvDownloadServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\ForgotPasswordServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\AiDemoServlet.java" ^
  "%SRC%\com\bupt\ta\servlet\ResetPasswordServlet.java"

if %ERRORLEVEL% neq 0 ( echo [ERROR] Compilation failed. ^& exit /b 1 )

echo [3/4] Assembling WAR...
xcopy /s /e /y "%WEB%\*" "out\"
if not exist "out\WEB-INF\data" mkdir "out\WEB-INF\data"
if not exist "out\WEB-INF\data\users.json" echo [] > "out\WEB-INF\data\users.json"
if not exist "out\WEB-INF\data\profiles.json" echo [] > "out\WEB-INF\data\profiles.json"
if not exist "out\WEB-INF\data\applications.json" echo [] > "out\WEB-INF\data\applications.json"

echo [4/4] Packaging WAR...
cd out
jar -cvf "..\%WAR%" .
cd ..

echo.
echo Done! Copy %WAR% to %TOMCAT_HOME%\webapps\
echo Then open: http://localhost:8080/ta-recruitment/login
endlocal