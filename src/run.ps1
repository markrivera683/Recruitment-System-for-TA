# build-and-run.ps1 - Compile, package, deploy and start Tomcat
# Usage: cd src; .\build-and-run.ps1

# ============================================================
#  CONFIGURE THESE TWO PATHS FOR YOUR MACHINE
# ============================================================
$DefaultJdkHome = 'D:\Apps\OpenJDKs\OpenJDK11.0.29'
$DefaultTomcat   = 'D:\Apps\IntelliJ Idea\apache-tomcat-9.0.115'

$JDK_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { $DefaultJdkHome }
$TOMCAT   = if ($env:CATALINA_HOME) { $env:CATALINA_HOME } else { $DefaultTomcat }
# ============================================================

function Get-Executable($name, $defaultPath) {
    if (Test-Path $defaultPath) { return $defaultPath }
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    return $null
}

$JAVAC       = Get-Executable 'javac.exe' "$JDK_HOME\bin\javac.exe"
$JAR         = Get-Executable 'jar.exe' "$JDK_HOME\bin\jar.exe"
$SERVLET_JAR = "$TOMCAT\lib\servlet-api.jar"

if ($JAVAC) { $env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $JAVAC) }
if ($TOMCAT) { $env:CATALINA_HOME = $TOMCAT }

Write-Host "Resolved JAVA_HOME=$env:JAVA_HOME"
Write-Host "Resolved CATALINA_HOME=$env:CATALINA_HOME"

# --- Validate paths ---
foreach ($p in @($JAVAC, $JAR, $SERVLET_JAR, "$TOMCAT\bin\startup.bat")) {
    if (-not (Test-Path $p)) { Write-Host "[ERROR] Not found: $p"; exit 1 }
}

$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$POSSIBLE_BASES = @(
    $SCRIPT_DIR,
    (Split-Path $SCRIPT_DIR -Parent)
)
$BASE = $POSSIBLE_BASES | Where-Object {
    (Test-Path (Join-Path $_ 'src\main\java')) -and (Test-Path (Join-Path $_ 'src\main\webapp'))
} | Select-Object -First 1

if (-not $BASE) {
    Write-Host "[WARN] Could not locate expected repository root. Using script directory as base: $SCRIPT_DIR"
    $BASE = $SCRIPT_DIR
}

$SRC    = Join-Path $BASE 'src\main\java'
$WEB    = Join-Path $BASE 'src\main\webapp'
$OUT    = Join-Path $BASE 'out'
$OUTBIN = Join-Path $OUT 'WEB-INF\classes'
$WAR    = Join-Path $BASE 'ta-recruitment.war'

# --- 1. Clean ---
Write-Host '[1/4] Cleaning...'
if (Test-Path $OUT) { Remove-Item -Recurse -Force $OUT }
New-Item -ItemType Directory -Force -Path $OUTBIN | Out-Null

# --- 2. Compile ---
Write-Host '[2/4] Compiling...'
$sources = Get-ChildItem -Path $SRC -Recurse -Filter *.java | Sort-Object FullName | Select-Object -ExpandProperty FullName
if (-not $sources) {
    Write-Host "[ERROR] No Java source files found in $SRC"
    exit 1
}
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