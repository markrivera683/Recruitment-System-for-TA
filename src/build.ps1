# Build script for TA Recruitment System
# Run from the src\ folder:  .\build.ps1

$JAVAC       = "D:\Apps\OpenJDKs\OpenJDK21.0.2\bin\javac.exe"
$JAR         = "D:\Apps\OpenJDKs\OpenJDK21.0.2\bin\jar.exe"
$TOMCAT      = "D:\Apps\IntelliJ Idea\apache-tomcat-9.0.115"
$SERVLET_JAR = "$TOMCAT\lib\servlet-api.jar"

$BASE   = Split-Path -Parent $MyInvocation.MyCommand.Path
$SRC    = "$BASE\src\main\java"
$WEB    = "$BASE\src\main\webapp"
$OUT    = "$BASE\out"
$OUTBIN = "$OUT\WEB-INF\classes"
$WAR    = "$BASE\ta-recruitment.war"

Write-Host "[1/4] Cleaning..."
if (Test-Path $OUT) { Remove-Item -Recurse -Force $OUT }
New-Item -ItemType Directory -Force -Path $OUTBIN | Out-Null

Write-Host "[2/4] Compiling..."
# Must match compile.bat: single pass, all sources (Roles, EducationEntry, CvDownloadServlet, …)
$sources = @(
    "$SRC\com\bupt\ta\model\Roles.java",
    "$SRC\com\bupt\ta\model\User.java",
    "$SRC\com\bupt\ta\model\EducationEntry.java",
    "$SRC\com\bupt\ta\model\ApplicantProfile.java",
    "$SRC\com\bupt\ta\service\FileStore.java",
    "$SRC\com\bupt\ta\service\AuthService.java",
    "$SRC\com\bupt\ta\service\ProfileService.java",
    "$SRC\com\bupt\ta\servlet\BaseServlet.java",
    "$SRC\com\bupt\ta\servlet\AdminServlet.java",
    "$SRC\com\bupt\ta\servlet\LoginServlet.java",
    "$SRC\com\bupt\ta\servlet\RegisterServlet.java",
    "$SRC\com\bupt\ta\servlet\LogoutServlet.java",
    "$SRC\com\bupt\ta\servlet\ProfileServlet.java",
    "$SRC\com\bupt\ta\servlet\CvDownloadServlet.java",
    "$SRC\com\bupt\ta\servlet\ForgotPasswordServlet.java",
    "$SRC\com\bupt\ta\servlet\ResetPasswordServlet.java"
)
& $JAVAC --release 11 -encoding UTF-8 -cp $SERVLET_JAR -d $OUTBIN @sources
if ($LASTEXITCODE -ne 0) { Write-Host "[ERROR] Compilation failed."; exit 1 }

Write-Host "[3/4] Assembling WAR..."
Copy-Item -Recurse -Force "$WEB\*" $OUT
if (-not (Test-Path "$OUT\WEB-INF\data")) { New-Item -ItemType Directory -Force -Path "$OUT\WEB-INF\data" | Out-Null }
if (-not (Test-Path "$OUT\WEB-INF\data\users.json"))    { Set-Content "$OUT\WEB-INF\data\users.json"    '[]' }
if (-not (Test-Path "$OUT\WEB-INF\data\profiles.json")) { Set-Content "$OUT\WEB-INF\data\profiles.json" '[]' }

Write-Host "[4/4] Packaging WAR..."
Push-Location $OUT
& $JAR -cvf $WAR .
Pop-Location

Write-Host ""
Write-Host "Done!  $WAR"
Write-Host "Copy to: $TOMCAT\webapps\"
Write-Host "Open:    http://localhost:8080/ta-recruitment/login"