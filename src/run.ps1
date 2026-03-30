# build-and-run.ps1 - Compile, package, deploy and start Tomcat
# Usage: cd src; .\build-and-run.ps1

# ============================================================
#  CONFIGURE THESE TWO PATHS FOR YOUR MACHINE
# ============================================================
$JDK_HOME = 'D:\Apps\OpenJDKs\OpenJDK21.0.2'
$TOMCAT   = 'D:\Apps\IntelliJ Idea\apache-tomcat-9.0.115'
# ============================================================

$JAVAC       = "$JDK_HOME\bin\javac.exe"
$JAR         = "$JDK_HOME\bin\jar.exe"
$SERVLET_JAR = "$TOMCAT\lib\servlet-api.jar"

$env:JAVA_HOME     = $JDK_HOME
$env:CATALINA_HOME = $TOMCAT

# --- Validate paths ---
foreach ($p in @($JAVAC, $JAR, $SERVLET_JAR, "$TOMCAT\bin\startup.bat")) {
    if (-not (Test-Path $p)) { Write-Host "[ERROR] Not found: $p"; exit 1 }
}

$BASE   = Split-Path -Parent $MyInvocation.MyCommand.Path
$SRC    = "$BASE\src\main\java"
$WEB    = "$BASE\src\main\webapp"
$OUT    = "$BASE\out"
$OUTBIN = "$OUT\WEB-INF\classes"
$WAR    = "$BASE\ta-recruitment.war"

# --- 1. Clean ---
Write-Host '[1/4] Cleaning...'
if (Test-Path $OUT) { Remove-Item -Recurse -Force $OUT }
New-Item -ItemType Directory -Force -Path $OUTBIN | Out-Null

# --- 2. Compile ---
Write-Host '[2/4] Compiling...'
$sources = @(
    "$SRC\com\bupt\ta\model\Roles.java",
    "$SRC\com\bupt\ta\model\EducationEntry.java",
    "$SRC\com\bupt\ta\model\User.java",
    "$SRC\com\bupt\ta\model\ApplicantProfile.java",
    "$SRC\com\bupt\ta\service\FileStore.java",
    "$SRC\com\bupt\ta\service\AuthService.java",
    "$SRC\com\bupt\ta\service\ProfileService.java",
    "$SRC\com\bupt\ta\servlet\BaseServlet.java",
    "$SRC\com\bupt\ta\servlet\LoginServlet.java",
    "$SRC\com\bupt\ta\servlet\RegisterServlet.java",
    "$SRC\com\bupt\ta\servlet\LogoutServlet.java",
    "$SRC\com\bupt\ta\servlet\ProfileServlet.java",
    "$SRC\com\bupt\ta\servlet\ForgotPasswordServlet.java",
    "$SRC\com\bupt\ta\servlet\ResetPasswordServlet.java"
)
& $JAVAC -source 11 -target 11 -encoding UTF-8 -cp $SERVLET_JAR -d $OUTBIN @sources
if ($LASTEXITCODE -ne 0) { Write-Host '[ERROR] Compilation failed.'; exit 1 }
Write-Host "Compiled $($sources.Count) files OK"

# --- 3. Package ---
Write-Host '[3/4] Packaging WAR...'
Copy-Item -Recurse -Force "$WEB\*" $OUT
$dataDir = "$OUT\WEB-INF\data"
if (-not (Test-Path $dataDir)) { New-Item -ItemType Directory -Force -Path $dataDir | Out-Null }
if (-not (Test-Path "$dataDir\users.json"))    { Set-Content "$dataDir\users.json"    '[]' }
if (-not (Test-Path "$dataDir\profiles.json")) { Set-Content "$dataDir\profiles.json" '[]' }
Push-Location $OUT
& $JAR -cvf $WAR . | Out-Null
Pop-Location
Copy-Item -Force $WAR "$TOMCAT\webapps\"
Write-Host "WAR deployed: $WAR"

# --- 4. Start Tomcat ---
Write-Host '[4/4] Starting Tomcat...'
& "$TOMCAT\bin\startup.bat"
Write-Host 'Waiting for Tomcat to start (6s)...'
Start-Sleep -Seconds 6
Start-Process 'http://localhost:8080/ta-recruitment/login'
Write-Host 'Done!  http://localhost:8080/ta-recruitment/login'